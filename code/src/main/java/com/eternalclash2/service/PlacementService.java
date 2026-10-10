package com.eternalclash2.service;

import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.GameRepository;
import com.eternalclash2.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlacementService {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final CityRepository cityRepository;

    @Transactional
    public void selectBase(Long gameId, Long playerId, Long cityId) {
        Game game = gameRepository.findByIdForUpdate(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));
                
        if (game.getStatus() != GameStatus.PLACEMENT) {
            throw new BusinessLogicException("Game is not in placement phase");
        }
        
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + playerId));
        if (!player.getGame().getId().equals(gameId)) throw new BusinessLogicException("Player does not belong to this game");
        
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + cityId));
        if (!city.getGame().getId().equals(gameId)) throw new BusinessLogicException("City does not belong to this game");
        
        player.setStartingCityId(cityId);
        playerRepository.save(player);
        
        checkAndResolvePlacement(gameId);
    }
    
    private void checkAndResolvePlacement(Long gameId) {
        List<Player> players = playerRepository.findByGame_IdOrderById(gameId);
        
        boolean allSelected = players.stream().allMatch(p -> p.getStartingCityId() != null);
        if (!allSelected) return;
        
        // Check for conflicts
        Map<Long, Integer> cityCounts = new HashMap<>();
        for (Player p : players) {
            cityCounts.put(p.getStartingCityId(), cityCounts.getOrDefault(p.getStartingCityId(), 0) + 1);
        }
        
        boolean hasConflict = cityCounts.values().stream().anyMatch(count -> count > 1);
        
        if (hasConflict) {
            // Reset all selections
            for (Player p : players) {
                p.setStartingCityId(null);
            }
            playerRepository.saveAll(players);
            // Optionally: Emit some event or push notification to clients that a conflict occurred
        } else {
            // Assign cities to players
            for (Player p : players) {
                City city = cityRepository.findById(p.getStartingCityId())
                        .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + p.getStartingCityId()));
                city.setPlayer(p);
                city.setName(p.getName() + "'s " + city.getName());
                city.setSoldiers(0);
                city.setFood(50);
                cityRepository.save(city);
                
                p.setStartingCityId(null); // Clear after assignment
            }
            playerRepository.saveAll(players);
            
            Game game = gameRepository.findByIdForUpdate(gameId).orElseThrow();
            game.setStatus(GameStatus.IN_PROGRESS);
            game.setCurrentTurnNumber(1);
            gameRepository.save(game);
        }
    }
}
