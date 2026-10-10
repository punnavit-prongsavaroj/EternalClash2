package com.eternalclash2.service;

import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    private Game game;

    @BeforeEach
    void buildFixtures() {
        game = Game.builder().id(1L).build();
    }

    private Player player(Long id, boolean alive) {
        return Player.builder().id(id).name("Player" + id).game(game).isAlive(alive).build();
    }

    @Test
    void tc01_findById_returnsTheStoredPlayer() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player(1L, true)));

        assertEquals(1L, playerService.findById(1L).getId());
    }

    @Test
    void tc02_findById_withUnknownId_throwsResourceNotFoundException() {
        when(playerRepository.findById(2L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> playerService.findById(2L));

        assertEquals("Player not found with id: 2", exception.getMessage());
    }

    @Test
    void tc03_getAlivePlayerValidated_returnsAnAlivePlayer() {
        Player alive = player(1L, true);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(alive));

        assertTrue(playerService.getAlivePlayerValidated(1L).getIsAlive());
    }

    @Test
    void tc04_getAlivePlayerValidated_withUnknownId_reportsNotFound() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> playerService.getAlivePlayerValidated(99L));

        assertEquals("Player not found with id: 99", exception.getMessage());
    }

    @Test
    void tc05_getAlivePlayerValidated_forAnEliminatedPlayer_isRejected() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player(1L, false)));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> playerService.getAlivePlayerValidated(1L));

        assertEquals("Eliminated players cannot take actions", exception.getMessage());
    }

    @Test
    void tc06_findByGame_returnsEveryPlayerOfTheGame() {
        when(playerRepository.findByGame_IdOrderById(1L))
                .thenReturn(List.of(player(1L, true), player(2L, true), player(3L, false)));

        assertEquals(3, playerService.findByGame(1L).size());
    }

    @Test
    void tc07_findAliveByGame_filtersOutEliminatedPlayers() {
        when(playerRepository.findByGame_IdOrderById(1L))
                .thenReturn(List.of(player(1L, true), player(2L, false), player(3L, true)));

        List<Player> alive = playerService.findAliveByGame(1L);

        assertEquals(2, alive.size());
        assertEquals(1L, alive.get(0).getId());
        assertEquals(3L, alive.get(1).getId());
    }
}
