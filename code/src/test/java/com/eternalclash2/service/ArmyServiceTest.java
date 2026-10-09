package com.eternalclash2.service;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.MapEdge;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArmyServiceTest {

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

    // This list is OUR OWN substitute for "ArmyRepository.size()".
    // Every time the mock's save(...) is called, we push the army into here,
    // so we can later check savedArmies.size() instead of a real DB.
    private final List<Army> savedArmies = new ArrayList<>();

    private Game game;
    private Player player1;
    private Player player2;
    private City city1;
    private City city2;

    @BeforeEach
    void setUp() {
        game = Game.builder().id(1L).status(GameStatus.IN_PROGRESS).build();

        player1 = Player.builder().id(1L).name("Player1").game(game).isAlive(true).build();
        player2 = Player.builder().id(2L).name("Player2").game(game).isAlive(true).build();

        city1 = City.builder().id(1L).player(player1).game(game).soldiers(50).build();
        city2 = City.builder().id(2L).player(player2).game(game).soldiers(50).build();

        // Requirement table:
        // Mock PlayerRepository = [Player(ID1), Player(ID2)]
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.findById(2L)).thenReturn(Optional.of(player2));

        // Mock CityRepository = [City(ID1, Player1), City(ID2, Player2)]
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city1));
        when(cityRepository.findById(2L)).thenReturn(Optional.of(city2));
        // id 3L is NOT in the mock's "list" -> Mockito returns null by default
        // for an unstubbed call, but our service calls .orElseThrow() on the
        // Optional, so we must explicitly stub it to return Optional.empty()
        // to simulate "not found".
        when(cityRepository.findById(3L)).thenReturn(Optional.empty());

        // city1 and city2 need to be directly connected on the map,
        // otherwise sendArmy throws "not directly connected" before we
        // even get to the cases this suite is testing.
        MapEdge edge = MapEdge.builder().id(1L).game(game).city1(city1).city2(city2).build();
        when(mapEdgeRepository.findByGame_Id(1L)).thenReturn(List.of(edge));

        // Capture whatever gets passed to armyRepository.save(...) into our own list,
        // and just return it back (like a real save() would).
        when(armyRepository.save(any(Army.class))).thenAnswer(invocation -> {
            Army army = invocation.getArgument(0);
            savedArmies.add(army);
            return army;
        });

        // cityService.deductFoodFromNetwork(...) returns void, so Mockito
        // already does nothing by default - no stub needed, but we could write:
        // doNothing().when(cityService).deductFoodFromNetwork(any(), anyInt());
    }

    // TC01: sendArmy(1L, 2L, 10, 3) -> ArmyRepository.size() == 1
    @Test
    void tc01_sendArmy_validRequest_savesOneArmy() {
        armyService.sendArmy(1L, 2L, 10, 3);

        assertEquals(1, savedArmies.size());
        Army saved = savedArmies.get(0);
        assertEquals(player1, saved.getOwner());
        assertEquals(city1, saved.getSourceCity());
        assertEquals(city2, saved.getTargetCity());
        assertEquals(ArmyStatus.TRAVELING, saved.getStatus());
    }

    // TC02: sendArmy(1L, 3L, 10, 3) -> target city 3L doesn't exist -> CityNotFound
    @Test
    void tc02_sendArmy_targetCityNotFound_throwsResourceNotFoundException() {
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> armyService.sendArmy(1L, 3L, 10, 3));

        assertTrue(ex.getMessage().contains("City not found with id: 3"));
        assertEquals(0, savedArmies.size());
    }

    // TC03: sendArmy(3L, 1L, 10, 3) -> source city 3L doesn't exist -> CityNotFound
    @Test
    void tc03_sendArmy_sourceCityNotFound_throwsResourceNotFoundException() {
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> armyService.sendArmy(3L, 1L, 10, 3));

        assertTrue(ex.getMessage().contains("City not found with id: 3"));
        assertEquals(0, savedArmies.size());
    }

    // TC04: sendArmy(1L, 2L, 0, 3) -> "Army must contain at least one soldier"
    @Test
    void tc04_sendArmy_zeroSoldiers_throwsBusinessLogicException() {
        BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                () -> armyService.sendArmy(1L, 2L, 0, 3));

        assertEquals("Army must contain at least one soldier", ex.getMessage());
        assertEquals(0, savedArmies.size());
    }

    // TC05: sendArmy(1L, 2L, -10, 3) -> "Army must contain at least one soldier"
    @Test
    void tc05_sendArmy_negativeSoldiers_throwsBusinessLogicException() {
        BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                () -> armyService.sendArmy(1L, 2L, -10, 3));

        assertEquals("Army must contain at least one soldier", ex.getMessage());
        assertEquals(0, savedArmies.size());
    }

     @Test
    void cancelIfDestroyed_zeroSoldiersAndTraveling_cancelsAndSaves() {
        Army army = Army.builder()
                .soldiers(0)
                .status(ArmyStatus.TRAVELING)
                .build();

        armyService.cancelIfDestroyed(army);

        assertEquals(ArmyStatus.CANCELLED, army.getStatus());   // เช็คค่า (state)
        verify(armyRepository, times(1)).save(army);             // เช็คว่า save ถูกเรียก (behavior)
    }

    @Test
    void cancelIfDestroyed_stillHasSoldiers_doesNothing() {
        Army army = Army.builder()
                .soldiers(50)
                .status(ArmyStatus.TRAVELING)
                .build();

        armyService.cancelIfDestroyed(army);

        assertEquals(ArmyStatus.TRAVELING, army.getStatus());    // status ไม่ควรเปลี่ยน
        verify(armyRepository, never()).save(any());              // ไม่ควรมีการ save เกิดขึ้นเลย
    }
}