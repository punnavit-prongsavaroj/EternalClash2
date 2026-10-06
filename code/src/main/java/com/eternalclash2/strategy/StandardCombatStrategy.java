package com.eternalclash2.strategy;
import com.eternalclash2.domain.entity.Player;

public class StandardCombatStrategy implements CombatStrategy {
    @Override
    public int calculateKills(Player player, int soldiers) {
        double ratio = player.getMarshal() == null || player.getMarshal().getAttackKillRatio() == null
                ? 1.0 : player.getMarshal().getAttackKillRatio();
        return Math.max(0, (int) Math.floor(soldiers * ratio));
    }

    @Override
    public boolean canSurviveDestruction() {
        return false;
    }
}
