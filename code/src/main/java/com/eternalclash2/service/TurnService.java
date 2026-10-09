package com.eternalclash2.service;

import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.service.GameClock;
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

@Service
@RequiredArgsConstructor
public class TurnService {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final TurnActionRepository turnActionRepository;
    private final GameEventService gameEventService;
    private final BattleService battleService;
    private final CityService cityService;
    private final CityRepository cityRepository;

    @Transactional
    public Game resolveAndAdvance(Long gameId) {
        Game game = gameRepository.findByIdForUpdate(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));
                
        new com.eternalclash2.state.GameStateContext(game.getStatus()).getCurrentState().validateTurnResolution();

        int currentTurn = game.getCurrentTurnNumber();
        List<Player> alivePlayers = playerRepository.findByGame_IdOrderById(gameId).stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsAlive())).toList();
        if (alivePlayers.isEmpty()) throw new BusinessLogicException("Game has no active players");
        
        List<City> allCities = cityRepository.findByGame_Id(gameId);
        boolean allActed = allCities.stream()
                .filter(c -> c.getPlayer() != null && Boolean.TRUE.equals(c.getPlayer().getIsAlive()))
                .allMatch(c -> Boolean.TRUE.equals(c.getActionUsedThisTurn()));
                
        if (!allActed) return game; // Wait for everyone instead of throwing an error

        gameEventService.processTurn(gameId, currentTurn);
        battleService.resolveBattles(gameId, currentTurn);
        
        Game refreshed = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));
        if (refreshed.getStatus() == GameStatus.FINISHED) return refreshed;

        if (GameClock.isSeasonEnd(currentTurn)) {
            cityService.applySeasonUpkeep(gameId);
        }
        
        // Reset action tracking for next turn
        allCities.forEach(c -> c.setActionUsedThisTurn(false));
        cityRepository.saveAll(allCities);
        
        refreshed.setCurrentTurnNumber(currentTurn + 1);
        return gameRepository.save(refreshed);
    }

    @Transactional(readOnly = true)
    public Game getCurrentGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));
    }
}
