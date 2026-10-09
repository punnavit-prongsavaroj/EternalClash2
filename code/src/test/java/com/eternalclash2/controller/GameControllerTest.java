package com.eternalclash2.controller;

import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.dto.GameDto;
import com.eternalclash2.dto.PlayerDto;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.service.GameClock;
import com.eternalclash2.service.GameService;
import com.eternalclash2.service.GameViewService;
import com.eternalclash2.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {

    @Mock
    private GameService gameService;

    @Mock
    private PlayerService playerService;

    @Mock
    private GameViewService gameViewService;

    @InjectMocks
    private GameController gameController;

    private Game game(Long id, String roomCode, GameStatus status, int turn) {
        return Game.builder().id(id).roomCode(roomCode).status(status).currentTurnNumber(turn).build();
    }

    private Player player(Long id, Game game, String name) {
        return Player.builder().id(id).game(game).name(name).isAlive(true).build();
    }

    @Test
    void tc01_createGame_returnsCreatedWithRoomCode() {
        when(gameService.createGame()).thenReturn(game(1L, "TEST99", GameStatus.WAITING, 0));

        ResponseEntity<GameDto> response = gameController.createGame();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("TEST99", response.getBody().roomCode());
    }

    @Test
    void tc02_startGame_returnsMarshalSelectionStatus() {
        when(gameService.startGame(1L)).thenReturn(game(1L, "TEST99", GameStatus.MARSHAL_SELECTION, 0));

        ResponseEntity<GameDto> response = gameController.startGame(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(GameStatus.MARSHAL_SELECTION, response.getBody().status());
    }

    @Test
    void tc03_getGame_returnsTheRequestedGame() {
        when(gameService.findById(1L)).thenReturn(game(1L, "TEST99", GameStatus.IN_PROGRESS, 3));

        ResponseEntity<GameDto> response = gameController.getGame(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().id());
        assertEquals(GameStatus.IN_PROGRESS, response.getBody().status());
    }

    @Test
    void tc04_getGameByCode_returnsTheMatchingGame() {
        when(gameService.findByRoomCode("ABC123")).thenReturn(game(1L, "ABC123", GameStatus.WAITING, 0));

        ResponseEntity<GameDto> response = gameController.getGameByCode("ABC123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ABC123", response.getBody().roomCode());
    }

    @Test
    void tc05_getGameByCode_withUnknownCodePropagatesResourceNotFoundException() {
        when(gameService.findByRoomCode("NOPE"))
                .thenThrow(new ResourceNotFoundException("Game not found with code: NOPE"));

        assertThrows(ResourceNotFoundException.class, () -> gameController.getGameByCode("NOPE"));
    }

    @Test
    void tc06_addPlayer_returnsCreatedPlayerDto() {
        Game game = game(1L, "TEST99", GameStatus.WAITING, 0);
        when(gameService.addPlayer(1L, "Player1")).thenReturn(player(100L, game, "Player1"));

        ResponseEntity<PlayerDto> response = gameController.addPlayer(1L,
                new com.eternalclash2.dto.AddPlayerRequest("Player1"));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(100L, response.getBody().id());
    }

    @Test
    void tc07_getPlayers_returnsEveryPlayerOfTheGame() {
        Game game = game(1L, "TEST99", GameStatus.IN_PROGRESS, 3);
        when(playerService.findByGame(1L)).thenReturn(List.of(
                player(100L, game, "Player1"), player(101L, game, "Player2")));

        ResponseEntity<List<PlayerDto>> response = gameController.getPlayers(1L, false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void tc08_getAllGames_returnsAPagedPageOfTen() {
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("createdAt"));
        List<Game> games = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> game((long) i, "ROOM" + i, GameStatus.WAITING, 0))
                .toList();
        Page<Game> page = new PageImpl<>(games, pageable, games.size());
        when(gameService.findAll(pageable)).thenReturn(page);

        ResponseEntity<Page<GameDto>> response = gameController.getAllGames(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10, response.getBody().getContent().size());
        assertEquals(10, response.getBody().getPageable().getPageSize());
    }

    @Test
    void tc09_getSnapshot_returnsCitiesEdgesAndPlayers() {
        GameViewService.GameSnapshot snapshot = new GameViewService.GameSnapshot(
                1L, GameStatus.IN_PROGRESS, 3, GameClock.season(3), true,
                List.of(new GameViewService.PlayerSnapshot(100L, "Player1", true, null, true)),
                List.of(new GameViewService.NodeSnapshot(10L, "City-10", 1.0, 2.0, 100L, 50, 30, false)),
                List.of(new GameViewService.EdgeSnapshot(1L, 10L, 20L)),
                List.of(), List.of());
        when(gameViewService.getSnapshot(1L, 100L)).thenReturn(snapshot);

        ResponseEntity<GameViewService.GameSnapshot> response = gameController.getSnapshot(1L, 100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().nodes().size());
        assertEquals(1, response.getBody().edges().size());
        assertEquals(1, response.getBody().players().size());
    }
}
