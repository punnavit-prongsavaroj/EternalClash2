package com.eternalclash2.strategy;

import com.eternalclash2.domain.entity.Army;


public interface CombatStrategy {

    public int calculateCasualties(Army attacker, Army defender);
}