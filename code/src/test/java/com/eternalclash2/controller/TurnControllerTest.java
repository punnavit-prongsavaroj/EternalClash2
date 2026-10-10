package com.eternalclash2.controller;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.entity.TurnAction;
import com.eternalclash2.domain.enums.ActionType;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.dto.GameDto;
import com.eternalclash2.dto.TurnActionDto;
import com.eternalclash2.dto.TurnActionRequest;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurnControllerTest {

    @Mock
    private TurnService turnService;

    @Mock
    private TurnActionService turnActionService;

    @InjectMocks
    private TurnController turnController;

    private final Game game = Game.builder().id(1L).status(GameStatus.IN_PROGRESS).currentTurnNumber(3).build();
    private final Player player = Player.builder().id(100L).game(game).name("Player1").isAlive(true).build();

    private TurnAction action(Long id, ActionType actionType, Army army) {
        return TurnAction.builder().id(id).game(game).turnNumber(3).player(player)
                .city(City.builder().id(10L).player(player).build()).actionType(actionType)
                .foodBefore(50).foodAfter(25).soldiersBefore(100).soldiersAfter(80).army(army).build();
    }

    @Test
    void tc01_submitAction_returnsCreatedTurnAction() {
        when(turnActionService.performAction(1L, 100L, 10L, ActionType.PRODUCE_FOOD, null, null))
                .thenReturn(action(50L, ActionType.PRODUCE_FOOD, null));

        ResponseEntity<TurnActionDto> response = turnController.submitAction(1L, 100L,
                new TurnActionRequest(ActionType.PRODUCE_FOOD, 10L, null, null));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(50L, response.getBody().id());
    }

    @Test
    void tc02_submitAction_sendArmyCarriesTheArmyId() {
        Army army = Army.builder().id(11L).owner(player).soldiers(20).build();
        when(turnActionService.performAction(1L, 100L, 10L, ActionType.SEND_ARMY, 3L, 20))
                .thenReturn(action(51L, ActionType.SEND_ARMY, army));

        ResponseEntity<TurnActionDto> response = turnController.submitAction(1L, 100L,
                new TurnActionRequest(ActionType.SEND_ARMY, 10L, 3L, 20));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(11L, response.getBody().armyId());
    }

    @Test
    void tc03_resolveTurn_returnsTheAdvancedGame() {
        when(turnService.resolveAndAdvance(1L))
                .thenReturn(Game.builder().id(1L).status(GameStatus.IN_PROGRESS).currentTurnNumber(2).build());

        ResponseEntity<GameDto> response = turnController.resolveTurn(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().currentTurnNumber());
    }
}
