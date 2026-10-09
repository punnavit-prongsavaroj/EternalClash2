package com.eternalclash2.service;

import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.GameEvent;
import com.eternalclash2.domain.entity.MapEdge;
import com.eternalclash2.domain.entity.Marshal;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.entity.PlayerStats;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.GameEventRepository;
import com.eternalclash2.repository.MapEdgeRepository;
import com.eternalclash2.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private PlayerService playerService;
    @Mock
    private MapEdgeRepository mapEdgeRepository;
    @Mock
    private GameEventRepository gameEventRepository;

    @InjectMocks
    private CityService cityService;

    private Game game;
    private Player player;

    @BeforeEach
    void buildFixtures() {
        game = Game.builder().id(1L).status(GameStatus.IN_PROGRESS).currentTurnNumber(1).build();
        player = Player.builder().id(100L).name("Player1").game(game).isAlive(true).build();
        player.setPlayerStats(PlayerStats.builder().player(player).build());
    }

    private City city(Long id, int food, int soldiers) {
        return City.builder().id(id).game(game).player(player).food(food).soldiers(soldiers).build();
    }

    private City cityOf(Long id, Player owner, int food, int soldiers) {
        return City.builder().id(id).game(game).player(owner).food(food).soldiers(soldiers).build();
    }

    private MapEdge edge(Long id, City one, City two) {
        return MapEdge.builder().id(id).game(game).city1(one).city2(two).build();
    }

    private void givePlayerProduction(int foodProduction, int soldierProduction) {
        player.setMarshal(Marshal.builder().id(1L).name("Marshal").foodProduction(foodProduction)
                .soldierProduction(soldierProduction).build());
    }

    private void stubCityAndOwner(City city) {
        when(cityRepository.findById(city.getId())).thenReturn(Optional.of(city));
        when(playerService.getAlivePlayerValidated(100L)).thenReturn(player);
    }

    private void stubCitySave() {
        when(cityRepository.save(any(City.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void stubNetwork(MapEdge... edges) {
        when(mapEdgeRepository.findByGame_Id(1L)).thenReturn(List.of(edges));
    }

    @Test
    void tc01_produceFood_addsTheMarshalsFoodProduction() {
        City source = city(10L, 100, 20);
        givePlayerProduction(20, 20);
        stubCityAndOwner(source);
        stubCitySave();

        City result = cityService.produceFood(10L, 1);

        assertEquals(120, result.getFood());
        assertTrue(result.getActionUsedThisTurn());
        assertEquals(20, player.getPlayerStats().getTotalFoodProduced());
    }

    @Test
    void tc02_produceFood_withUnknownCity_throwsResourceNotFoundException() {
        when(cityRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cityService.produceFood(1L, 1));

        assertEquals("City not found: 1", exception.getMessage());
    }

    @Test
    void tc03_produceFood_inWinter_halvesTheProduction() {
        City source = city(10L, 100, 20);
        givePlayerProduction(20, 20);
        stubCityAndOwner(source);
        stubCitySave();

        City result = cityService.produceFood(10L, 10);

        assertEquals(110, result.getFood());
        assertTrue(result.getActionUsedThisTurn());
        assertEquals(10, player.getPlayerStats().getTotalFoodProduced());
    }

    @Test
    void tc04_recruitSoldiers_inSummer_paysTwentyFoodForFifteenSoldiers() {
        City source = city(10L, 100, 30);
        givePlayerProduction(20, 20);
        stubCityAndOwner(source);
        stubCitySave();
        stubNetwork();

        City result = cityService.recruitSoldiers(10L, 1);

        assertEquals(45, result.getSoldiers());
        assertEquals(80, result.getFood());
        assertTrue(result.getActionUsedThisTurn());
        assertEquals(15, player.getPlayerStats().getTotalSoldiersRecruited());
    }

    @Test
    void tc05_recruitSoldiers_withoutEnoughFoodInNetwork_isRejected() {
        City source = city(10L, 10, 30);
        givePlayerProduction(20, 20);
        stubCityAndOwner(source);
        stubNetwork();

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> cityService.recruitSoldiers(10L, 1));

        assertTrue(exception.getMessage().startsWith("Not enough food in the connected network"));
    }

    @Test
    void tc06_recruitSoldiers_inRainySeason_getsTheFullProduction() {
        City source = city(10L, 100, 40);
        givePlayerProduction(20, 20);
        stubCityAndOwner(source);
        stubCitySave();
        stubNetwork();

        City result = cityService.recruitSoldiers(10L, 6);

        assertEquals(60, result.getSoldiers());
        assertEquals(80, result.getFood());
        assertTrue(result.getActionUsedThisTurn());
        assertEquals(20, player.getPlayerStats().getTotalSoldiersRecruited());
    }

    @Test
    void tc07_getConnectedNetwork_returnsTheSourceAndTheCityItIsJoinedTo() {
        City first = city(1L, 100, 10);
        City second = city(2L, 100, 10);
        stubNetwork(edge(1L, first, second));

        assertEquals(2, cityService.getConnectedNetwork(first).size());
    }

    @Test
    void tc08_getConnectedNetwork_walksThroughAnIntermediaryCity() {
        City first = city(1L, 100, 10);
        City second = city(2L, 100, 10);
        City fourth = city(4L, 100, 10);
        stubNetwork(edge(1L, first, second), edge(2L, second, fourth));

        assertEquals(3, cityService.getConnectedNetwork(first).size());
    }

    @Test
    void tc09_findById_returnsTheStoredCity() {
        City stored = city(1L, 100, 10);
        when(cityRepository.findById(1L)).thenReturn(Optional.of(stored));

        assertEquals(1L, cityService.findById(1L).getId());
    }

    @Test
    void tc10_findById_withUnknownId_throwsResourceNotFoundException() {
        when(cityRepository.findById(2L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cityService.findById(2L));

        assertEquals("City not found with id: 2", exception.getMessage());
    }

    @Test
    void tc11_save_delegatesToTheRepository() {
        City stored = city(1L, 100, 10);

        cityService.save(stored);

        verify(cityRepository).save(stored);
    }

    @Test
    void tc12_deductFoodFromNetwork_splitsTheCostEvenlyAcrossTwoCities() {
        City first = city(1L, 100, 10);
        City second = city(2L, 100, 10);
        stubNetwork(edge(1L, first, second));

        cityService.deductFoodFromNetwork(first, 10);

        assertEquals(95, first.getFood());
        assertEquals(95, second.getFood());
    }

    @Test
    void tc13_deductFoodFromNetwork_chargesTheRoundingRemainderToTheSourceCity() {
        City first = city(1L, 100, 10);
        City second = city(2L, 100, 10);
        City third = city(3L, 100, 10);
        stubNetwork(edge(1L, first, second), edge(2L, first, third));

        cityService.deductFoodFromNetwork(first, 10);

        assertEquals(96, first.getFood());
        assertEquals(97, second.getFood());
        assertEquals(97, third.getFood());
    }

    @Test
    void tc14_applySeasonUpkeep_inAWellFedNetwork_splitsTheUpkeepEvenly() {
        City first = city(1L, 100, 25);
        City third = city(3L, 100, 25);
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of(first, third));
        stubNetwork(edge(1L, first, third));

        cityService.applySeasonUpkeep(1L);

        assertEquals(75, first.getFood());
        assertEquals(75, third.getFood());
        verify(gameEventRepository, never()).save(any());
    }

    @Test
    void tc15_applySeasonUpkeep_letsTheTriggeringCityAbsorbTheRemainder() {
        City first = city(1L, 50, 50);
        City third = city(3L, 100, 25);
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of(first, third));
        stubNetwork(edge(1L, first, third));

        cityService.applySeasonUpkeep(1L);

        assertEquals(12, first.getFood());
        assertEquals(63, third.getFood());
        verify(gameEventRepository, never()).save(any());
    }

    @Test
    void tc16_applySeasonUpkeep_skipsNeutralCities() {
        City neutral = City.builder().id(1L).game(game).food(50).soldiers(10).build();
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of(neutral));

        cityService.applySeasonUpkeep(1L);

        assertEquals(50, neutral.getFood());
        assertEquals(10, neutral.getSoldiers());
        verify(gameEventRepository, never()).save(any());
    }

    @Test
    void tc17_applySeasonUpkeep_processesEachNetworkOnlyOnce() {
        City a = city(1L, 20, 10);
        City b = city(2L, 0, 0);
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of(a, b));
        stubNetwork(edge(1L, a, b));

        cityService.applySeasonUpkeep(1L);

        assertEquals(10, a.getFood());
        assertEquals(0, b.getFood());
    }

    @Test
    void tc18_applySeasonUpkeep_consumesExactlyTheSoldiersOfASingleCity() {
        City only = city(1L, 20, 20);
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of(only));
        stubNetwork();

        cityService.applySeasonUpkeep(1L);

        assertEquals(0, only.getFood());
        assertEquals(20, only.getSoldiers());
        verify(gameEventRepository, never()).save(any());
    }

    @Test
    void tc19_applySeasonUpkeep_addsTheRemainderToTheTriggeringCity() {
        City first = city(1L, 50, 15);
        City second = city(2L, 50, 14);
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of(first, second));
        stubNetwork(edge(1L, first, second));

        cityService.applySeasonUpkeep(1L);

        assertEquals(35, first.getFood());
        assertEquals(36, second.getFood());
    }

    @Test
    void tc20_applySeasonUpkeep_movesAShortfallOntoTheSolventCity() {
        City first = city(1L, 2, 8);
        City second = city(2L, 28, 8);
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of(first, second));
        stubNetwork(edge(1L, first, second));

        cityService.applySeasonUpkeep(1L);

        assertEquals(0, first.getFood());
        assertEquals(14, second.getFood());
    }

    @Test
    void tc21_applySeasonUpkeep_treatsTwoUnconnectedNetworksIndependently() {
        Player other = Player.builder().id(200L).name("Player2").game(game).isAlive(true).build();
        City a = cityOf(1L, player, 100, 50);
        City b = cityOf(2L, other, 10, 50);
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of(a, b));
        stubNetwork();

        cityService.applySeasonUpkeep(1L);

        assertEquals(50, a.getFood());
        assertEquals(50, a.getSoldiers());
        assertEquals(10, b.getSoldiers());
        ArgumentCaptor<GameEvent> event = ArgumentCaptor.forClass(GameEvent.class);
        verify(gameEventRepository, times(1)).save(event.capture());
        assertEquals(other, event.getValue().getAffectedPlayer());
        assertEquals(-40, event.getValue().getSoldierImpact());
    }

    @Test
    void tc22_applySeasonUpkeep_paysADeficitCityWithTheNetworkExcess() {
        City first = city(1L, 12, 10);
        City second = city(2L, 3, 15);
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of(first, second));
        stubNetwork(edge(1L, first, second));

        cityService.applySeasonUpkeep(1L);

        assertEquals(0, first.getFood());
        assertEquals(0, second.getFood());
        assertEquals(5, second.getSoldiers());
        ArgumentCaptor<GameEvent> event = ArgumentCaptor.forClass(GameEvent.class);
        verify(gameEventRepository, times(1)).save(event.capture());
        assertEquals(-10, event.getValue().getSoldierImpact());
    }

    @Test
    void tc23_applySeasonUpkeep_withNoExcess_LeavesNothingToShareBetweenDeficitCities() {
        City first = city(1L, 10, 10);
        City second = city(2L, 5, 15);
        City third = city(3L, 0, 15);
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of(first, second, third));
        stubNetwork(edge(1L, first, second), edge(2L, second, third));

        cityService.applySeasonUpkeep(1L);

        assertEquals(10, first.getSoldiers());
        assertEquals(5, second.getSoldiers());
        assertEquals(0, third.getSoldiers());
        verify(gameEventRepository, times(2)).save(any());
    }

    @Test
    void tc24_applySeasonUpkeep_withZeroExcess_everyDeficitCityLosesItsFullShortfall() {
        City first = city(1L, 0, 10);
        City second = city(2L, 0, 8);
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of(first, second));
        stubNetwork(edge(1L, first, second));

        cityService.applySeasonUpkeep(1L);

        assertEquals(0, first.getSoldiers());
        assertEquals(0, second.getSoldiers());
        verify(gameEventRepository, times(2)).save(any());
    }

    @Test
    void tc25_applySeasonUpkeep_savesAllCitiesExactlyOnce() {
        City first = city(1L, 100, 10);
        when(cityRepository.findByGame_Id(1L)).thenReturn(List.of(first));
        stubNetwork();

        cityService.applySeasonUpkeep(1L);

        verify(cityRepository, times(1)).saveAll(List.of(first));
    }

    @Test
    void tc26_applySeasonUpkeep_onAGameWithoutCities_doesNothing() {
        when(cityRepository.findByGame_Id(99L)).thenReturn(List.of());

        cityService.applySeasonUpkeep(99L);

        verify(cityRepository, times(1)).saveAll(List.of());
        verify(gameEventRepository, never()).save(any());
    }
}
