package com.eternalclash2.dto;

import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Player;

public record PlayerDto(Long id, Long gameId, String name, Boolean alive, Integer eliminatedAtTurn,
                        Integer rerollCount, MarshalDto marshal) {

    public static PlayerDto from(Player player) {
        if (player == null) {
            return null;
        }
        return new PlayerDto(player.getId(), player.getGame().getId(), player.getName(), player.getIsAlive(),
                player.getEliminatedAtTurn(), player.getRerollCount(), MarshalDto.from(player.getMarshal()));
    }
}
