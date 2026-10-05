package com.eternalclash2.service;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.entity.TurnAction;
import com.eternalclash2.domain.enums.ArmyStatus;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.domain.enums.Season;
import com.eternalclash2.domain.service.GameClock;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.ArmyRepository;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.GameRepository;
import com.eternalclash2.repository.PlayerRepository;
import com.eternalclash2.repository.TurnActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameViewService {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final CityRepository cityRepository;
    private final ArmyRepository armyRepository;
    private final TurnActionRepository turnActionRepository;

    @Transactional(readOnly = true)
    public GameSnapshot getSnapshot(Long gameId, Long viewerPlayerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));
        Player viewer = playerRepository.findById(viewerPlayerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + viewerPlayerId));
        if (!viewer.getGame().getId().equals(gameId)) throw new BusinessLogicException("Viewer does not belong to this game");

        int turn = game.getCurrentTurnNumber();
        List<PlayerSnapshot> players = playerRepository.findByGame_IdOrderById(gameId).stream().map(player -> {
            boolean own = player.getId().equals(viewerPlayerId);
            City city = cityRepository.findByPlayer_Id(player.getId()).orElse(null);
            return new PlayerSnapshot(player.getId(), player.getName(), Boolean.TRUE.equals(player.getIsAlive()),
                    player.getMarshal() == null ? null : player.getMarshal().getName(), own,
                    own && city != null ? city.getFood() : null,
                    own && city != null ? city.getSoldiers() : null);
        }).toList();

        List<ActionSnapshot> actions = GameClock.isDaytime(turn)
                ? turnActionRepository.findByGame_IdAndTurnNumber(gameId, turn).stream()
                    .map(action -> actionSnapshot(action, viewerPlayerId, turn)).toList()
                : List.of();

        List<ArmySnapshot> armies = armyRepository.findByTarget_Game_IdAndStatus(gameId, ArmyStatus.TRAVELING).stream()
                .map(army -> armySnapshot(army, viewerPlayerId, turn)).toList();
        return new GameSnapshot(game.getId(), game.getStatus(), turn, GameClock.season(Math.max(1, turn)),
                GameClock.isDaytime(Math.max(1, turn)), players, actions, armies);
    }

    private ActionSnapshot actionSnapshot(TurnAction action, Long viewerId, int turn) {
        Long targetId = null;
        if (action.getArmy() != null) {
            Player owner = action.getPlayer();
            Player target = action.getArmy().getTarget();
            boolean ownAction = viewerId.equals(owner.getId());
            boolean warned = viewerId.equals(target.getId()) && action.getArmy().getArrivalTurn() - turn <= 2;
            if (ownAction || warned || revealsTarget(owner)) targetId = target.getId();
        }
        return new ActionSnapshot(action.getPlayer().getId(), action.getActionType(), targetId);
    }

    private ArmySnapshot armySnapshot(Army army, Long viewerId, int turn) {
        boolean owner = army.getOwner().getId().equals(viewerId);
        boolean targetCanSee = army.getTarget().getId().equals(viewerId) && army.getArrivalTurn() - turn <= 2;
        boolean targetPublic = revealsTarget(army.getOwner());
        Long targetId = owner || targetCanSee || targetPublic ? army.getTarget().getId() : null;
        Integer soldiers = owner ? army.getSoldiers() : null;
        Integer arrivalTurn = owner || targetCanSee || targetPublic ? army.getArrivalTurn() : null;
        return new ArmySnapshot(army.getId(), army.getOwner().getId(), targetId, soldiers, arrivalTurn, army.getStatus());
    }

    private boolean revealsTarget(Player player) {
        return player.getMarshal() != null && Boolean.TRUE.equals(player.getMarshal().getRevealsAttackTarget());
    }

    public record GameSnapshot(Long gameId, GameStatus status, int currentTurn, Season season, boolean daytime,
                               List<PlayerSnapshot> players, List<ActionSnapshot> visibleActions,
                               List<ArmySnapshot> visibleArmies) {}
    public record PlayerSnapshot(Long playerId, String name, boolean alive, String marshalName, boolean isViewer,
                                 Integer food, Integer citySoldiers) {}
    public record ActionSnapshot(Long playerId, com.eternalclash2.domain.enums.ActionType actionType,
                                 Long visibleTargetPlayerId) {}
    public record ArmySnapshot(Long armyId, Long ownerPlayerId, Long visibleTargetPlayerId, Integer soldiers,
                               Integer arrivalTurn, ArmyStatus status) {}
}
