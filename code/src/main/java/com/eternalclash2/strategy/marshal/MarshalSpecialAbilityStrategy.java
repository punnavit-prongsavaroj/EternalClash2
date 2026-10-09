package com.eternalclash2.strategy.marshal;

import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.entity.GameEvent;
import com.eternalclash2.domain.entity.City;

public interface MarshalSpecialAbilityStrategy {
    boolean preventsAccidents();
    boolean survivesDestruction();
    boolean travelsFaster();
    boolean causesRebellions();
    double getRecruitFoodMultiplier();
    void applyCustomEventEffects(GameEvent event, Player player);
}
