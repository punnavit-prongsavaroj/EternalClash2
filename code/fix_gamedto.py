import re

with open('src/main/java/com/eternalclash2/dto/GameDto.java', 'r', encoding='utf-8') as f:
    java = f.read()

old_sig = 'public record GameDto(Long id, GameStatus status, Integer currentTurnNumber, int playerCount, int alivePlayerCount,'
new_sig = 'public record GameDto(Long id, String roomCode, GameStatus status, Integer currentTurnNumber, int playerCount, int alivePlayerCount,'
java = java.replace(old_sig, new_sig)

old_ret = 'return new GameDto(game.getId(), game.getStatus(), game.getCurrentTurnNumber(), players.size(),'
new_ret = 'return new GameDto(game.getId(), game.getRoomCode(), game.getStatus(), game.getCurrentTurnNumber(), players.size(),'
java = java.replace(old_ret, new_ret)

with open('src/main/java/com/eternalclash2/dto/GameDto.java', 'w', encoding='utf-8') as f:
    f.write(java)
