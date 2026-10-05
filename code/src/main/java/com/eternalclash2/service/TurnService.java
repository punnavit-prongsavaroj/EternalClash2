package com.eternalclash2.service;

import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.domain.service.GameClock;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.GameRepository;
import com.eternalclash2.repository.PlayerRepository;
import com.eternalclash2.repository.TurnActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TurnService {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final TurnActionRepository turnActionRepository;
    private final GameEventService gameEventService;
    private final BattleService battleService;
    private final CityService cityService;

    /** Resolve the current turn after each living player has submitted exactly one action. */
    @Transactional
    public Game resolveAndAdvance(Long gameId) {
        Game game = gameRepository.findByIdForUpdate(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));
        if (game.getStatus() != GameStatus.IN_PROGRESS) throw new BusinessLogicException("Game is not in progress");
        int currentTurn = game.getCurrentTurnNumber();
        List<Player> alivePlayers = playerRepository.findByGame_IdOrderById(gameId).stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsAlive())).toList();
        if (alivePlayers.isEmpty()) throw new BusinessLogicException("Game has no active players");
        boolean allActed = alivePlayers.stream().allMatch(p -> turnActionRepository
                .existsByGame_IdAndTurnNumberAndPlayer_Id(gameId, currentTurn, p.getId()));
        if (!allActed) throw new BusinessLogicException("Every living player must submit an action before the turn resolves");

        gameEventService.processTurn(gameId, currentTurn);
        battleService.resolveTurnBattles(gameId, currentTurn);
        Game refreshed = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));
        if (refreshed.getStatus() == GameStatus.FINISHED) return refreshed;

        if (GameClock.isSeasonEnd(currentTurn)) {
            for (Player player : playerRepository.findByGame_IdOrderById(gameId)) {
                if (Boolean.TRUE.equals(player.getIsAlive())) cityService.applySeasonUpkeep(player.getId());
            }
        }
        refreshed.setCurrentTurnNumber(currentTurn + 1);
        return gameRepository.save(refreshed);
    }

    @Transactional(readOnly = true)
    public Game getCurrentGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));
    }
}
