import re

with open('src/main/java/com/eternalclash2/repository/GameRepository.java', 'r', encoding='utf-8') as f:
    java = f.read()

old = 'Optional<Game> findByIdForUpdate(@Param("id") Long id);'
new = '''Optional<Game> findByIdForUpdate(@Param("id") Long id);

    Optional<Game> findByRoomCode(String roomCode);'''
java = java.replace(old, new)

with open('src/main/java/com/eternalclash2/repository/GameRepository.java', 'w', encoding='utf-8') as f:
    f.write(java)
