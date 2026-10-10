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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameRuleServiceTest {

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameRuleService gameRuleService;

    private Game game(GameStatus status) {
        return Game.builder().id(1L).status(status).build();
    }

    private Player player(Long id, Game game, boolean alive) {
        return Player.builder().id(id).name("Player" + id).game(game).isAlive(alive).build();
    }

    private City ownedBy(Game game, Player owner) {
        return City.builder().id(10L).game(game).player(owner).build();
    }

    @Test
    void tc01_checkPlayerElimination_withoutCities_eliminateThePlayer() {
        Game game = game(GameStatus.IN_PROGRESS);
        Player player = player(100L, game, true);
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of());

        gameRuleService.checkPlayerElimination(player, 5);

        assertFalse(player.getIsAlive());
        assertEquals(5, player.getEliminatedAtTurn());
        verify(playerRepository).save(player);
    }

    @Test
    void tc02_checkPlayerElimination_withOneCityLeft_survives() {
        Game game = game(GameStatus.IN_PROGRESS);
        Player player = player(100L, game, true);
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of(ownedBy(game, player)));

        gameRuleService.checkPlayerElimination(player, 5);

        assertTrue(player.getIsAlive());
        verify(playerRepository, never()).save(any());
    }

    @Test
    void tc03_checkWinCondition_withTheLastAlivePlayer_declaresThatWinner() {
        Game game = game(GameStatus.IN_PROGRESS);
        Player winner = player(100L, game, true);
        Player loser = player(200L, game, false);
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(winner, loser));

        gameRuleService.checkWinCondition(game);

        assertEquals(GameStatus.FINISHED, game.getStatus());
        assertSame(winner, game.getWinner());
        verify(gameRepository).save(game);
    }

    @Test
    void tc04_checkWinCondition_withNobodyAlive_finishesWithoutAWinner() {
        Game game = game(GameStatus.IN_PROGRESS);
        when(playerRepository.findByGame_IdOrderById(1L))
                .thenReturn(List.of(player(100L, game, false), player(200L, game, false)));

        gameRuleService.checkWinCondition(game);

        assertEquals(GameStatus.FINISHED, game.getStatus());
        assertNull(game.getWinner());
        verify(gameRepository).save(game);
    }

    @Test
    void tc05_checkWinCondition_whileTwoPlayersRemain_theGameContinues() {
        Game game = game(GameStatus.IN_PROGRESS);
        when(playerRepository.findByGame_IdOrderById(1L))
                .thenReturn(List.of(player(100L, game, true), player(200L, game, true)));

        gameRuleService.checkWinCondition(game);

        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        verify(gameRepository, never()).save(any());
    }

    @Test
    void tc06_checkWinCondition_beforeTheBattlePhase_theGuardBlocksFinishing() {
        for (GameStatus phase : List.of(GameStatus.WAITING, GameStatus.PLACEMENT, GameStatus.MARSHAL_SELECTION)) {
            Game game = game(phase);
            when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of());

            gameRuleService.checkWinCondition(game);

            assertEquals(phase, game.getStatus());
        }
        verify(gameRepository, never()).save(any());
    }

    @Test
    void tc07_checkPlayerElimination_isNotIdempotentSoTheSecondCallSavesAgain() {
        Game game = game(GameStatus.IN_PROGRESS);
        Player player = player(100L, game, false);
        player.setEliminatedAtTurn(3);
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of());

        gameRuleService.checkPlayerElimination(player, 7);

        assertFalse(player.getIsAlive());
        assertEquals(7, player.getEliminatedAtTurn());
        verify(playerRepository, times(1)).save(player);
    }
}
