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
import com.eternalclash2.repository.TurnActionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurnServiceTest {

    @Mock
    private GameRepository gameRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private TurnActionRepository turnActionRepository;
    @Mock
    private GameEventService gameEventService;
    @Mock
    private BattleService battleService;
    @Mock
    private CityService cityService;
    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private TurnService turnService;

    private Game game;
    private Player player1;
    private Player player2;

    @BeforeEach
    void buildFixtures() {
        game = Game.builder().id(1L).status(GameStatus.IN_PROGRESS).currentTurnNumber(1).build();
        player1 = Player.builder().id(100L).name("Player1").game(game).isAlive(true).build();
        player2 = Player.builder().id(200L).name("Player2").game(game).isAlive(true).build();
    }

    private City city(Long id, Player owner, boolean acted) {
        return City.builder().id(id).game(game).player(owner).food(50).soldiers(20)
                .actionUsedThisTurn(acted).build();
    }

    private void stubAlivePlayers() {
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(player1, player2));
    }

    @Test
    void tc01_resolveAndAdvance_whenEveryoneActed_closesTheTurnAndAdvances() {
        City city1 = city(10L, player1, true);
        City city2 = city(11L, player2, true);
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        stubAlivePlayers();
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of(city1, city2));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(gameRepository.save(game)).thenReturn(game);

        Game advanced = turnService.resolveAndAdvance(1L);

        verify(gameEventService).processTurn(1L, 1);
        verify(battleService).resolveBattles(1L, 1);
        assertFalse(city1.getActionUsedThisTurn());
        assertFalse(city2.getActionUsedThisTurn());
        verify(cityRepository).saveAll(List.of(city1, city2));
        assertEquals(2, advanced.getCurrentTurnNumber());
        verify(gameRepository).save(game);
    }

    @Test
    void tc02_resolveAndAdvance_resetsEveryActedCity() {
        List<City> cities = List.of(city(10L, player1, true), city(11L, player2, true),
                city(12L, player1, true), city(13L, player2, true));
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        stubAlivePlayers();
        when(cityRepository.findByGame_Id(1L)).thenReturn(cities);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        turnService.resolveAndAdvance(1L);

        assertTrue(cities.stream().noneMatch(c -> Boolean.TRUE.equals(c.getActionUsedThisTurn())));
    }

    @Test
    void tc03_resolveAndAdvance_whileACityHasNotActed_waitsWithoutAdvancing() {
        City acted = city(10L, player1, true);
        City idle = city(11L, player2, false);
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        stubAlivePlayers();
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of(acted, idle));

        Game result = turnService.resolveAndAdvance(1L);

        assertSame(game, result);
        verify(gameEventService, never()).processTurn(anyLong(), anyInt());
        verify(battleService, never()).resolveBattles(anyLong(), anyInt());
        assertEquals(1, result.getCurrentTurnNumber());
    }

    @Test
    void tc04_resolveAndAdvance_withNoAlivePlayer_isRejected() {
        Player eliminated = Player.builder().id(100L).name("Player1").game(game).isAlive(false).build();
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(eliminated));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> turnService.resolveAndAdvance(1L));

        assertEquals("Game has no active players", exception.getMessage());
    }

    @Test
    void tc05_resolveAndAdvance_withUnknownGame_throwsResourceNotFoundException() {
        when(gameRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> turnService.resolveAndAdvance(99L));

        assertEquals("Game not found with id: 99", exception.getMessage());
    }

    @Test
    void tc06_resolveAndAdvance_returnsDirectlyWhenTheBattleFinishedTheGame() {
        City acted = city(10L, player1, true);
        Game finished = Game.builder().id(1L).status(GameStatus.FINISHED).currentTurnNumber(1).build();
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(player1, player2));
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of(acted));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(finished));

        Game result = turnService.resolveAndAdvance(1L);

        assertSame(finished, result);
        verify(cityService, never()).applySeasonUpkeep(anyLong());
        verify(cityRepository, never()).saveAll(any());
        verify(gameRepository, never()).save(any());
    }

    @Test
    void tc07_resolveAndAdvance_onTheLastTurnOfASeason_appliesSeasonUpkeep() {
        game.setCurrentTurnNumber(4);
        City acted = city(10L, player1, true);
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        stubAlivePlayers();
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of(acted));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        turnService.resolveAndAdvance(1L);

        verify(cityService, times(1)).applySeasonUpkeep(1L);
    }

    @Test
    void tc08_resolveAndAdvance_midSeason_skipsSeasonUpkeep() {
        game.setCurrentTurnNumber(3);
        City acted = city(10L, player1, true);
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        stubAlivePlayers();
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of(acted));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        turnService.resolveAndAdvance(1L);

        verify(cityService, never()).applySeasonUpkeep(anyLong());
    }

    @Test
    void tc09_resolveAndAdvance_beforeTheBattlePhase_isRejectedByTheStateMachine() {
        Game waiting = Game.builder().id(1L).status(GameStatus.WAITING).currentTurnNumber(0).build();
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(waiting));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> turnService.resolveAndAdvance(1L));

        assertEquals("ไม่สามารถจบเทิร์นได้ เกมกำลังรอผู้เล่นอื่น", exception.getMessage());
    }

    @Test
    void tc10_getCurrentGame_returnsTheStoredGame() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        assertSame(game, turnService.getCurrentGame(1L));
    }
}
