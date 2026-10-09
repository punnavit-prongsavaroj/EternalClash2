package com.eternalclash2.controller;

import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.GlobalExceptionHandler;
import com.eternalclash2.service.PlacementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PlacementControllerTest {

    @Mock
    private PlacementService placementService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PlacementController(placementService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void tc01_selectBase_readsQueryParametersAndReturnsEmptyBody() throws Exception {
        mockMvc.perform(post("/api/games/1/placement").param("playerId", "1").param("cityId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(placementService).selectBase(1L, 1L, 1L);
    }

    @Test
    void tc02_selectBase_outsidePlacementPhase_isRejectedAsConflict() throws Exception {
        doThrow(new BusinessLogicException("Game is not in placement phase"))
                .when(placementService).selectBase(1L, 1L, 99L);

        mockMvc.perform(post("/api/games/1/placement").param("playerId", "1").param("cityId", "99"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Game is not in placement phase"));
    }
}
