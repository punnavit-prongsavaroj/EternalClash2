package com.eternalclash2.service;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.MapEdge;
import com.eternalclash2.domain.entity.Marshal;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.ArmyStatus;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.ArmyRepository;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.MapEdgeRepository;
import com.eternalclash2.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArmyServiceTest {

    private static final String CITY_NOT_FOUND_3 = "City not found with id: 3";

    @Mock
    private ArmyRepository armyRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private CityService cityService;
    @Mock
    private MapEdgeRepository mapEdgeRepository;

    @InjectMocks
    private ArmyService armyService;

    /** Stands in for ArmyRepository.size(): every army handed to save() lands here. */
    private final List<Army> savedArmies = new ArrayList<>();

    private Game game;
    private Player player1;
    private Player player2;
    private City city1;
    private City city2;

    @BeforeEach
    void buildFixtures() {
        game = Game.builder().id(1L).status(GameStatus.IN_PROGRESS).build();
        player1 = Player.builder().id(1L).name("Player1").game(game).isAlive(true).build();
        player2 = Player.builder().id(2L).name("Player2").game(game).isAlive(true).build();
        city1 = City.builder().id(1L).game(game).player(player1).soldiers(50).food(100).build();
        city2 = City.builder().id(2L).game(game).player(player2).soldiers(30).food(40).build();
    }

    private void stubLookups() {
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city1));
        when(cityRepository.findById(2L)).thenReturn(Optional.of(city2));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
    }

    private void stubConnected() {
        when(mapEdgeRepository.findByGame_Id(1L)).thenReturn(
                List.of(MapEdge.builder().id(1L).game(game).city1(city1).city2(city2).build()));
    }

    private void stubSave() {
        when(armyRepository.save(any(Army.class))).thenAnswer(invocation -> {
            Army army = invocation.getArgument(0);
            savedArmies.add(army);
            return army;
        });
    }

    private void stubSendArmyPath() {
        stubLookups();
        stubConnected();
        stubSave();
    }

    private Army army(Long id, int soldiers, ArmyStatus status, int arrivalTurn) {
        return Army.builder().id(id).owner(player1).soldiers(soldiers).status(status)
                .departureTurn(1).arrivalTurn(arrivalTurn).build();
    }

    @Test
    void tc01_sendArmy_validRequest_savesOneArmy() {
        stubSendArmyPath();

        armyService.sendArmy(1L, 2L, 10, 3);

        assertEquals(1, savedArmies.size());
        Army saved = savedArmies.get(0);
        assertEquals(player1, saved.getOwner());
        assertEquals(city1, saved.getSourceCity());
        assertEquals(city2, saved.getTargetCity());
        assertEquals(ArmyStatus.TRAVELING, saved.getStatus());
    }

    @Test
    void tc02_sendArmy_targetCityNotFound_throwsResourceNotFoundException() {
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city1));
        when(cityRepository.findById(3L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> armyService.sendArmy(1L, 3L, 10, 3));

        assertTrue(exception.getMessage().contains(CITY_NOT_FOUND_3));
        assertEquals(0, savedArmies.size());
    }

    @Test
    void tc03_sendArmy_sourceCityNotFound_throwsResourceNotFoundException() {
        when(cityRepository.findById(3L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> armyService.sendArmy(3L, 1L, 10, 3));

        assertTrue(exception.getMessage().contains(CITY_NOT_FOUND_3));
        assertEquals(0, savedArmies.size());
    }

    @Test
    void tc04_sendArmy_zeroSoldiers_throwsBusinessLogicException() {
        stubLookups();

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> armyService.sendArmy(1L, 2L, 0, 3));

        assertEquals("Army must contain at least one soldier", exception.getMessage());
        assertEquals(0, savedArmies.size());
    }

    @Test
    void tc05_sendArmy_negativeSoldiers_throwsBusinessLogicException() {
        stubLookups();

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> armyService.sendArmy(1L, 2L, -10, 3));

        assertEquals("Army must contain at least one soldier", exception.getMessage());
        assertEquals(0, savedArmies.size());
    }

    @Test
    void tc06_sendArmy_moreSoldiersThanTheCityHolds_isRejected() {
        stubLookups();

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> armyService.sendArmy(1L, 2L, 51, 3));

        assertEquals("Not enough soldiers in the city", exception.getMessage());
    }

    @Test
    void tc07_sendArmy_exactlyAllCitySoldiers_isAccepted() {
        stubSendArmyPath();

        armyService.sendArmy(1L, 2L, 50, 3);

        assertEquals(1, savedArmies.size());
        assertEquals(0, city1.getSoldiers());
    }

    @Test
    void tc08_sendArmy_whenGameIsNotInProgress_isRejected() {
        game.setStatus(GameStatus.FINISHED);
        stubLookups();

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> armyService.sendArmy(1L, 2L, 10, 3));

        assertEquals("Game is not in progress", exception.getMessage());
    }

    @Test
    void tc09_sendArmy_toACityOfAnotherGame_isRejected() {
        Game otherGame = Game.builder().id(2L).status(GameStatus.IN_PROGRESS).build();
        City city3 = City.builder().id(3L).game(otherGame).player(player2).soldiers(30).build();
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city1));
        when(cityRepository.findById(3L)).thenReturn(Optional.of(city3));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> armyService.sendArmy(1L, 3L, 10, 3));

        assertEquals("Target must be in the same game", exception.getMessage());
    }

    @Test
    void tc10_sendArmy_toAnUnconnectedCity_isRejected() {
        stubLookups();
        when(mapEdgeRepository.findByGame_Id(1L)).thenReturn(List.of());

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> armyService.sendArmy(1L, 2L, 10, 3));

        assertEquals("You can only send an army to a directly connected city", exception.getMessage());
    }

    @Test
    void tc11_sendArmy_byAnEliminatedPlayer_isRejected() {
        Player eliminated = Player.builder().id(1L).name("Player1").game(game).isAlive(false).build();
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city1));
        when(cityRepository.findById(2L)).thenReturn(Optional.of(city2));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(eliminated));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> armyService.sendArmy(1L, 2L, 10, 3));

        assertEquals("Eliminated players cannot command armies", exception.getMessage());
    }

    @Test
    void tc12_sendArmy_inRainySeason_travelTakesOneExtraTurn() {
        stubSendArmyPath();

        armyService.sendArmy(1L, 2L, 10, 5);

        Army saved = savedArmies.get(0);
        assertEquals(4, saved.getArrivalTurn() - saved.getDepartureTurn());
    }

    @Test
    void tc13_sendArmy_withFasterTravelMarshal_travelTakesOneTurnLess() {
        Player faster = Player.builder().id(1L).name("Player1").game(game).isAlive(true)
                .marshal(Marshal.builder().specialAbilityType("FASTER_TRAVEL").build()).build();
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city1));
        when(cityRepository.findById(2L)).thenReturn(Optional.of(city2));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(faster));
        stubConnected();
        stubSave();

        armyService.sendArmy(1L, 2L, 10, 1);

        Army saved = savedArmies.get(0);
        assertEquals(2, saved.getArrivalTurn() - saved.getDepartureTurn());
    }

    @Test
    void tc14_cancelIfDestroyed_deadTravelingArmy_isSaved() {
        Army destroyed = army(91L, 0, ArmyStatus.TRAVELING, 4);

        armyService.cancelIfDestroyed(destroyed);

        verify(armyRepository, times(1)).save(destroyed);
    }

    @Test
    void tc15_cancelIfDestroyed_arrivedArmyIsLeftUntouched() {
        Army arrived = army(91L, 10, ArmyStatus.ARRIVED, 4);

        armyService.cancelIfDestroyed(arrived);

        verify(armyRepository, never()).save(arrived);
    }

    @Test
    void tc16_cancelIfDestroyed_armyThatStillHasSoldiersIsLeftUntouched() {
        Army marching = army(91L, 10, ArmyStatus.TRAVELING, 4);

        armyService.cancelIfDestroyed(marching);

        verify(armyRepository, never()).save(marching);
    }

    @Test
    void tc17_findDueArmies_returnsBothArmiesThatHaveArrivedByTurnThree() {
        when(armyRepository.findByTargetCity_Game_IdAndStatus(1L, ArmyStatus.TRAVELING))
                .thenReturn(List.of(army(91L, 10, ArmyStatus.TRAVELING, 2), army(92L, 20, ArmyStatus.TRAVELING, 2)));

        assertEquals(2, armyService.findDueArmies(1L, 3).size());
    }

    @Test
    void tc18_findDueArmies_excludesArmiesThatArriveLater() {
        when(armyRepository.findByTargetCity_Game_IdAndStatus(1L, ArmyStatus.TRAVELING))
                .thenReturn(List.of(army(91L, 10, ArmyStatus.TRAVELING, 2), army(92L, 20, ArmyStatus.TRAVELING, 3)));

        List<Army> due = armyService.findDueArmies(1L, 2);

        assertEquals(1, due.size());
        assertEquals(91L, due.get(0).getId());
    }

    @Test
    void tc19_findByOwner_returnsBothTravelingArmiesOfPlayerOne() {
        when(armyRepository.findByOwner_IdAndStatus(1L, ArmyStatus.TRAVELING))
                .thenReturn(List.of(army(91L, 10, ArmyStatus.TRAVELING, 2), army(92L, 20, ArmyStatus.TRAVELING, 3)));

        assertEquals(2, armyService.findByOwner(1L).size());
    }

    @Test
    void tc20_findByOwner_forPlayerTwoWithoutArmiesReturnsNothing() {
        when(armyRepository.findByOwner_IdAndStatus(2L, ArmyStatus.TRAVELING)).thenReturn(List.of());

        assertEquals(0, armyService.findByOwner(2L).size());
    }

    @Test
    void tc21_findById_returnsTheStoredArmy() {
        when(armyRepository.findById(1L)).thenReturn(Optional.of(army(1L, 10, ArmyStatus.TRAVELING, 2)));

        assertEquals(1L, armyService.findById(1L).getId());
    }

    @Test
    void tc22_findById_withUnknownId_throwsResourceNotFoundException() {
        when(armyRepository.findById(3L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> armyService.findById(3L));

        assertEquals("Army not found with id: 3", exception.getMessage());
    }

    @Test
    void tc23_sendArmy_chargesTwelveFoodForTenSoldiers() {
        stubSendArmyPath();

        armyService.sendArmy(1L, 2L, 10, 3);

        verify(cityService).deductFoodFromNetwork(city1, 12);
    }

    @Test
    void tc24_sendArmy_removesExactlyTheRequestedSoldiers() {
        stubSendArmyPath();

        armyService.sendArmy(1L, 2L, 10, 3);

        assertEquals(40, city1.getSoldiers());
    }

    @Test
    void tc25_sendArmy_marksTheSourceCityAsAlreadyActed() {
        stubSendArmyPath();

        armyService.sendArmy(1L, 2L, 10, 3);

        assertTrue(city1.getActionUsedThisTurn());
    }

    @Test
    void tc26_sendArmy_savesTheCityAndTheArmyOnceEach() {
        stubSendArmyPath();

        armyService.sendArmy(1L, 2L, 10, 3);

        verify(cityRepository, times(1)).save(city1);
        verify(armyRepository, times(1)).save(any(Army.class));
    }

    @Test
    void tc27_sendArmy_savesATravelingArmyWithDepartureAndArrivalTurns() {
        stubSendArmyPath();

        armyService.sendArmy(1L, 2L, 10, 3);

        ArgumentCaptor<Army> saved = ArgumentCaptor.forClass(Army.class);
        verify(armyRepository).save(saved.capture());
        assertEquals(ArmyStatus.TRAVELING, saved.getValue().getStatus());
        assertEquals(3, saved.getValue().getDepartureTurn());
        assertEquals(6, saved.getValue().getArrivalTurn());
    }

    @Test
    void tc28_cancelIfDestroyed_setsStatusCancelledAndZeroSoldiers() {
        Army destroyed = army(91L, 0, ArmyStatus.TRAVELING, 4);

        armyService.cancelIfDestroyed(destroyed);

        assertEquals(ArmyStatus.CANCELLED, destroyed.getStatus());
        assertEquals(0, destroyed.getSoldiers());
    }

    @Test
    void tc29_findDueArmies_withNoTravelingArmiesReturnsEmpty() {
        when(armyRepository.findByTargetCity_Game_IdAndStatus(1L, ArmyStatus.TRAVELING)).thenReturn(List.of());

        assertTrue(armyService.findDueArmies(1L, 5).isEmpty());
    }

    @Test
    void tc30_findById_onAnEmptyRepository_throwsResourceNotFoundException() {
        when(armyRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> armyService.findById(1L));

        assertEquals("Army not found with id: 1", exception.getMessage());
    }
}
