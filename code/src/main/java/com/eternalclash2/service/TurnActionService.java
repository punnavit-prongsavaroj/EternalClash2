package com.eternalclash2.service;

import com.eternalclash2.command.*;
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
import com.eternalclash2.repository.GameEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TurnActionService {
    private final TurnActionRepository turnActionRepository;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final CityRepository cityRepository;
    private final CityService cityService;
    private final ArmyService armyService;
    private final GameEventRepository gameEventRepository;

    @Transactional
    public TurnAction performAction(Long gameId, Long playerId, Long cityId, ActionType requestedAction,
                                    Long targetCityId, Integer soldierCount) {
        Game game = gameRepository.findByIdForUpdate(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + playerId));
        if (!player.getGame().getId().equals(gameId)) throw new BusinessLogicException("Player does not belong to this game");
        
        new com.eternalclash2.state.GameStateContext(game.getStatus()).getCurrentState().validateActionSubmission();

        if (!Boolean.TRUE.equals(player.getIsAlive())) throw new BusinessLogicException("Eliminated players cannot take actions");
        
        int turn = game.getCurrentTurnNumber();
        if (turnActionRepository.existsByGame_IdAndTurnNumberAndCity_Id(gameId, turn, cityId)) {
            return turnActionRepository.findByGame_IdAndTurnNumber(gameId, turn).stream()
                    .filter(a -> a.getCity().getId().equals(cityId)).findFirst().orElseThrow();
        }
        if (requestedAction == null) throw new BusinessLogicException("Action type is required");

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found: " + cityId));
        
        if (city.getPlayer() == null || !city.getPlayer().getId().equals(playerId)) {
            throw new BusinessLogicException("You do not own this city");
        }
                
        int foodBefore = city.getFood();
        int soldiersBefore = city.getSoldiers();

        PlayerActionCommand command = switch (requestedAction) {
            case PRODUCE_FOOD -> new ProduceFoodCommand(cityService, cityId, turn);
            case RECRUIT_SOLDIERS -> new RecruitSoldiersCommand(cityService, cityId, turn);
            case SEND_ARMY -> new SendArmyCommand(armyService, city, targetCityId, soldierCount, turn, gameEventRepository, game);
            case NONE -> new NoneCommand();
        };

        command.execute();

        City after = cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found: " + cityId));
                
        return turnActionRepository.save(TurnAction.builder().game(game).turnNumber(turn).player(player).city(city)
                .actionType(command.getRecordedAction()).army(command.getArmy()).foodBefore(foodBefore).foodAfter(after.getFood())
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
}
