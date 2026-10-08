import re

with open('code/src/main/java/com/eternalclash2/command/ProduceFoodCommand.java', 'r', encoding='utf-8') as f:
    java = f.read()

java = java.replace('private final Long playerId;', 'private final Long cityId;')
java = java.replace('public ProduceFoodCommand(CityService cityService, Long playerId', 'public ProduceFoodCommand(CityService cityService, Long cityId')
java = java.replace('this.playerId = playerId;', 'this.cityId = cityId;')
java = java.replace('cityService.produceFood(playerId', 'cityService.produceFood(cityId')

with open('code/src/main/java/com/eternalclash2/command/ProduceFoodCommand.java', 'w', encoding='utf-8') as f:
    f.write(java)
    
with open('code/src/main/java/com/eternalclash2/command/RecruitSoldiersCommand.java', 'r', encoding='utf-8') as f:
    java = f.read()

java = java.replace('private final Long playerId;', 'private final Long cityId;')
java = java.replace('public RecruitSoldiersCommand(CityService cityService, Long playerId', 'public RecruitSoldiersCommand(CityService cityService, Long cityId')
java = java.replace('this.playerId = playerId;', 'this.cityId = cityId;')
java = java.replace('cityService.recruitSoldiers(playerId', 'cityService.recruitSoldiers(cityId')

with open('code/src/main/java/com/eternalclash2/command/RecruitSoldiersCommand.java', 'w', encoding='utf-8') as f:
    f.write(java)
