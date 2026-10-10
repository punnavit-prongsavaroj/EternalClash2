package com.eternalclash2.service;

import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.MapEdge;
import com.eternalclash2.domain.entity.Marshal;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.GameRepository;
import com.eternalclash2.repository.MapEdgeRepository;
import com.eternalclash2.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private MapEdgeRepository mapEdgeRepository;
    @Mock
    private MarshalService marshalService;
    @Mock
    private MarshalDraftService marshalDraftService;

    @InjectMocks
    private GameService gameService;

    private Game waitingGame;
    private long generatedCityId = 1L;

    @BeforeEach
    void buildFixtures() {
        waitingGame = Game.builder().id(1L).status(GameStatus.WAITING).currentTurnNumber(0).build();
    }

    /** startGame builds its map from persisted cities, so the mock has to hand back an id like JPA does. */
    private void stubCityPersistence() {
        when(cityRepository.save(any(City.class))).thenAnswer(invocation -> {
            City city = invocation.getArgument(0);
            city.setId(generatedCityId++);
            return city;
        });
    }

    private List<Marshal> marshals(int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(i -> Marshal.builder().id((long) i).name("Marshal" + i).build())
                .toList();
    }

    @Test
    void tc01_createGame_startsAWaitingGameWithASixCharacterRoomCode() {
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Game created = gameService.createGame();

        assertNotNull(created);
        assertEquals(GameStatus.WAITING, created.getStatus());
        assertEquals(0, created.getCurrentTurnNumber());
        assertEquals(6, created.getRoomCode().length());
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void tc02_addPlayer_savesANewAlivePlayerWithStats() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(waitingGame));
        when(playerRepository.countByGame_Id(1L)).thenReturn(1L);
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Player player = gameService.addPlayer(1L, "Player1");

        assertEquals("Player1", player.getName());
        assertEquals(Boolean.TRUE, player.getIsAlive());
        assertEquals(0, player.getRerollCount());
        assertNotNull(player.getPlayerStats());
    }

    @Test
    void tc03_addPlayer_onAFullGame_isRejected() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(waitingGame));
        when(playerRepository.countByGame_Id(1L)).thenReturn(7L);

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> gameService.addPlayer(1L, "Player8"));

        assertEquals("A game can have at most 7 players", exception.getMessage());
    }

    @Test
    void tc04_addPlayer_afterTheStart_isRejected() {
        waitingGame.setStatus(GameStatus.IN_PROGRESS);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(waitingGame));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> gameService.addPlayer(1L, "Player2"));

        assertEquals("Game is no longer accepting players", exception.getMessage());
    }

    @Test
    void tc05_addPlayer_withABlankName_isRejected() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(waitingGame));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> gameService.addPlayer(1L, "   "));

        assertEquals("Player name is required", exception.getMessage());
    }

    @Test
    void tc06_startGame_withASinglePlayer_isRejected() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(waitingGame));
        when(playerRepository.countByGame_Id(1L)).thenReturn(1L);

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> gameService.startGame(1L));

        assertEquals("A game requires between 2 and 7 players", exception.getMessage());
    }

    @Test
    void tc07_startGame_withTwoPlayers_generatesTheMapAndOpensTheDraft() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(waitingGame));
        when(playerRepository.countByGame_Id(1L)).thenReturn(2L);
        when(marshalService.ensureDefaultMarshals()).thenReturn(marshals(3));
        stubCityPersistence();
        when(gameRepository.save(waitingGame)).thenReturn(waitingGame);

        Game started = gameService.startGame(1L);

        assertEquals(GameStatus.MARSHAL_SELECTION, started.getStatus());
        assertEquals(0, started.getCurrentTurnNumber());
        verify(cityRepository, times(6)).save(any(City.class));
        ArgumentCaptor<List<MapEdge>> edges = ArgumentCaptor.forClass(List.class);
        verify(mapEdgeRepository).saveAll(edges.capture());
        assertTrue(edges.getValue().size() >= 6);
        verify(gameRepository).save(waitingGame);
        verify(marshalDraftService).prepareNextPlayer(1L);
    }

    @Test
    void tc08_startGame_twice_isRejected() {
        waitingGame.setStatus(GameStatus.MARSHAL_SELECTION);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(waitingGame));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> gameService.startGame(1L));

        assertEquals("Game has already started", exception.getMessage());
    }

    @Test
    void tc09_startGame_withoutEnoughMarshals_isRejected() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(waitingGame));
        when(playerRepository.countByGame_Id(1L)).thenReturn(2L);
        when(marshalService.ensureDefaultMarshals()).thenReturn(marshals(1));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> gameService.startGame(1L));

        assertEquals("Not enough marshal records are configured", exception.getMessage());
    }

    @Test
    void tc10_findByRoomCode_withAnUnknownCode_throwsResourceNotFoundException() {
        when(gameRepository.findByRoomCode("NOPE")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> gameService.findByRoomCode("NOPE"));

        assertEquals("Game not found with room code: NOPE", exception.getMessage());
    }

    @Test
    void tc11_findPlayers_returnsTheThreePlayersOfTheGame() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(waitingGame));
        when(playerRepository.findByGame_IdOrderById(1L)).thenReturn(List.of(
                Player.builder().id(1L).name("P1").game(waitingGame).build(),
                Player.builder().id(2L).name("P2").game(waitingGame).build(),
                Player.builder().id(3L).name("P3").game(waitingGame).build()));

        assertEquals(3, gameService.findPlayers(1L).size());
    }
}
