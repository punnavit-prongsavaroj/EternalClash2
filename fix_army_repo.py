import re

with open('code/src/main/java/com/eternalclash2/repository/ArmyRepository.java', 'r', encoding='utf-8') as f:
    java = f.read()

java = java.replace('findByTarget_Game_IdAndStatus', 'findByTargetCity_Game_IdAndStatus')

with open('code/src/main/java/com/eternalclash2/repository/ArmyRepository.java', 'w', encoding='utf-8') as f:
    f.write(java)
