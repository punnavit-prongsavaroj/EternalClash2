package com.eternalclash2.dto;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.GameEvent;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.EventType;
import com.eternalclash2.domain.enums.LocationType;

public record GameEventDto(Long id, Long gameId, Integer turnNumber, EventType eventType, Long affectedPlayerId,
                           Long affectedArmyId, LocationType locationType, Integer foodImpact, Integer soldierImpact,
                           Integer extraTravelTurns, String description) {

    public static GameEventDto from(GameEvent event) {
        if (event == null) {
            return null;
        }
        Player affectedPlayer = event.getAffectedPlayer();
        Army affectedArmy = event.getAffectedArmy();
        return new GameEventDto(event.getId(), event.getGame().getId(), event.getTurnNumber(), event.getEventType(),
                affectedPlayer == null ? null : affectedPlayer.getId(),
                affectedArmy == null ? null : affectedArmy.getId(), event.getLocationType(), event.getFoodImpact(),
                event.getSoldierImpact(), event.getExtraTravelTurns(), event.getDescription());
    }
}
