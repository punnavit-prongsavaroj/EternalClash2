package com.eternalclash2.strategy;
import com.eternalclash2.domain.entity.Player;
import java.util.concurrent.ThreadLocalRandom;

public class KongmingCombatStrategy extends StandardCombatStrategy {
    @Override
    public boolean canSurviveDestruction() {
        return ThreadLocalRandom.current().nextInt(100) < 50;
    }
}
