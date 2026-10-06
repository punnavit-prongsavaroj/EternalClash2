import re

with open('src/main/java/com/eternalclash2/service/GameViewService.java', 'r', encoding='utf-8') as f:
    java = f.read()

old_logic = '''        List<ActionSnapshot> actions = GameClock.isDaytime(turn)
                ? turnActionRepository.findByGame_IdAndTurnNumber(gameId, turn).stream()
                    .map(action -> actionSnapshot(action, viewerPlayerId, turn)).toList()
                : List.of();'''

new_logic = '''        // ดึง Action ของเทิร์นที่แล้วมาแสดง (ถ้าเทิร์นก่อนหน้าเป็นกลางวัน หรือเป็น Action ของตัวเอง)
        int prevTurn = turn - 1;
        List<ActionSnapshot> actions = prevTurn > 0
                ? turnActionRepository.findByGame_IdAndTurnNumber(gameId, prevTurn).stream()
                    .filter(action -> GameClock.isDaytime(prevTurn) || action.getPlayer().getId().equals(viewerPlayerId))
                    .map(action -> actionSnapshot(action, viewerPlayerId, prevTurn)).toList()
                : List.of();'''

java = java.replace(old_logic, new_logic)

with open('src/main/java/com/eternalclash2/service/GameViewService.java', 'w', encoding='utf-8') as f:
    f.write(java)
