package com.eternalclash2.strategy;
import com.eternalclash2.domain.entity.Player;
import java.util.HashMap;
import java.util.Map;

public class CombatContext {
    private static final Map<String, CombatStrategy> strategyMap = new HashMap<>();

    static {
        strategyMap.put("SURVIVE_DESTRUCTION", new KongmingCombatStrategy());
    }

    private final CombatStrategy strategy;

    public CombatContext(Player player) {
        if (player.getMarshal() != null && player.getMarshal().getSpecialAbilityType() != null) {
            this.strategy = strategyMap.getOrDefault(player.getMarshal().getSpecialAbilityType(), new StandardCombatStrategy());
        } else {
            this.strategy = new StandardCombatStrategy();
        }
    }

    public int executeKills(Player player, int soldiers) {
        return strategy.calculateKills(player, soldiers);
    }

    public boolean executeSurvival() {
        return strategy.canSurviveDestruction();
    }
}
