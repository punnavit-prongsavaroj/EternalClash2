package com.eternalclash2.strategy;
import com.eternalclash2.domain.entity.Player;

public interface CombatStrategy {
    int calculateKills(Player player, int soldiers);
    boolean canSurviveDestruction();
}
