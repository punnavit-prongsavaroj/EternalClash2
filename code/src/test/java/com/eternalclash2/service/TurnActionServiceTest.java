package com.eternalclash2.service;

import com.eternalclash2.command.CommandFactory;
import com.eternalclash2.command.PlayerActionCommand;
import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.entity.TurnAction;
import com.eternalclash2.domain.enums.ActionType;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.GameEventRepository;
import com.eternalclash2.repository.GameRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurnActionServiceTest {

    @Mock
    private TurnActionRepository turnActionRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private PlayerService playerService;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private CityService cityService;
    @Mock
    private ArmyService armyService;
    @Mock
    private GameEventRepository gameEventRepository;
    @Mock
    private CommandFactory commandFactory;

    @InjectMocks
    private TurnActionService turnActionService;

    private Game game;
    private Player player;
    private City city;

    @BeforeEach
    void buildFixtures() {
        game = Game.builder().id(1L).status(GameStatus.IN_PROGRESS).currentTurnNumber(1).build();
        player = Player.builder().id(100L).name("Player1").game(game).isAlive(true).build();
        city = City.builder().id(10L).game(game).player(player).food(50).soldiers(20).build();
    }

    /** TC09/TC10 have to see which concrete command the real factory picks, so they get the unmocked one. */
    private TurnActionService withRealCommandFactory() {
        return new TurnActionService(turnActionRepository, gameRepository, playerService, cityRepository,
                cityService, armyService, gameEventRepository,
                new CommandFactory(cityService, armyService, gameEventRepository));
    }

    /** The four checks every accepted action passes before its command is built. */
    private void stubAcceptedSubmission() {
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        when(playerService.getAlivePlayerValidated(100L)).thenReturn(player);
        when(turnActionRepository.existsByGame_IdAndTurnNumberAndCity_Id(1L, 1, 10L)).thenReturn(false);
        when(cityRepository.findById(10L)).thenReturn(Optional.of(city));
    }

    @Test
    void tc01_performAction_byAnEliminatedPlayer_isRejected() {
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        when(playerService.getAlivePlayerValidated(100L))
                .thenThrow(new BusinessLogicException("Eliminated players cannot take actions"));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> turnActionService.performAction(1L, 100L, 10L, ActionType.PRODUCE_FOOD, null, null));

        assertEquals("Eliminated players cannot take actions", exception.getMessage());
    }

    @Test
    void tc02_performAction_recordsFoodAndSoldiersAroundTheCommand() {
        stubAcceptedSubmission();
        PlayerActionCommand command = mock(PlayerActionCommand.class);
        when(commandFactory.createCommand(ActionType.PRODUCE_FOOD, 10L, city, null, null, 1, game)).thenReturn(command);
        when(command.getRecordedAction()).thenReturn(ActionType.PRODUCE_FOOD);
        doAnswer(invocation -> {
            city.setFood(70);
            return null;
        }).when(command).execute();
        when(turnActionRepository.save(any(TurnAction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TurnAction result = turnActionService.performAction(1L, 100L, 10L, ActionType.PRODUCE_FOOD, null, null);

        assertNotNull(result);
        verify(command).execute();
        assertEquals(50, result.getFoodBefore());
        assertEquals(70, result.getFoodAfter());
        assertEquals(20, result.getSoldiersBefore());
        assertEquals(20, result.getSoldiersAfter());
    }

    @Test
    void tc03_performAction_withoutAnActionType_isRejected() {
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        when(playerService.getAlivePlayerValidated(100L)).thenReturn(player);
        when(turnActionRepository.existsByGame_IdAndTurnNumberAndCity_Id(1L, 1, 10L)).thenReturn(false);

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> turnActionService.performAction(1L, 100L, 10L, null, null, null));

        assertEquals("Action type is required", exception.getMessage());
    }

    @Test
    void tc04_performAction_twiceForOneCityInOneTurn_returnsTheFirstAction() {
        TurnAction existing = TurnAction.builder().id(500L).game(game).turnNumber(1).player(player).city(city)
                .actionType(ActionType.PRODUCE_FOOD).build();
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        when(playerService.getAlivePlayerValidated(100L)).thenReturn(player);
        when(turnActionRepository.existsByGame_IdAndTurnNumberAndCity_Id(1L, 1, 10L)).thenReturn(true);
        when(turnActionRepository.findByGame_IdAndTurnNumber(1L, 1)).thenReturn(List.of(existing));

        TurnAction result = turnActionService.performAction(1L, 100L, 10L, ActionType.PRODUCE_FOOD, null, null);

        assertSame(existing, result);
        verify(commandFactory, never()).createCommand(any(), any(), any(), any(), any(), anyInt(), any());
        verify(turnActionRepository, never()).save(any());
    }

    @Test
    void tc05_performAction_onSomebodyElsesCity_isRejected() {
        Player other = Player.builder().id(200L).name("Player2").game(game).isAlive(true).build();
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        when(playerService.getAlivePlayerValidated(100L)).thenReturn(player);
        when(turnActionRepository.existsByGame_IdAndTurnNumberAndCity_Id(1L, 1, 10L)).thenReturn(false);
        when(cityRepository.findById(10L)).thenReturn(Optional.of(
                City.builder().id(10L).game(game).player(other).food(50).soldiers(20).build()));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> turnActionService.performAction(1L, 100L, 10L, ActionType.PRODUCE_FOOD, null, null));

        assertEquals("You do not own this city", exception.getMessage());
    }

    @Test
    void tc06_performAction_withAPlayerFromAnotherGame_isRejected() {
        Game otherGame = Game.builder().id(2L).status(GameStatus.IN_PROGRESS).currentTurnNumber(1).build();
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        when(playerService.getAlivePlayerValidated(100L))
                .thenReturn(Player.builder().id(100L).name("Player1").game(otherGame).isAlive(true).build());

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> turnActionService.performAction(1L, 100L, 10L, ActionType.PRODUCE_FOOD, null, null));

        assertEquals("Player does not belong to this game", exception.getMessage());
    }

    @Test
    void tc07_performAction_withUnknownGame_throwsResourceNotFoundException() {
        when(gameRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> turnActionService.performAction(99L, 100L, 10L, ActionType.PRODUCE_FOOD, null, null));

        assertEquals("Game not found with id: 99", exception.getMessage());
    }

    @Test
    void tc08_performAction_withUnknownCity_throwsResourceNotFoundException() {
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        when(playerService.getAlivePlayerValidated(100L)).thenReturn(player);
        when(turnActionRepository.existsByGame_IdAndTurnNumberAndCity_Id(1L, 1, 99L)).thenReturn(false);
        when(cityRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> turnActionService.performAction(1L, 100L, 99L, ActionType.PRODUCE_FOOD, null, null));

        assertEquals("City not found: 99", exception.getMessage());
    }

    @Test
    void tc09_performAction_forSendArmy_recordsTheArmyThatWasSent() {
        City target = City.builder().id(11L).game(game).soldiers(30).food(40).build();
        Army army = Army.builder().id(900L).owner(player).sourceCity(city).targetCity(target)
                .soldiers(10).departureTurn(1).arrivalTurn(4).build();
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(game));
        when(playerService.getAlivePlayerValidated(100L)).thenReturn(player);
        when(turnActionRepository.existsByGame_IdAndTurnNumberAndCity_Id(1L, 1, 10L)).thenReturn(false);
        when(cityRepository.findById(10L)).thenReturn(Optional.of(city));
        when(armyService.sendArmy(10L, 11L, 10, 1)).thenReturn(army);
        when(turnActionRepository.save(any(TurnAction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TurnAction result = withRealCommandFactory()
                .performAction(1L, 100L, 10L, ActionType.SEND_ARMY, 11L, 10);

        assertEquals(ActionType.SEND_ARMY, result.getActionType());
        assertSame(army, result.getArmy());
    }

    @Test
    void tc10_performAction_forNone_recordsNoArmy() {
        stubAcceptedSubmission();
        when(turnActionRepository.save(any(TurnAction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TurnAction result = withRealCommandFactory().performAction(1L, 100L, 10L, ActionType.NONE, null, null);

        assertEquals(ActionType.NONE, result.getActionType());
        assertNull(result.getArmy());
        verify(armyService, never()).sendArmy(anyLong(), anyLong(), anyInt(), anyInt());
    }

    @Test
    void tc11_performAction_beforeThePlayPhase_isRejectedByTheStateMachine() {
        Game waiting = Game.builder().id(1L).status(GameStatus.WAITING).currentTurnNumber(0).build();
        when(gameRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(waiting));
        when(playerService.getAlivePlayerValidated(100L)).thenReturn(player);

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> turnActionService.performAction(1L, 100L, 10L, ActionType.PRODUCE_FOOD, null, null));

        assertEquals("ไม่สามารถทำแอคชันได้ เกมกำลังรอผู้เล่นอื่น", exception.getMessage());
    }

    @Test
    void tc12_findTurnActions_returnsTheTwoActionsOfTurnThree() {
        when(turnActionRepository.findByGame_IdAndTurnNumber(1L, 3)).thenReturn(List.of(
                TurnAction.builder().id(1L).game(game).turnNumber(3).build(),
                TurnAction.builder().id(2L).game(game).turnNumber(3).build()));

        assertEquals(2, turnActionService.findTurnActions(1L, 3).size());
    }
}
