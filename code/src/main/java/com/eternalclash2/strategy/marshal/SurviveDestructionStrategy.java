package com.eternalclash2.strategy.marshal;

import com.eternalclash2.domain.entity.GameEvent;
import com.eternalclash2.domain.entity.Player;

public class SurviveDestructionStrategy extends StandardMarshalStrategy {
    @Override
    public boolean survivesDestruction() {
        return true;
    }
}
