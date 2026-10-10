package com.eternalclash2.strategy;

import com.eternalclash2.domain.entity.Marshal;
import com.eternalclash2.domain.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class StandardCombatStrategyTest {

    private final StandardCombatStrategy strategy = new StandardCombatStrategy();

    private Player playerWithRatio(Double attackKillRatio) {
        return Player.builder()
                .marshal(attackKillRatio == null ? null
                        : Marshal.builder().attackKillRatio(attackKillRatio).build())
                .build();
    }

    @Test
    void tc01_calculateKills_withoutMarshalDefaultsToRatioOne() {
        Player player = Player.builder().marshal(null).build();

        assertEquals(100, strategy.calculateKills(player, 100));
    }

    @Test
    void tc02_calculateKills_clampsNegativeRatioToZero() {
        assertEquals(0, strategy.calculateKills(playerWithRatio(-1.0), 100));
    }

    @Test
    void tc03_calculateKills_appliesTheMarshalRatio() {
        assertEquals(350, strategy.calculateKills(playerWithRatio(3.5), 100));
    }

    @Test
    void tc04_calculateKills_floorsFractionalKills() {
        assertEquals(7, strategy.calculateKills(playerWithRatio(0.5), 15));
    }

    @Test
    void tc05_canSurviveDestruction_isAlwaysFalse() {
        assertFalse(strategy.canSurviveDestruction());
        assertFalse(strategy.canSurviveDestruction());
        assertFalse(strategy.canSurviveDestruction());
    }
}
