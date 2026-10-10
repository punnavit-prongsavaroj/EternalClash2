package com.eternalclash2.strategy.marshal;

import com.eternalclash2.domain.entity.Marshal;
import com.eternalclash2.domain.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarshalAbilityContextTest {

    private MarshalAbilityContext contextFor(String specialAbilityType) {
        return new MarshalAbilityContext(
                Player.builder().marshal(Marshal.builder().specialAbilityType(specialAbilityType).build()).build());
    }

    @Test
    void tc01_getStrategy_withoutMarshalUsesStandardStrategy() {
        MarshalAbilityContext context = new MarshalAbilityContext(Player.builder().marshal(null).build());

        assertInstanceOf(StandardMarshalStrategy.class, context.getStrategy());
    }

    @Test
    void tc02_getStrategy_noAccidentPreventsAccidentsAndRaisesRecruitCost() {
        MarshalSpecialAbilityStrategy strategy = contextFor("NO_ACCIDENT").getStrategy();

        assertInstanceOf(NoAccidentStrategy.class, strategy);
        assertTrue(strategy.preventsAccidents());
        assertEquals(1.25, strategy.getRecruitFoodMultiplier());
    }

    @Test
    void tc03_getStrategy_rebellionCausesRebellions() {
        MarshalSpecialAbilityStrategy strategy = contextFor("REBELLION").getStrategy();

        assertInstanceOf(RebellionStrategy.class, strategy);
        assertTrue(strategy.causesRebellions());
    }

    @Test
    void tc04_getStrategy_unknownSpecialFallsBackToStandard() {
        assertInstanceOf(StandardMarshalStrategy.class, contextFor("UNKNOWN_FOO").getStrategy());
    }

    @Test
    void tc05_getStrategy_fasterTravelOnlyChangesTravel() {
        MarshalSpecialAbilityStrategy strategy = contextFor("FASTER_TRAVEL").getStrategy();

        assertInstanceOf(FasterTravelStrategy.class, strategy);
        assertTrue(strategy.travelsFaster());
        assertFalse(strategy.preventsAccidents());
        assertFalse(strategy.causesRebellions());
        assertFalse(strategy.survivesDestruction());
        assertEquals(1.0, strategy.getRecruitFoodMultiplier());
    }

    @Test
    void tc06_getStrategy_surviveDestructionSurvivesDestruction() {
        MarshalSpecialAbilityStrategy strategy = contextFor("SURVIVE_DESTRUCTION").getStrategy();

        assertInstanceOf(SurviveDestructionStrategy.class, strategy);
        assertTrue(strategy.survivesDestruction());
    }

    @Test
    void tc07_getStrategy_withNullSpecialTypeFallsBackToStandard() {
        assertInstanceOf(StandardMarshalStrategy.class, contextFor(null).getStrategy());
    }

    @Test
    void tc08_getStrategy_noAccidentLeavesOtherFlagsAtTheirDefaults() {
        MarshalSpecialAbilityStrategy strategy = contextFor("NO_ACCIDENT").getStrategy();

        assertFalse(strategy.travelsFaster());
        assertFalse(strategy.causesRebellions());
    }
}
