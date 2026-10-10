package com.eternalclash2.controller;

import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.service.GameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GameControllerTest {

    @Mock
    private GameService gameService;

    @InjectMocks
    private GameController gameController;

    @Test
    void createGame_ReturnsCreatedGame() {
        Game mockGame = Game.builder().id(1L).roomCode("TEST99").status(GameStatus.WAITING).build();
        when(gameService.createGame()).thenReturn(mockGame);

        ResponseEntity<com.eternalclash2.dto.GameDto> response = gameController.createGame();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("TEST99", response.getBody().roomCode());
    }

    @Test
    void startGame_ReturnsStartedGame() {
        Game mockGame = Game.builder().id(1L).status(GameStatus.MARSHAL_SELECTION).build();
        when(gameService.startGame(1L)).thenReturn(mockGame);

        ResponseEntity<com.eternalclash2.dto.GameDto> response = gameController.startGame(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(GameStatus.MARSHAL_SELECTION, response.getBody().status());
    }
}
