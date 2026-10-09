package com.eternalclash2.strategy.marshal;

import com.eternalclash2.domain.entity.Marshal;
import com.eternalclash2.domain.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MarshalAbilityContextTest {

    @Test
    void nullMarshal_ReturnsStandardStrategy() {
        Player player = Player.builder().build();
        MarshalAbilityContext context = new MarshalAbilityContext(player);
        
        assertInstanceOf(StandardMarshalStrategy.class, context.getStrategy());
    }

    @Test
    void noAccidentMarshal_ReturnsNoAccidentStrategy() {
        Marshal marshal = Marshal.builder().specialAbilityType("NO_ACCIDENT").build();
        Player player = Player.builder().marshal(marshal).build();
        
        MarshalAbilityContext context = new MarshalAbilityContext(player);
        
        assertInstanceOf(NoAccidentStrategy.class, context.getStrategy());
        assertTrue(context.getStrategy().preventsAccidents());
        assertEquals(1.25, context.getStrategy().getRecruitFoodMultiplier());
    }

    @Test
    void rebellionMarshal_ReturnsRebellionStrategy() {
        Marshal marshal = Marshal.builder().specialAbilityType("REBELLION").build();
        Player player = Player.builder().marshal(marshal).build();
        
        MarshalAbilityContext context = new MarshalAbilityContext(player);
        
        assertInstanceOf(RebellionStrategy.class, context.getStrategy());
        assertTrue(context.getStrategy().causesRebellions());
    }

    @Test
    void unknownAbility_ReturnsStandardStrategy() {
        Marshal marshal = Marshal.builder().specialAbilityType("UNKNOWN_FOO").build();
        Player player = Player.builder().marshal(marshal).build();
        
        MarshalAbilityContext context = new MarshalAbilityContext(player);
        
        assertInstanceOf(StandardMarshalStrategy.class, context.getStrategy());
    }
}
