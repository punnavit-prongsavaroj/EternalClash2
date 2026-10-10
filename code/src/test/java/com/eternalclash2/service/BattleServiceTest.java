package com.eternalclash2.service;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.Battle;
import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.GameEvent;
import com.eternalclash2.domain.entity.Marshal;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.ArmyStatus;
import com.eternalclash2.domain.enums.BattleResult;
import com.eternalclash2.domain.enums.BattleType;
import com.eternalclash2.domain.enums.EventType;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.ArmyRepository;
import com.eternalclash2.repository.BattleRepository;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.GameEventRepository;
import com.eternalclash2.repository.GameRepository;
import com.eternalclash2.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BattleServiceTest {

    @Mock
    private ArmyRepository armyRepository;
    @Mock
    private BattleRepository battleRepository;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private GameEventRepository gameEventRepository;
    @Mock
    private GameRuleService gameRuleService;

    @InjectMocks
    private BattleService battleService;

    private Game game;
    private Player attacker;
    private Player defender;

    @BeforeEach
    void buildFixtures() {
        game = Game.builder().id(1L).status(GameStatus.IN_PROGRESS).currentTurnNumber(1).build();
        attacker = Player.builder().id(100L).name("Attacker").game(game).isAlive(true).build();
        defender = Player.builder().id(200L).name("Defender").game(game).isAlive(true).build();
    }

    private City city(Long id, Player owner, int soldiers) {
        return City.builder().id(id).game(game).name("City" + id).player(owner).soldiers(soldiers).food(30).build();
    }

    private Army marching(Long id, Player owner, City source, City target, int soldiers) {
        return Army.builder().id(id).owner(owner).sourceCity(source).targetCity(target).soldiers(soldiers)
                .departureTurn(1).arrivalTurn(1).status(ArmyStatus.TRAVELING).build();
    }

    private void stubTraveling(Army... armies) {
        when(armyRepository.findByTargetCity_Game_IdAndStatus(1L, ArmyStatus.TRAVELING)).thenReturn(List.of(armies));
    }

    @Test
    void tc01_resolveBattles_aStrongSiegeCapturesTheCity() {
        City source = city(1L, attacker, 0);
        City target = city(2L, defender, 10);
        Army army = marching(11L, attacker, source, target, 100);
        stubTraveling(army);

        battleService.resolveBattles(1L, 1);

        assertEquals(ArmyStatus.DESTROYED, army.getStatus());
        assertSame(attacker, target.getPlayer());
        verify(gameRuleService).checkPlayerElimination(defender, 1);
        verify(gameRuleService).checkWinCondition(game);
        ArgumentCaptor<Battle> saved = ArgumentCaptor.forClass(Battle.class);
        verify(battleRepository).save(saved.capture());
        assertEquals(BattleResult.ATTACKER_WIN, saved.getValue().getResult());
        assertTrue(saved.getValue().getIsCityDestroyed());
    }

    @Test
    void tc02_resolveBattles_aWeakSiegeIsRepelled() {
        City source = city(1L, attacker, 0);
        City target = city(2L, defender, 100);
        Army army = marching(11L, attacker, source, target, 10);
        stubTraveling(army);

        battleService.resolveBattles(1L, 1);

        assertEquals(ArmyStatus.DESTROYED, army.getStatus());
        assertSame(defender, target.getPlayer());
        assertTrue(target.getSoldiers() > 0);
        ArgumentCaptor<Battle> saved = ArgumentCaptor.forClass(Battle.class);
        verify(battleRepository).save(saved.capture());
        assertEquals(BattleResult.DEFENDER_WIN, saved.getValue().getResult());
    }

    @Test
    void tc03_findById_withUnknownId_throwsResourceNotFoundException() {
        when(battleRepository.findById(2L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> battleService.findById(2L));

        assertEquals("Battle not found with id: 2", exception.getMessage());
    }

    @Test
    void tc04_resolveBattles_anArmyReachingItsOwnCityReinforcesItWithoutABattle() {
        City home = city(1L, attacker, 30);
        City friendlyCity = city(2L, attacker, 10);
        Army army = marching(11L, attacker, home, friendlyCity, 10);
        stubTraveling(army);

        battleService.resolveBattles(1L, 1);

        assertEquals(20, friendlyCity.getSoldiers());
        assertEquals(0, army.getSoldiers());
        assertEquals(ArmyStatus.DESTROYED, army.getStatus());
        verify(battleRepository, never()).save(any());
    }

    @Test
    void tc05_resolveBattles_armiesCrossingTheSameEdgeFightInTheField() {
        City city1 = city(1L, attacker, 0);
        City city2 = city(2L, defender, 0);
        Army first = marching(11L, attacker, city1, city2, 60);
        Army second = marching(12L, defender, city2, city1, 40);
        first.setArrivalTurn(3);
        second.setArrivalTurn(3);
        stubTraveling(first, second);

        battleService.resolveBattles(1L, 2);

        assertEquals(20, first.getSoldiers());
        assertEquals(0, second.getSoldiers());
        assertEquals(ArmyStatus.CANCELLED, second.getStatus());
        ArgumentCaptor<Battle> saved = ArgumentCaptor.forClass(Battle.class);
        verify(battleRepository, times(1)).save(saved.capture());
        assertEquals(BattleType.FIELD_ENCOUNTER, saved.getValue().getBattleType());
        assertFalse(saved.getValue().getIsCityDestroyed());
    }

    @Test
    void tc06_resolveBattles_aNeutralCityDefendsOneForOne() {
        City source = city(1L, attacker, 0);
        City neutral = city(2L, null, 30);
        Army army = marching(11L, attacker, source, neutral, 50);
        stubTraveling(army);

        battleService.resolveBattles(1L, 1);

        assertSame(attacker, neutral.getPlayer());
        assertEquals(20, neutral.getSoldiers());
        assertEquals(0, neutral.getFood());
        verify(gameRuleService, never()).checkPlayerElimination(any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void tc07_resolveBattles_kongmingDefenderCanSaveItsCityByHalfTheRolls() {
        defender.setMarshal(Marshal.builder().id(5L).name("Kongming").specialAbilityType("SURVIVE_DESTRUCTION").build());
        City source = city(1L, attacker, 0);
        City target = city(2L, defender, 10);
        boolean survivalSeen = false;

        // KongmingCombatStrategy rolls ThreadLocalRandom, so the survival branch can only be observed statistically.
        for (int attempt = 0; attempt < 60 && !survivalSeen; attempt++) {
            City livingTarget = city(2L, defender, 10);
            Army army = marching(11L, attacker, source, livingTarget, 50);
            stubTraveling(army);
            Mockito.clearInvocations(battleRepository, gameEventRepository, gameRuleService);

            battleService.resolveBattles(1L, 1);

            if (livingTarget.getPlayer() == defender) {
                survivalSeen = true;
                assertEquals(1, livingTarget.getSoldiers());
                assertEquals(0, army.getSoldiers());
                assertEquals(ArmyStatus.DESTROYED, army.getStatus());
                ArgumentCaptor<GameEvent> event = ArgumentCaptor.forClass(GameEvent.class);
                verify(gameEventRepository, times(1)).save(event.capture());
                assertEquals(EventType.REBELLION, event.getValue().getEventType());
            }
        }

        assertTrue(survivalSeen, "the 50% survival roll never came up in 60 attempts");
    }

    @Test
    void tc08_resolveBattles_attackersThatFailToTakeTheCityAreWipedOut() {
        City source = city(1L, attacker, 0);
        City target = city(2L, defender, 30);
        attacker.setMarshal(Marshal.builder().id(4L).name("CaoCao").attackKillRatio(0.5).build());
        Army army = marching(11L, attacker, source, target, 40);
        stubTraveling(army);

        battleService.resolveBattles(1L, 1);

        assertEquals(0, army.getSoldiers());
        assertEquals(ArmyStatus.DESTROYED, army.getStatus());
        ArgumentCaptor<Battle> saved = ArgumentCaptor.forClass(Battle.class);
        verify(battleRepository).save(saved.capture());
        assertEquals(BattleResult.DEFENDER_WIN, saved.getValue().getResult());
        assertEquals(10, saved.getValue().getDefenderSoldiers() - saved.getValue().getDefenderCasualties());
    }

    @Test
    void tc09_resolveBattles_resolvesTwoArmiesAgainstOneCityInOrder() {
        City home = city(1L, attacker, 0);
        City friendly = city(3L, attacker, 5);
        City captured = city(2L, defender, 10);
        Army first = marching(11L, attacker, home, captured, 30);
        Army second = marching(12L, attacker, home, captured, 20);
        Army empty = marching(13L, attacker, home, friendly, 0);
        stubTraveling(first, second, empty);

        battleService.resolveBattles(1L, 1);

        assertSame(attacker, captured.getPlayer());
        assertEquals(40, captured.getSoldiers());
        assertEquals(0, second.getSoldiers());
        assertEquals(ArmyStatus.TRAVELING, empty.getStatus());
        verify(battleRepository, times(1)).save(any());
    }

    @Test
    void tc10_resolveBattles_withNothingDueThisTurn_changesNothing() {
        City source = city(1L, attacker, 0);
        City target = city(2L, defender, 10);
        Army army = marching(11L, attacker, source, target, 50);
        army.setArrivalTurn(5);
        stubTraveling(army);

        battleService.resolveBattles(1L, 1);

        assertEquals(50, army.getSoldiers());
        assertEquals(ArmyStatus.TRAVELING, army.getStatus());
        verify(battleRepository, never()).save(any());
        verify(gameRuleService, never()).checkWinCondition(any());
    }

    @Test
    void tc11_resolveBattles_stopsAsSoonAsTheGameIsFinished() {
        City home = city(1L, attacker, 0);
        City firstTarget = city(2L, defender, 10);
        City secondTarget = city(3L, defender, 10);
        Army first = marching(11L, attacker, home, firstTarget, 30);
        Army second = marching(12L, attacker, home, secondTarget, 30);
        stubTraveling(first, second);
        Mockito.doAnswer(invocation -> {
            game.setStatus(GameStatus.FINISHED);
            return null;
        }).when(gameRuleService).checkWinCondition(game);

        battleService.resolveBattles(1L, 1);

        assertEquals(0, first.getSoldiers());
        assertEquals(30, second.getSoldiers());
        assertEquals(ArmyStatus.TRAVELING, second.getStatus());
        verify(battleRepository, times(1)).save(any());
    }
}
