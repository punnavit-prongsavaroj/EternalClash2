package com.eternalclash2.service;

import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.GameRepository;
import com.eternalclash2.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameRuleService {
    private final PlayerRepository playerRepository;
    private final CityRepository cityRepository;
    private final GameRepository gameRepository;

    @Transactional
    public void checkPlayerElimination(Player player, int turnNumber) {
        long citiesOwned = cityRepository.findByGame_Id(player.getGame().getId()).stream()
                .filter(c -> c.getPlayer() != null && c.getPlayer().getId().equals(player.getId())).count();
                
        if (citiesOwned == 0) {
            player.setIsAlive(false);
            player.setEliminatedAtTurn(turnNumber);
            playerRepository.save(player);
        }
    }

    @Transactional
    public void checkWinCondition(Game game) {
        List<Player> alive = playerRepository.findByGame_IdOrderById(game.getId()).stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsAlive())).toList();
        if (alive.size() <= 1 && game.getStatus() != GameStatus.WAITING && game.getStatus() != GameStatus.PLACEMENT && game.getStatus() != GameStatus.MARSHAL_SELECTION) {
            game.setStatus(GameStatus.FINISHED);
            game.setWinner(alive.isEmpty() ? null : alive.get(0));
            gameRepository.save(game);
        }
    }
}
