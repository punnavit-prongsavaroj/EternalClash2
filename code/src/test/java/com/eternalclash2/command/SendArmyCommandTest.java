package com.eternalclash2.command;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.GameEvent;
import com.eternalclash2.domain.entity.Marshal;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.ActionType;
import com.eternalclash2.domain.enums.EventType;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.repository.GameEventRepository;
import com.eternalclash2.service.ArmyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendArmyCommandTest {

    private static final String REQUIRED_TARGET_MESSAGE =
            "Target and soldier count are required to send an army";

    /** The project pins mock-maker-subclass, so the 20% roll cannot be forced with a static mock. */
    private static final int ATTEMPTS = 300;

    @Mock
    private ArmyService armyService;

    @Mock
    private GameEventRepository gameEventRepository;

    private final Game game = Game.builder().id(1L).build();

    private Player player(Long id, String name, Marshal marshal) {
        return Player.builder().id(id).name(name).game(game).marshal(marshal).build();
    }

    private City city(Long id, String name, Player owner) {
        return City.builder().id(id).name(name).player(owner).food(100).soldiers(50).build();
    }

    @Test
    void tc01_execute_sendsArmyThroughArmyService() {
        City sourceCity = city(10L, "City-10", player(100L, "Attacker", null));
        SendArmyCommand command = new SendArmyCommand(
                armyService, sourceCity, 20L, 20, 1, gameEventRepository, game);

        command.execute();

        verify(armyService).sendArmy(10L, 20L, 20, 1);
    }

    @Test
    void tc02_execute_withNullTargetCityId_throwsAndSendsNothing() {
        City sourceCity = city(10L, "City-10", player(100L, "Attacker", null));
        SendArmyCommand command = new SendArmyCommand(
                armyService, sourceCity, null, 20, 1, gameEventRepository, game);

        BusinessLogicException exception = assertThrows(BusinessLogicException.class, command::execute);

        assertEquals(REQUIRED_TARGET_MESSAGE, exception.getMessage());
        verifyNoInteractions(armyService, gameEventRepository);
    }

    @Test
    void tc03_execute_withNullSoldierCount_throwsSameException() {
        City sourceCity = city(10L, "City-10", player(100L, "Attacker", null));
        SendArmyCommand command = new SendArmyCommand(
                armyService, sourceCity, 20L, null, 1, gameEventRepository, game);

        BusinessLogicException exception = assertThrows(BusinessLogicException.class, command::execute);

        assertEquals(REQUIRED_TARGET_MESSAGE, exception.getMessage());
        verifyNoInteractions(armyService, gameEventRepository);
    }

    @Test
    void tc04_execute_whenStubbornSoldiersRebel_armyIsNotSentButActionIsConsumed() {
        Player owner = player(100L, "Attacker",
                Marshal.builder().specialAbilityType("SURVIVE_DESTRUCTION").build());
        City sourceCity = city(10L, "City-10", owner);
        Army created = Army.builder().id(11L).soldiers(20).build();
        when(armyService.sendArmy(eq(10L), eq(20L), eq(20), anyInt())).thenReturn(created);

        int rebellions = 0;
        for (int turn = 1; turn <= ATTEMPTS; turn++) {
            sourceCity.setActionUsedThisTurn(false);
            SendArmyCommand command = new SendArmyCommand(
                    armyService, sourceCity, 20L, 20, turn, gameEventRepository, game);

            command.execute();

            if (command.getRecordedAction() == ActionType.NONE) {
                rebellions++;
                assertTrue(sourceCity.getActionUsedThisTurn(),
                        "a failed send must still consume the turn action");
            } else {
                assertFalse(sourceCity.getActionUsedThisTurn());
                assertSame(created, command.getArmy());
            }
        }

        assertTrue(rebellions > 0 && rebellions < ATTEMPTS,
                "both the 20% rebellion branch and the normal branch must occur, rebellions=" + rebellions);
        verify(armyService, times(ATTEMPTS - rebellions)).sendArmy(eq(10L), eq(20L), eq(20), anyInt());

        ArgumentCaptor<GameEvent> saved = ArgumentCaptor.forClass(GameEvent.class);
        verify(gameEventRepository, times(rebellions)).save(saved.capture());
        for (GameEvent event : saved.getAllValues()) {
            assertEquals(EventType.REBELLION, event.getEventType());
            assertSame(owner, event.getAffectedPlayer());
        }
    }

    @Test
    void tc05_execute_whenMarshalRevealsTarget_declarationOfWarIsLogged() {
        Player owner = player(100L, "Attacker", Marshal.builder().revealsAttackTarget(true).build());
        City sourceCity = city(10L, "City-10", owner);
        Player defender = player(200L, "Defender", null);
        City targetCity = city(20L, "City-20", defender);
        Army army = Army.builder().id(11L).sourceCity(sourceCity).targetCity(targetCity).soldiers(20).build();
        SendArmyCommand command = new SendArmyCommand(
                armyService, sourceCity, 20L, 20, 1, gameEventRepository, game);
        when(armyService.sendArmy(10L, 20L, 20, 1)).thenReturn(army);

        command.execute();

        verify(armyService).sendArmy(10L, 20L, 20, 1);
        ArgumentCaptor<GameEvent> saved = ArgumentCaptor.forClass(GameEvent.class);
        verify(gameEventRepository).save(saved.capture());
        GameEvent event = saved.getValue();
        assertEquals(EventType.REBELLION, event.getEventType());
        assertSame(defender, event.getAffectedPlayer());
        assertTrue(event.getDescription().contains("City-20"));
    }

    @Test
    void tc06_execute_recordsSendArmyActionAndReturnsTheCreatedArmy() {
        City sourceCity = city(10L, "City-10", player(100L, "Attacker", null));
        Army created = Army.builder().id(11L).soldiers(20).build();
        SendArmyCommand command = new SendArmyCommand(
                armyService, sourceCity, 20L, 20, 1, gameEventRepository, game);
        when(armyService.sendArmy(10L, 20L, 20, 1)).thenReturn(created);

        command.execute();

        assertEquals(ActionType.SEND_ARMY, command.getRecordedAction());
        assertSame(created, command.getArmy());
        verifyNoInteractions(gameEventRepository);
    }
}
