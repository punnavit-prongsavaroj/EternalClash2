import re

with open('src/main/java/com/eternalclash2/service/GameService.java', 'r', encoding='utf-8') as f:
    java = f.read()

import_old = 'import org.springframework.transaction.annotation.Transactional;'
import_new = 'import org.springframework.transaction.annotation.Transactional;\nimport java.util.UUID;\nimport java.util.Random;'
java = java.replace(import_old, import_new)

create_old = '''    @Transactional
    public Game createGame() {
        return gameRepository.save(Game.builder().status(GameStatus.WAITING).currentTurnNumber(0).build());
    }'''

create_new = '''    @Transactional
    public Game createGame() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 6; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return gameRepository.save(Game.builder().roomCode(sb.toString()).status(GameStatus.WAITING).currentTurnNumber(0).build());
    }
    
    @Transactional(readOnly = true)
    public Game findByRoomCode(String roomCode) {
        return gameRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with room code: " + roomCode));
    }'''

java = java.replace(create_old, create_new)

with open('src/main/java/com/eternalclash2/service/GameService.java', 'w', encoding='utf-8') as f:
    f.write(java)
