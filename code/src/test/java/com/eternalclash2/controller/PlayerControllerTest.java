package com.eternalclash2.controller;

import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Marshal;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.exception.GlobalExceptionHandler;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PlayerControllerTest {

    @Mock
    private PlayerService playerService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PlayerController(playerService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void tc01_getPlayer_serialisesOnlyFlatReferences() throws Exception {
        Game game = Game.builder().id(10L).status(GameStatus.IN_PROGRESS).currentTurnNumber(3).build();
        Player player = Player.builder().id(1L).game(game).name("Pizza").isAlive(true).rerollCount(0)
                .marshal(Marshal.builder().id(7L).name("โจโฉ").foodProduction(25).build()).build();
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
    void tc02_getPlayer_withUnknownId_returnsNotFound() throws Exception {
        given(playerService.findById(99L)).willThrow(new ResourceNotFoundException("Player not found"));

        mockMvc.perform(get("/api/players/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
