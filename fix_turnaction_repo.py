import re

with open('code/src/main/java/com/eternalclash2/repository/TurnActionRepository.java', 'r', encoding='utf-8') as f:
    java = f.read()

java = java.replace('existsByGame_IdAndTurnNumberAndPlayer_Id(Long gameId, Integer turnNumber, Long playerId)', 'existsByGame_IdAndTurnNumberAndCity_Id(Long gameId, Integer turnNumber, Long cityId)')

with open('code/src/main/java/com/eternalclash2/repository/TurnActionRepository.java', 'w', encoding='utf-8') as f:
    f.write(java)
