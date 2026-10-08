with open('code/src/main/java/com/eternalclash2/service/MarshalDraftService.java', 'r', encoding='utf-8') as f:
    java = f.read()

java = java.replace('game.setStatus(GameStatus.IN_PROGRESS);\n            game.setCurrentTurnNumber(1);', 'game.setStatus(GameStatus.PLACEMENT);')

with open('code/src/main/java/com/eternalclash2/service/MarshalDraftService.java', 'w', encoding='utf-8') as f:
    f.write(java)
