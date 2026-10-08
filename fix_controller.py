import re

with open('code/src/main/java/com/eternalclash2/dto/TurnActionRequest.java', 'r', encoding='utf-8') as f:
    java = f.read()

java = java.replace('Long targetPlayerId', 'Long cityId, Long targetCityId')
java = java.replace('ActionType actionType,', 'ActionType actionType, @NotNull(message = "cityId is required")')

with open('code/src/main/java/com/eternalclash2/dto/TurnActionRequest.java', 'w', encoding='utf-8') as f:
    f.write(java)
    
with open('code/src/main/java/com/eternalclash2/controller/TurnController.java', 'r', encoding='utf-8') as f:
    java = f.read()
    
java = java.replace('request.actionType(), request.targetPlayerId(), request.soldierCount()', 'request.cityId(), request.actionType(), request.targetCityId(), request.soldierCount()')

with open('code/src/main/java/com/eternalclash2/controller/TurnController.java', 'w', encoding='utf-8') as f:
    f.write(java)
