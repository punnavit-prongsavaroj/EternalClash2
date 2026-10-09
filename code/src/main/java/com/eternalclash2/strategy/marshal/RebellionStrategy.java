package com.eternalclash2.strategy.marshal;

import com.eternalclash2.domain.entity.GameEvent;
import com.eternalclash2.domain.entity.Player;

public class RebellionStrategy extends StandardMarshalStrategy {
    @Override
    public boolean causesRebellions() {
        return true;
    }
}
