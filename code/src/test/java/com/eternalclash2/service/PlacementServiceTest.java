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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlacementServiceTest {

    @Mock
    private GameRepository gameRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private PlacementService placementService;

    private Game game;
    private Player player1;
    private Player player2;
    private City city1;
    private City city2;

    @BeforeEach
    void buildFixtures() {
        game = Game.builder().id(1L).status(GameStatus.PLACEMENT).currentTurnNumber(0).build();
        player1 = Player.builder().id(1L).name("Player1").game(game).isAlive(true).build();
        player2 = Player.builder().id(2L).name("Player2").game(game).isAlive(true).build();
        city1 = City.builder().id(1L).game(game).name("City1").soldiers(30).food(0).build();
        city2 = City.builder().id(2L).game(game).name("City2").soldiers(30).food(0).build();
    }

    private void stubPlacementLookups() {
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
    }

    private void stubPlayer(Long id, Player player) {
        when(playerRepository.findById(id)).thenReturn(Optional.of(player));
    }

    private void stubCity(Long id, City city) {
        when(cityRepository.findById(id)).thenReturn(Optional.of(city));
    }

    @Test
    void tc01_selectBase_givesEveryPlayerADistinctCityAndStartsTheGame() {
        stubPlacementLookups();
        stubPlayer(1L, player1);
        stubPlayer(2L, player2);
        stubCity(1L, city1);
        stubCity(2L, city2);
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(player1, player2));

        placementService.selectBase(1L, 1L, 1L);
        placementService.selectBase(1L, 2L, 2L);

        assertSame(player1, city1.getPlayer());
        assertEquals("Player1's City1", city1.getName());
        assertEquals(0, city1.getSoldiers());
        assertEquals(50, city1.getFood());
        assertSame(player2, city2.getPlayer());
        assertEquals("Player2's City2", city2.getName());
        assertNull(player1.getStartingCityId());
        assertNull(player2.getStartingCityId());
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertEquals(1, game.getCurrentTurnNumber());
        verify(gameRepository).save(game);
    }

    @Test
    void tc02_selectBase_onTheSameCity_resetsBothSelections() {
        stubPlacementLookups();
        stubPlayer(1L, player1);
        stubPlayer(2L, player2);
        stubCity(1L, city1);
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(player1, player2));

        placementService.selectBase(1L, 1L, 1L);
        placementService.selectBase(1L, 2L, 1L);

        assertNull(player1.getStartingCityId());
        assertNull(player2.getStartingCityId());
        verify(playerRepository).saveAll(List.of(player1, player2));
        assertEquals(GameStatus.PLACEMENT, game.getStatus());
        verify(cityRepository, never()).save(any(City.class));
    }

    @Test
    void tc03_selectBase_whileAnotherPlayerHasNotChosen_waits() {
        stubPlacementLookups();
        stubPlayer(1L, player1);
        stubCity(1L, city1);
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(player1, player2));

        placementService.selectBase(1L, 1L, 1L);

        assertEquals(1L, player1.getStartingCityId());
        assertNull(city1.getPlayer());
        assertEquals(GameStatus.PLACEMENT, game.getStatus());
        verify(cityRepository, never()).save(any(City.class));
    }

    @Test
    void tc04_selectBase_outsideThePlacementPhase_isRejected() {
        game.setStatus(GameStatus.IN_PROGRESS);
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> placementService.selectBase(1L, 1L, 1L));

        assertEquals("Game is not in placement phase", exception.getMessage());
    }

    @Test
    void tc05_selectBase_withUnknownGame_throwsResourceNotFoundException() {
        when(gameRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> placementService.selectBase(99L, 1L, 1L));

        assertEquals("Game not found with id: 99", exception.getMessage());
    }

    @Test
    void tc06_selectBase_withAPlayerFromAnotherGame_isRejected() {
        Game otherGame = Game.builder().id(2L).status(GameStatus.PLACEMENT).build();
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(
                Player.builder().id(1L).name("Player1").game(otherGame).isAlive(true).build()));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> placementService.selectBase(1L, 1L, 1L));

        assertEquals("Player does not belong to this game", exception.getMessage());
    }

    @Test
    void tc07_selectBase_withUnknownPlayer_throwsResourceNotFoundException() {
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> placementService.selectBase(1L, 99L, 1L));

        assertEquals("Player not found with id: 99", exception.getMessage());
    }

    @Test
    void tc08_selectBase_withACityFromAnotherGame_isRejected() {
        Game otherGame = Game.builder().id(2L).status(GameStatus.PLACEMENT).build();
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(cityRepository.findById(1L)).thenReturn(Optional.of(
                City.builder().id(1L).game(otherGame).name("City1").build()));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> placementService.selectBase(1L, 1L, 1L));

        assertEquals("City does not belong to this game", exception.getMessage());
    }

    @Test
    void tc09_selectBase_withUnknownCity_throwsResourceNotFoundException() {
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(cityRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> placementService.selectBase(1L, 1L, 99L));

        assertEquals("City not found with id: 99", exception.getMessage());
    }

    @Test
    void tc10_selectBase_withNoPlayersListedStillStartsTheGame() {
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city1));
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of());

        placementService.selectBase(1L, 1L, 1L);

        // allMatch on an empty stream is true, so the game resolves and advances with nobody on the board.
        assertEquals(1L, player1.getStartingCityId());
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertEquals(1, game.getCurrentTurnNumber());
        verify(playerRepository).saveAll(List.of());
        verify(cityRepository, never()).save(any(City.class));
    }
}
