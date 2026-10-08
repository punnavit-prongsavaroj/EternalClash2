package com.eternalclash2.dto;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.Battle;
import com.eternalclash2.domain.enums.BattleResult;
import com.eternalclash2.domain.enums.BattleType;

public record BattleDto(Long id, Long gameId, Integer turnNumber, BattleType battleType, Long attackerPlayerId,
                        Long attackerArmyId, Long defenderPlayerId, Long defenderArmyId, Integer attackerSoldiers,
                        Integer defenderSoldiers, Integer attackerCasualties, Integer defenderCasualties,
                        Boolean cityDestroyed, BattleResult result) {

    public static BattleDto from(Battle battle) {
        if (battle == null) {
            return null;
        }
        Army attacker = battle.getAttackerArmy();
        Army defender = battle.getDefenderArmy();
        return new BattleDto(battle.getId(), battle.getGame().getId(), battle.getTurnNumber(), battle.getBattleType(),
                attacker.getOwner().getId(), attacker.getId(), battle.getDefenderPlayer().getId(),
                defender == null ? null : defender.getId(), battle.getAttackerSoldiers(), battle.getDefenderSoldiers(),
                battle.getAttackerCasualties(), battle.getDefenderCasualties(), battle.getIsCityDestroyed(),
                battle.getResult());
    }
}
