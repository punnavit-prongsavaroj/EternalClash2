package com.eternalclash2.strategy.marshal;

import com.eternalclash2.domain.entity.GameEvent;
import com.eternalclash2.domain.entity.Player;

public class NoAccidentStrategy extends StandardMarshalStrategy {
    @Override
    public boolean preventsAccidents() {
        return true;
    }

    @Override
    public double getRecruitFoodMultiplier() {
        return 1.25;
    }
}
