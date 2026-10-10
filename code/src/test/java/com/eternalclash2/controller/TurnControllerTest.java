package com.eternalclash2.controller;

import com.eternalclash2.dto.TurnActionDto;
import com.eternalclash2.dto.TurnActionRequest;
import com.eternalclash2.domain.entity.TurnAction;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.ActionType;
import com.eternalclash2.service.TurnActionService;
import com.eternalclash2.service.TurnService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TurnControllerTest {

    @Mock
    private TurnActionService turnActionService;

    @Mock
    private TurnService turnService;

    @InjectMocks
    private TurnController turnController;

    @Test
    void submitAction_ReturnsCreated() {
        TurnActionRequest request = new TurnActionRequest(ActionType.PRODUCE_FOOD, 10L, null, null);

        Game game = Game.builder().id(1L).build();
        Player player = Player.builder().id(100L).name("P1").build();
        TurnAction mockAction = TurnAction.builder().id(50L).game(game).player(player).actionType(ActionType.PRODUCE_FOOD).build();
        when(turnActionService.performAction(eq(1L), eq(100L), eq(10L), eq(ActionType.PRODUCE_FOOD), any(), any()))
            .thenReturn(mockAction);

        ResponseEntity<TurnActionDto> response = turnController.submitAction(1L, 100L, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(50L, response.getBody().id());
    }
}
