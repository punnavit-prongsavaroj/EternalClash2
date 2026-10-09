package com.eternalclash2.service;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.entity.PlayerStats;
import com.eternalclash2.domain.enums.ArmyStatus;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.repository.ArmyRepository;
import com.eternalclash2.repository.BattleRepository;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.GameEventRepository;
import com.eternalclash2.repository.GameRepository;
import com.eternalclash2.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BattleServiceTest {

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

    @Test
    void resolveBattles_CitySiege_AttackerWins() {
        Game game = Game.builder().id(1L).status(GameStatus.IN_PROGRESS).build();
        Player attacker = Player.builder().id(100L).name("Attacker").game(game).playerStats(new PlayerStats()).build();
        Player defender = Player.builder().id(200L).name("Defender").game(game).build();
        
        City targetCity = City.builder().id(10L).name("CityName").player(defender).soldiers(10).build();
        Army army = Army.builder().id(1L).owner(attacker).targetCity(targetCity)
                .soldiers(100).departureTurn(1).arrivalTurn(1).status(ArmyStatus.TRAVELING).build();

        when(armyRepository.findByTargetCity_Game_IdAndStatus(1L, ArmyStatus.TRAVELING))
                .thenReturn(List.of(army));

        battleService.resolveBattles(1L, 1);

        assertEquals(ArmyStatus.DESTROYED, army.getStatus());
        assertEquals(attacker, targetCity.getPlayer()); // City captured
        verify(gameRuleService).checkPlayerElimination(defender, 1);
        verify(gameRuleService).checkWinCondition(game);
        verify(battleRepository).save(any());
    }

    @Test
    void resolveBattles_CitySiege_DefenderWins() {
        Game game = Game.builder().id(1L).status(GameStatus.IN_PROGRESS).build();
        Player attacker = Player.builder().id(100L).name("Attacker").game(game).build();
        Player defender = Player.builder().id(200L).name("Defender").game(game).build();
        
        City targetCity = City.builder().id(10L).player(defender).soldiers(100).build();
        Army army = Army.builder().id(1L).owner(attacker).targetCity(targetCity)
                .soldiers(10).departureTurn(1).arrivalTurn(1).status(ArmyStatus.TRAVELING).build();

        when(armyRepository.findByTargetCity_Game_IdAndStatus(1L, ArmyStatus.TRAVELING))
                .thenReturn(List.of(army));

        battleService.resolveBattles(1L, 1);

        assertEquals(ArmyStatus.DESTROYED, army.getStatus());
        assertEquals(defender, targetCity.getPlayer()); // City not captured
        assertTrue(targetCity.getSoldiers() > 0);
        verify(battleRepository).save(any());
    }
}
