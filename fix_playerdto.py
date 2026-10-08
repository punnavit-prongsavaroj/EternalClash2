import re

with open('code/src/main/java/com/eternalclash2/dto/PlayerDto.java', 'r', encoding='utf-8') as f:
    java = f.read()

java = java.replace(', String cityName, Integer food, Integer soldiers', '')
java = java.replace('''        City city = player.getCity();
        return new PlayerDto(player.getId(), player.getGame().getId(), player.getName(), player.getIsAlive(),
                player.getEliminatedAtTurn(), player.getRerollCount(), MarshalDto.from(player.getMarshal()),
                city == null ? null : city.getName(), city == null ? null : city.getFood(),
                city == null ? null : city.getSoldiers());''', '''        return new PlayerDto(player.getId(), player.getGame().getId(), player.getName(), player.getIsAlive(),
                player.getEliminatedAtTurn(), player.getRerollCount(), MarshalDto.from(player.getMarshal()));''')

with open('code/src/main/java/com/eternalclash2/dto/PlayerDto.java', 'w', encoding='utf-8') as f:
    f.write(java)
