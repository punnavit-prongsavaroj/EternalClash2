package com.eternalclash2.service;

import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.repository.GameRepository;
import com.eternalclash2.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private MarshalService marshalService;

    @Mock
    private com.eternalclash2.repository.CityRepository cityRepository;

    @Mock
    private com.eternalclash2.repository.MapEdgeRepository mapEdgeRepository;

    @Mock
    private com.eternalclash2.service.MarshalDraftService marshalDraftService;

    @InjectMocks
    private GameService gameService;

    @Test
    void createGame_Success() {
        Game game = Game.builder().id(1L).roomCode("TEST12").status(GameStatus.WAITING).build();
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        Game created = gameService.createGame();

        assertNotNull(created);
        assertEquals(GameStatus.WAITING, created.getStatus());
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void addPlayer_Success() {
        Game game = Game.builder().id(1L).status(GameStatus.WAITING).build();
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        
        Player savedPlayer = Player.builder().id(100L).name("Player1").game(game).build();
        when(playerRepository.save(any(Player.class))).thenReturn(savedPlayer);

        Player player = gameService.addPlayer(1L, "Player1");

        assertNotNull(player);
        assertEquals("Player1", player.getName());
        verify(playerRepository).save(argThat(p -> p.getPlayerStats() != null));
    }

    @Test
    void addPlayer_ThrowsWhenGameFull() {
        Game game = Game.builder().id(1L).status(GameStatus.WAITING).build();
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        
        // Mock 7 players already in game
        when(playerRepository.countByGame_Id(1L)).thenReturn(7L);

        BusinessLogicException ex = assertThrows(BusinessLogicException.class, () -> gameService.addPlayer(1L, "Player8"));
        assertEquals("A game can have at most 7 players", ex.getMessage());
    }

    @Test
    void startGame_Success() {
        Game game = Game.builder().id(1L).status(GameStatus.WAITING).build();
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.countByGame_Id(1L)).thenReturn(2L);
        when(marshalService.ensureDefaultMarshals()).thenReturn(List.of(
            com.eternalclash2.domain.entity.Marshal.builder().name("ขงเบ้ง").build(),
            com.eternalclash2.domain.entity.Marshal.builder().name("จูล่ง").build(),
            com.eternalclash2.domain.entity.Marshal.builder().name("จิวยี่").build()
        ));
        when(cityRepository.save(any(com.eternalclash2.domain.entity.City.class))).thenAnswer(i -> {
            com.eternalclash2.domain.entity.City c = i.getArgument(0);
            c.setId(System.nanoTime()); // unique id
            return c;
        });
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        Game started = gameService.startGame(1L);

        assertEquals(GameStatus.MARSHAL_SELECTION, started.getStatus());
        verify(gameRepository).save(game);
    }
}
