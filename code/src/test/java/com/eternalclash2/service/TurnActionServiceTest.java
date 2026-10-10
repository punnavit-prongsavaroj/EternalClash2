package com.eternalclash2.service;

import com.eternalclash2.command.CommandFactory;
import com.eternalclash2.command.PlayerActionCommand;
import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.entity.TurnAction;
import com.eternalclash2.domain.enums.ActionType;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.GameRepository;
import com.eternalclash2.repository.TurnActionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TurnActionServiceTest {

    @Mock
    private TurnActionRepository turnActionRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlayerService playerService;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CommandFactory commandFactory;

    @InjectMocks
    private TurnActionService turnActionService;

    @Test
    void performAction_ThrowsIfPlayerDead() {
        Game game = Game.builder().id(1L).status(GameStatus.IN_PROGRESS).build();
        Player player = Player.builder().id(100L).game(game).isAlive(false).build();
        
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        when(playerService.getAlivePlayerValidated(100L)).thenThrow(new BusinessLogicException("Eliminated players cannot take actions"));

        assertThrows(BusinessLogicException.class, () -> 
            turnActionService.performAction(1L, 100L, 10L, ActionType.PRODUCE_FOOD, null, null));
    }

    @Test
    void performAction_Success() {
        Game game = Game.builder().id(1L).status(GameStatus.IN_PROGRESS).build();
        Player player = Player.builder().id(100L).game(game).isAlive(true).build();
        City city = City.builder().id(10L).player(player).food(100).soldiers(50).build();
        
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        when(playerService.getAlivePlayerValidated(100L)).thenReturn(player);
        when(cityRepository.findById(10L)).thenReturn(Optional.of(city));
        
        PlayerActionCommand mockCommand = mock(PlayerActionCommand.class);
        when(commandFactory.createCommand(eq(ActionType.PRODUCE_FOOD), eq(10L), eq(city), any(), any(), anyInt(), eq(game)))
            .thenReturn(mockCommand);
            
        TurnAction saved = TurnAction.builder().id(500L).build();
        when(turnActionRepository.save(any(TurnAction.class))).thenReturn(saved);

        TurnAction result = turnActionService.performAction(1L, 100L, 10L, ActionType.PRODUCE_FOOD, null, null);

        assertNotNull(result);
        verify(mockCommand).execute();
        verify(turnActionRepository).save(any());
    }
}
