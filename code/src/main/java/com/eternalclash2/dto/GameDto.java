package com.eternalclash2.dto;

import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.GameStatus;

import java.time.LocalDateTime;
import java.util.List;

public record GameDto(Long id, GameStatus status, Integer currentTurnNumber, int playerCount, int alivePlayerCount,
                      Long winnerId, String winnerName, LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static GameDto from(Game game) {
        if (game == null) {
            return null;
        }
        // Game.builder() ข้ามค่าตั้งต้นของ players (ไม่มี @Builder.Default) → เกมใหม่จึงได้ null
        List<Player> players = game.getPlayers() == null ? List.of() : game.getPlayers();
        Player winner = game.getWinner();
        return new GameDto(game.getId(), game.getStatus(), game.getCurrentTurnNumber(), players.size(),
                (int) players.stream().filter(p -> Boolean.TRUE.equals(p.getIsAlive())).count(),
                winner == null ? null : winner.getId(), winner == null ? null : winner.getName(),
                game.getCreatedAt(), game.getUpdatedAt());
    }
}
