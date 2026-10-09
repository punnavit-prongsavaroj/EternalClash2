package com.eternalclash2.strategy.marshal;

import com.eternalclash2.domain.entity.GameEvent;
import com.eternalclash2.domain.entity.Player;

public class StandardMarshalStrategy implements MarshalSpecialAbilityStrategy {
    @Override
    public boolean preventsAccidents() { return false; }

    @Override
    public boolean survivesDestruction() { return false; }

    @Override
    public boolean travelsFaster() { return false; }

    @Override
    public boolean causesRebellions() { return false; }

    @Override
    public double getRecruitFoodMultiplier() { return 1.0; }

    @Override
    public void applyCustomEventEffects(GameEvent event, Player player) {
        // No custom effects
    }
}
