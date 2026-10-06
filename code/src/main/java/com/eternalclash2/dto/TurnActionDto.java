package com.eternalclash2.dto;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.TurnAction;
import com.eternalclash2.domain.enums.ActionType;

public record TurnActionDto(Long id, Long gameId, Integer turnNumber, Long playerId, String playerName,
                            ActionType actionType, Long armyId, Integer foodBefore, Integer foodAfter,
                            Integer soldiersBefore, Integer soldiersAfter) {

    public static TurnActionDto from(TurnAction action) {
        if (action == null) {
            return null;
        }
        Army army = action.getArmy();
        return new TurnActionDto(action.getId(), action.getGame().getId(), action.getTurnNumber(),
                action.getPlayer().getId(), action.getPlayer().getName(), action.getActionType(),
                army == null ? null : army.getId(), action.getFoodBefore(), action.getFoodAfter(),
                action.getSoldiersBefore(), action.getSoldiersAfter());
    }
}
