package com.eternalclash2.controller;

import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Marshal;
import com.eternalclash2.domain.entity.MarshalCandidate;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.GlobalExceptionHandler;
import com.eternalclash2.service.MarshalCandidateService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MarshalDraftControllerTest {

    @Mock
    private MarshalCandidateService marshalCandidateService;

    private MockMvc mockMvc;

    private final Game game = Game.builder().id(1L).status(GameStatus.MARSHAL_SELECTION).currentTurnNumber(0).build();
    private final Player player = Player.builder().id(2L).game(game).name("Pizza").isAlive(true).rerollCount(1).build();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MarshalDraftController(marshalCandidateService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private MarshalCandidate candidate(Long id, int slot, String marshalName) {
        return MarshalCandidate.builder().id(id).player(player).slotNumber(slot).isSelected(false)
                .marshal(Marshal.builder().id(id).name(marshalName).build()).build();
    }

    @Test
    void tc01_reroll_returnsTheNewlyVisibleSlot() throws Exception {
        given(marshalCandidateService.reroll(2L)).willReturn(candidate(21L, 2, "ลิโป้"));

        mockMvc.perform(post("/api/players/2/marshal-candidates/reroll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotNumber").value(2))
                .andExpect(jsonPath("$.marshal.name").value("ลิโป้"));
    }

    @Test
    void tc02_getCurrentCandidate_returnsEverySavedSlot() throws Exception {
        given(marshalCandidateService.findForPlayer(2L)).willReturn(List.of(
                candidate(21L, 1, "โจโฉ"), candidate(22L, 2, "ลิโป้"), candidate(23L, 3, "ซุนซก")));

        mockMvc.perform(get("/api/players/2/marshal-candidates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void tc03_chooseCurrent_returnsTheChosenMarshal() throws Exception {
        given(marshalCandidateService.chooseCurrent(2L))
                .willReturn(Marshal.builder().id(22L).name("ลิโป้").foodProduction(20).build());

        mockMvc.perform(post("/api/players/2/marshal-candidates/choose"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(22))
                .andExpect(jsonPath("$.name").value("ลิโป้"));
    }

    @Test
    void tc04_reroll_withoutRerollsRemaining_isRejected() throws Exception {
        given(marshalCandidateService.reroll(2L))
                .willThrow(new BusinessLogicException("No rerolls remain"));

        mockMvc.perform(post("/api/players/2/marshal-candidates/reroll"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No rerolls remain"));
    }
}
