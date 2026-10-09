package com.eternalclash2.service;

import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.GameRepository;
import com.eternalclash2.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameRuleServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameRuleService gameRuleService;

    @Test
    void checkPlayerElimination_EliminatesWhenNoCities() {
        Game game = Game.builder().id(1L).build();
        Player player = Player.builder().id(100L).game(game).isAlive(true).build();
        
        // Return no cities owned by this player
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of());

        gameRuleService.checkPlayerElimination(player, 5);

        assertFalse(player.getIsAlive());
        assertEquals(5, player.getEliminatedAtTurn());
        verify(playerRepository).save(player);
    }

    @Test
    void checkPlayerElimination_DoesNotEliminateWhenHasCities() {
        Game game = Game.builder().id(1L).build();
        Player player = Player.builder().id(100L).game(game).isAlive(true).build();
        City city = City.builder().player(player).build();
        
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of(city));

        gameRuleService.checkPlayerElimination(player, 5);

        assertTrue(player.getIsAlive());
        verify(playerRepository, never()).save(any());
    }

    @Test
    void checkWinCondition_EndsGameWhenOnePlayerLeft() {
        Game game = Game.builder().id(1L).status(GameStatus.IN_PROGRESS).build();
        Player winner = Player.builder().id(100L).game(game).isAlive(true).build();
        
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(winner));

        gameRuleService.checkWinCondition(game);

        assertEquals(GameStatus.FINISHED, game.getStatus());
        assertEquals(winner, game.getWinner());
        verify(gameRepository).save(game);
    }
}
