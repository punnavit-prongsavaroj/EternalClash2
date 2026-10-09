package com.eternalclash2.strategy;

import com.eternalclash2.domain.entity.Marshal;
import com.eternalclash2.domain.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatContextTest {

    private static final int ROLLS = 200;

    private Player playerWithSpecial(String specialAbilityType, Double attackKillRatio) {
        return Player.builder()
                .marshal(Marshal.builder()
                        .specialAbilityType(specialAbilityType)
                        .attackKillRatio(attackKillRatio)
                        .build())
                .build();
    }

    @Test
    void tc01_executeSurvival_usesKongmingStrategyForSurviveDestruction() {
        CombatContext context = new CombatContext(playerWithSpecial("SURVIVE_DESTRUCTION", 1.0));

        boolean sawSurvival = false;
        for (int i = 0; i < ROLLS; i++) {
            sawSurvival |= context.executeSurvival();
        }

        assertTrue(sawSurvival, "KongmingCombatStrategy must sometimes survive destruction");
    }

    @Test
    void tc02_executeKills_fallsBackToStandardStrategyForUnregisteredSpecial() {
        Player player = playerWithSpecial("NO_ACCIDENT", 1.0);
        CombatContext context = new CombatContext(player);

        boolean sawSurvival = false;
        for (int i = 0; i < ROLLS; i++) {
            sawSurvival |= context.executeSurvival();
        }

        assertFalse(sawSurvival, "only SURVIVE_DESTRUCTION registers a survival-capable strategy");
        assertEquals(100, context.executeKills(player, 100));
    }

    @Test
    void tc03_executeSurvival_usesStandardStrategyWithoutMarshal() {
        CombatContext context = new CombatContext(Player.builder().marshal(null).build());

        assertFalse(context.executeSurvival());
        assertEquals(100, context.executeKills(Player.builder().marshal(null).build(), 100));
    }

    @Test
    void tc04_executeKills_appliesTheMarshalKillRatio() {
        Player player = playerWithSpecial("REBELLION", 2.0);
        CombatContext context = new CombatContext(player);

        assertEquals(80, context.executeKills(player, 40));
    }
}
