package com.eternalclash2.strategy;

import com.eternalclash2.domain.entity.Marshal;
import com.eternalclash2.domain.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KongmingCombatStrategyTest {

    private static final int SAMPLES = 10_000;

    private final KongmingCombatStrategy strategy = new KongmingCombatStrategy();

    @Test
    void tc01_canSurviveDestruction_survivesAboutHalfOfTenThousandRuns() {
        int survivals = 0;
        for (int i = 0; i < SAMPLES; i++) {
            if (strategy.canSurviveDestruction()) {
                survivals++;
            }
        }

        double rate = (double) survivals / SAMPLES;
        assertTrue(rate > 0.45 && rate < 0.55,
                "survival rate should be around 50%, but was " + (rate * 100) + "%");
    }

    @Test
    void tc02_canSurviveDestruction_neverReturnsAConstant() {
        boolean sawTrue = false;
        boolean sawFalse = false;

        for (int i = 0; i < 200; i++) {
            sawTrue |= strategy.canSurviveDestruction();
            sawFalse |= !strategy.canSurviveDestruction();
        }

        assertTrue(sawTrue, "expected at least one survival in 200 rolls");
        assertTrue(sawFalse, "expected at least one destruction in 200 rolls");
    }

    @Test
    void tc03_calculateKills_isIdenticalToTheStandardStrategy() {
        Player player = Player.builder()
                .marshal(Marshal.builder().attackKillRatio(3.5).build())
                .build();

        assertEquals(350, strategy.calculateKills(player, 100));
        assertEquals(new StandardCombatStrategy().calculateKills(player, 100),
                strategy.calculateKills(player, 100));
    }
}
