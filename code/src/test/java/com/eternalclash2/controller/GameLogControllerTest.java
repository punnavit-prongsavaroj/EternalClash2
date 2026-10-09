package com.eternalclash2.controller;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.Battle;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.GameEvent;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.EventType;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.domain.enums.LocationType;
import com.eternalclash2.exception.GlobalExceptionHandler;
import com.eternalclash2.service.BattleService;
import com.eternalclash2.service.GameEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GameLogControllerTest {

    @Mock
    private BattleService battleService;

    @Mock
    private GameEventService gameEventService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new GameLogController(battleService, gameEventService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Game game(Long id, int turn) {
        return Game.builder().id(id).status(GameStatus.IN_PROGRESS).currentTurnNumber(turn).build();
    }

    private Player player(Long id, Game game) {
        return Player.builder().id(id).game(game).name("Player" + id).isAlive(true).build();
    }

    private Battle battle(Long id, Game game, int turn, boolean cityDestroyed) {
        Player attacker = player(2L, game);
        Army attackerArmy = Army.builder().id(11L).owner(attacker).soldiers(50).build();
        return Battle.builder().id(id).game(game).turnNumber(turn).attackerArmy(attackerArmy)
                .defenderPlayer(player(3L, game)).attackerSoldiers(50).defenderSoldiers(30)
                .attackerCasualties(30).defenderCasualties(30).isCityDestroyed(cityDestroyed).build();
    }

    @Test
    void tc01_getBattles_returnsOnlyTheRequestedGameAndTurn() throws Exception {
        Game first = game(1L, 4);
        given(battleService.findAll()).willReturn(List.of(
                battle(31L, first, 4, true),
                battle(32L, game(2L, 9), 9, false)));

        mockMvc.perform(get("/api/games/1/battles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(31));

        mockMvc.perform(get("/api/games/1/battles").param("turnNumber", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cityDestroyed").value(true));

        mockMvc.perform(get("/api/games/1/battles").param("turnNumber", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void tc02_getEvents_returnsOnlyTheRequestedGameAndTurn() throws Exception {
        Game first = game(1L, 3);
        Player affected = player(2L, first);
        GameEvent epidemic = GameEvent.builder().id(41L).game(first).turnNumber(3).eventType(EventType.EPIDEMIC)
                .affectedPlayer(affected).locationType(LocationType.IN_CITY)
                .foodImpact(0).soldierImpact(-10).extraTravelTurns(0).description("Epidemic in City-10").build();
        GameEvent starvation = GameEvent.builder().id(42L).game(first).turnNumber(3).eventType(EventType.STARVATION)
                .affectedPlayer(affected).locationType(LocationType.IN_CITY)
                .foodImpact(-20).soldierImpact(0).extraTravelTurns(0).description("Starvation in City-10").build();
        GameEvent otherGame = GameEvent.builder().id(43L).game(game(2L, 9)).turnNumber(9).eventType(EventType.FLOOD)
                .affectedPlayer(player(5L, game(2L, 9))).locationType(LocationType.IN_CITY)
                .foodImpact(0).soldierImpact(0).extraTravelTurns(0).description("Flood elsewhere").build();
        given(gameEventService.findAll()).willReturn(List.of(epidemic, starvation, otherGame));

        mockMvc.perform(get("/api/games/1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/games/1/events").param("turnNumber", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].eventType").value("EPIDEMIC"));

        mockMvc.perform(get("/api/games/1/events").param("turnNumber", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
