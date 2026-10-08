import re

with open('code/src/main/java/com/eternalclash2/service/GameViewService.java', 'r', encoding='utf-8') as f:
    java = f.read()

new_content = '''package com.eternalclash2.service;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.MapEdge;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.entity.TurnAction;
import com.eternalclash2.domain.enums.ArmyStatus;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.domain.enums.Season;
import com.eternalclash2.service.GameClock;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.ArmyRepository;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.MapEdgeRepository;
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
    private final MapEdgeRepository mapEdgeRepository;
    private final ArmyRepository armyRepository;
    private final TurnActionRepository turnActionRepository;

    @Transactional(readOnly = true)
    public GameSnapshot getSnapshot(Long gameId, Long viewerPlayerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
        Player viewer = playerRepository.findById(viewerPlayerId)
                .orElseThrow(() -> new ResourceNotFoundException("Viewer not found"));
        
        int turn = game.getCurrentTurnNumber();
        
        List<PlayerSnapshot> players = playerRepository.findByGame_IdOrderById(gameId).stream().map(player -> 
            new PlayerSnapshot(player.getId(), player.getName(), Boolean.TRUE.equals(player.getIsAlive()),
                    player.getMarshal() == null ? null : player.getMarshal().getName(), player.getId().equals(viewerPlayerId))
        ).toList();

        List<NodeSnapshot> nodes = cityRepository.findByGame_Id(gameId).stream().map(city -> {
            boolean isOwner = city.getPlayer() != null && city.getPlayer().getId().equals(viewerPlayerId);
            boolean showSoldiers = GameClock.isDaytime(Math.max(1, turn)) || isOwner || city.getPlayer() == null;
            return new NodeSnapshot(
                city.getId(),
                city.getX(),
                city.getY(),
                city.getPlayer() != null ? city.getPlayer().getId() : null,
                isOwner ? city.getFood() : null, // Only owner sees food
                showSoldiers ? city.getSoldiers() : null,
                city.isActionUsedThisTurn()
            );
        }).toList();

        List<EdgeSnapshot> edges = mapEdgeRepository.findByGame_Id(gameId).stream().map(edge -> 
            new EdgeSnapshot(edge.getId(), edge.getCity1().getId(), edge.getCity2().getId())
        ).toList();

        int prevTurn = turn - 1;
        List<ActionSnapshot> actions = prevTurn > 0
                ? turnActionRepository.findByGame_IdAndTurnNumber(gameId, prevTurn).stream()
                    .filter(action -> GameClock.isDaytime(prevTurn) || action.getPlayer().getId().equals(viewerPlayerId))
                    .map(action -> actionSnapshot(action, viewerPlayerId, prevTurn)).toList()
                : List.of();

        List<ArmySnapshot> armies = armyRepository.findByTargetCity_Game_IdAndStatus(gameId, ArmyStatus.TRAVELING).stream()
                .map(army -> armySnapshot(army, viewerPlayerId, turn)).toList();

        return new GameSnapshot(game.getId(), game.getStatus(), turn, GameClock.season(Math.max(1, turn)),
                GameClock.isDaytime(Math.max(1, turn)), players, nodes, edges, actions, armies);
    }

    private ActionSnapshot actionSnapshot(TurnAction action, Long viewerId, int turn) {
        Long targetId = null;
        if (action.getArmy() != null) {
            Player owner = action.getPlayer();
            Player target = action.getArmy().getTargetCity().getPlayer();
            boolean ownAction = viewerId.equals(owner.getId());
            boolean targetIsNeutralOrMe = target == null || viewerId.equals(target.getId());
            boolean warned = targetIsNeutralOrMe && action.getArmy().getArrivalTurn() - turn <= 2;
            if (ownAction || warned || revealsTarget(owner)) targetId = action.getArmy().getTargetCity().getId();
        }
        return new ActionSnapshot(action.getPlayer().getId(), action.getCity().getId(), action.getActionType(), targetId);
    }

    private ArmySnapshot armySnapshot(Army army, Long viewerId, int turn) {
        boolean owner = army.getOwner().getId().equals(viewerId);
        Player targetPlayer = army.getTargetCity().getPlayer();
        boolean targetCanSee = (targetPlayer == null || targetPlayer.getId().equals(viewerId)) && army.getArrivalTurn() - turn <= 2;
        boolean targetPublic = revealsTarget(army.getOwner());
        Long targetId = owner || targetCanSee || targetPublic ? army.getTargetCity().getId() : null;
        Integer soldiers = owner ? army.getSoldiers() : null;
        Integer arrivalTurn = owner || targetCanSee || targetPublic ? army.getArrivalTurn() : null;
        return new ArmySnapshot(army.getId(), army.getOwner().getId(), army.getSourceCity().getId(), targetId, soldiers, arrivalTurn, army.getStatus());
    }

    private boolean revealsTarget(Player player) {
        return player.getMarshal() != null && Boolean.TRUE.equals(player.getMarshal().getRevealsAttackTarget());
    }

    public record GameSnapshot(Long gameId, GameStatus status, int currentTurn, Season season, boolean daytime,
                               List<PlayerSnapshot> players, List<NodeSnapshot> nodes, List<EdgeSnapshot> edges, 
                               List<ActionSnapshot> visibleActions, List<ArmySnapshot> visibleArmies) {}
    public record PlayerSnapshot(Long playerId, String name, boolean alive, String marshalName, boolean isViewer) {}
    public record NodeSnapshot(Long nodeId, Double x, Double y, Long ownerId, Integer food, Integer soldiers, boolean actionUsedThisTurn) {}
    public record EdgeSnapshot(Long edgeId, Long node1Id, Long node2Id) {}
    public record ActionSnapshot(Long playerId, Long sourceCityId, com.eternalclash2.domain.enums.ActionType actionType, Long visibleTargetCityId) {}
    public record ArmySnapshot(Long armyId, Long ownerPlayerId, Long sourceCityId, Long visibleTargetCityId, Integer soldiers, Integer arrivalTurn, ArmyStatus status) {}
}
'''

with open('code/src/main/java/com/eternalclash2/service/GameViewService.java', 'w', encoding='utf-8') as f:
    f.write(new_content)
print("Updated GameViewService.java")
