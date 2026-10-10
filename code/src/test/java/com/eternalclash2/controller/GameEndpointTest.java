package com.eternalclash2.controller;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.Battle;
import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Marshal;
import com.eternalclash2.domain.entity.MarshalCandidate;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.entity.TurnAction;
import com.eternalclash2.domain.enums.ActionType;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.service.BattleService;
import com.eternalclash2.service.GameEventService;
import com.eternalclash2.service.GameService;
import com.eternalclash2.service.GameViewService;
import com.eternalclash2.service.MarshalCandidateService;
import com.eternalclash2.service.PlayerService;
import com.eternalclash2.service.TurnActionService;
import com.eternalclash2.service.TurnService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {GameController.class, PlayerController.class, TurnController.class,
        MarshalDraftController.class, GameLogController.class})
class GameEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;
    @MockBean
    private PlayerService playerService;
    @MockBean
    private GameViewService gameViewService;
    @MockBean
    private TurnService turnService;
    @MockBean
    private TurnActionService turnActionService;
    @MockBean
    private MarshalCandidateService marshalCandidateService;
    @MockBean
    private BattleService battleService;
    @MockBean
    private GameEventService gameEventService;

    private Game game(Long id, GameStatus status, int turn) {
        return Game.builder().id(id).status(status).currentTurnNumber(turn).build();
    }

    private Player player(Long id, Game game) {
        return Player.builder().id(id).game(game).name("Pizza").isAlive(true).rerollCount(0)
                .marshal(Marshal.builder().id(7L).name("โจโฉ").foodProduction(25).build()).build();
    }

    @Test
    void tc01_postGames_createsAGameEvenThoughTheBuilderLeavesPlayersNull() throws Exception {
        given(gameService.createGame()).willReturn(game(1L, GameStatus.WAITING, 0));

        mockMvc.perform(post("/api/games"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.playerCount").value(0))
                .andExpect(jsonPath("$.alivePlayerCount").value(0));
    }

    @Test
    void tc02_getPlayer_serialisesWithoutFollowingEntityRelations() throws Exception {
        Game game = game(10L, GameStatus.IN_PROGRESS, 3);
        Player player = player(1L, game);
        given(playerService.findById(1L)).willReturn(player);

        mockMvc.perform(get("/api/players/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(10))
                .andExpect(jsonPath("$.name").value("Pizza"))
                .andExpect(jsonPath("$.marshal.name").value("โจโฉ"))
                .andExpect(jsonPath("$.game").doesNotExist())
                .andExpect(jsonPath("$.city").doesNotExist());
    }

    @Test
    void tc03_postPlayer_rejectsBlankPlayerName() throws Exception {
        mockMvc.perform(post("/api/games/1/players").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void tc04_postAction_recordsTheCommandResult() throws Exception {
        Game game = game(1L, GameStatus.IN_PROGRESS, 3);
        Player player = player(2L, game);
        City sourceCity = City.builder().id(10L).player(player).name("Source").build();
        City targetCity = City.builder().id(3L).player(player(4L, game)).name("Target").build();
        TurnAction action = TurnAction.builder().id(9L).game(game).turnNumber(3).player(player).city(sourceCity)
                .actionType(ActionType.SEND_ARMY).foodBefore(50).foodAfter(25).soldiersBefore(100).soldiersAfter(80)
                .army(Army.builder().id(11L).owner(player).sourceCity(sourceCity).targetCity(targetCity).soldiers(20).build()).build();
        given(turnActionService.performAction(eq(1L), eq(2L), eq(10L), eq(ActionType.SEND_ARMY), eq(3L), eq(20)))
                .willReturn(action);

        mockMvc.perform(post("/api/games/1/players/2/actions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cityId\":10,\"actionType\":\"SEND_ARMY\",\"targetCityId\":3,\"soldierCount\":20}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.turnNumber").value(3))
                .andExpect(jsonPath("$.actionType").value("SEND_ARMY"))
                .andExpect(jsonPath("$.armyId").value(11))
                .andExpect(jsonPath("$.soldiersAfter").value(80));
    }

    @Test
    void tc05_postReroll_revealsTheNextCandidateSlot() throws Exception {
        Player player = player(2L, game(1L, GameStatus.MARSHAL_SELECTION, 0));
        given(marshalCandidateService.reroll(2L)).willReturn(MarshalCandidate.builder().id(21L).player(player)
                .marshal(Marshal.builder().id(2L).name("ลิโป้").build()).slotNumber(2).isSelected(false).build());

        mockMvc.perform(post("/api/players/2/marshal-candidates/reroll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotNumber").value(2))
                .andExpect(jsonPath("$.marshal.name").value("ลิโป้"));
    }

    @Test
    void tc06_getBattles_areLimitedToOneGameAndOneTurn() throws Exception {
        Game first = game(1L, GameStatus.IN_PROGRESS, 4);
        Player attacker = player(2L, first);
        Player defender = player(3L, first);
        City targetCity = City.builder().id(3L).player(defender).name("DefCity").build();
        Army army = Army.builder().id(11L).owner(attacker).targetCity(targetCity).soldiers(50).build();
        given(battleService.findAll()).willReturn(List.of(
                Battle.builder().id(31L).game(first).turnNumber(4).attackerArmy(army).defenderPlayer(defender)
                        .attackerSoldiers(50).defenderSoldiers(30).attackerCasualties(30).defenderCasualties(30)
                        .isCityDestroyed(true).build(),
                Battle.builder().id(32L).game(game(2L, GameStatus.IN_PROGRESS, 9)).turnNumber(9).attackerArmy(army)
                        .defenderPlayer(defender).attackerSoldiers(10).defenderSoldiers(5).build()));

        mockMvc.perform(get("/api/games/1/battles"))
                .andExpect(content().json("[{\"id\":31,\"turnNumber\":4}]"));
        mockMvc.perform(get("/api/games/1/battles").param("turnNumber", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cityDestroyed").value(true));
    }
}
