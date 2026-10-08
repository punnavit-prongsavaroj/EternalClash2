package com.eternalclash2.strategy.marshal;

import com.eternalclash2.domain.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MarshalAbilityContext {
    private static final Map<String, MarshalSpecialAbilityStrategy> strategyMap = new HashMap<>();

    static {
        strategyMap.put("NO_ACCIDENT", new NoAccidentStrategy());
        strategyMap.put("SURVIVE_DESTRUCTION", new SurviveDestructionStrategy());
        strategyMap.put("FASTER_TRAVEL", new FasterTravelStrategy());
        strategyMap.put("REBELLION", new RebellionStrategy());
    }

    private final MarshalSpecialAbilityStrategy strategy;

    public MarshalAbilityContext(Player player) {
        if (player.getMarshal() != null && player.getMarshal().getSpecialAbilityType() != null) {
            this.strategy = strategyMap.getOrDefault(player.getMarshal().getSpecialAbilityType(), new StandardMarshalStrategy());
        } else {
            this.strategy = new StandardMarshalStrategy();
        }
    }

    public MarshalSpecialAbilityStrategy getStrategy() {
        return strategy;
    }
}
