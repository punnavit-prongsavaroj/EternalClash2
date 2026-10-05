package com.eternalclash2.service;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.entity.TurnAction;
import com.eternalclash2.domain.enums.ActionType;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.GameRepository;
import com.eternalclash2.repository.PlayerRepository;
import com.eternalclash2.repository.TurnActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class TurnActionService {
    private final TurnActionRepository turnActionRepository;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final CityRepository cityRepository;
    private final CityService cityService;
    private final ArmyService armyService;

    @Transactional
    public TurnAction performAction(Long gameId, Long playerId, ActionType requestedAction,
                                    Long targetPlayerId, Integer soldierCount) {
        Game game = gameRepository.findByIdForUpdate(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + playerId));
        if (!player.getGame().getId().equals(gameId)) throw new BusinessLogicException("Player does not belong to this game");
        if (game.getStatus() != GameStatus.IN_PROGRESS) throw new BusinessLogicException("Game is not in progress");
        if (!Boolean.TRUE.equals(player.getIsAlive())) throw new BusinessLogicException("Eliminated players cannot take actions");
        int turn = game.getCurrentTurnNumber();
        if (turnActionRepository.existsByGame_IdAndTurnNumberAndPlayer_Id(gameId, turn, playerId)) {
            throw new BusinessLogicException("Player has already submitted an action for this turn");
        }
        if (requestedAction == null) throw new BusinessLogicException("Action type is required");

        City city = cityRepository.findByPlayer_Id(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found for player: " + playerId));
        int foodBefore = city.getFood();
        int soldiersBefore = city.getSoldiers();
        ActionType recordedAction = requestedAction;
        Army army = null;

        switch (requestedAction) {
            case PRODUCE_FOOD -> cityService.produceFood(playerId, turn);
            case RECRUIT_SOLDIERS -> cityService.recruitSoldiers(playerId, turn);
            case SEND_ARMY -> {
                if (targetPlayerId == null || soldierCount == null) throw new BusinessLogicException("Target and soldier count are required to send an army");
                if (hasSpecial(player, "SURVIVE_DESTRUCTION") && ThreadLocalRandom.current().nextInt(100) < 20) {
                    recordedAction = ActionType.NONE;
                } else {
                    army = armyService.sendArmy(playerId, targetPlayerId, soldierCount, turn);
                }
            }
            case NONE -> { }
        }
        City after = cityRepository.findByPlayer_Id(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found for player: " + playerId));
        return turnActionRepository.save(TurnAction.builder().game(game).turnNumber(turn).player(player)
                .actionType(recordedAction).army(army).foodBefore(foodBefore).foodAfter(after.getFood())
                .soldiersBefore(soldiersBefore).soldiersAfter(after.getSoldiers()).build());
    }

    @Transactional(readOnly = true)
    public List<TurnAction> findTurnActions(Long gameId, int turnNumber) {
        return turnActionRepository.findByGame_IdAndTurnNumber(gameId, turnNumber);
    }

    @Transactional(readOnly = true)
    public List<TurnAction> findAll() { return turnActionRepository.findAll(); }

    @Transactional(readOnly = true)
    public TurnAction findById(Long id) {
        return turnActionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("TurnAction not found with id: " + id));
    }

    private boolean hasSpecial(Player player, String type) {
        return player.getMarshal() != null && type.equals(player.getMarshal().getSpecialAbilityType());
    }
}
