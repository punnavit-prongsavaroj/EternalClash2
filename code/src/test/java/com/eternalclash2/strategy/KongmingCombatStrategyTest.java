package com.eternalclash2.strategy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KongmingCombatStrategyTest {

    @Test
    void testSurviveDestructionProbability() {
        KongmingCombatStrategy strategy = new KongmingCombatStrategy();
        
        int survivalCount = 0;
        int totalTests = 10000;
        
        for (int i = 0; i < totalTests; i++) {
            if (strategy.canSurviveDestruction()) {
                survivalCount++;
            }
        }
        
        double survivalRate = (double) survivalCount / totalTests;
        
        assertTrue(survivalRate > 0.45 && survivalRate < 0.55, 
            "Survival rate should be around 50%, but was " + (survivalRate * 100) + "%");
    }
}
