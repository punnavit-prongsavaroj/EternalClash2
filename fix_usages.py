with open('code/src/main/java/com/eternalclash2/service/GameViewService.java', 'r', encoding='utf-8') as f:
    java = f.read()
java = java.replace('city.isActionUsedThisTurn()', 'Boolean.TRUE.equals(city.getActionUsedThisTurn())')
with open('code/src/main/java/com/eternalclash2/service/GameViewService.java', 'w', encoding='utf-8') as f:
    f.write(java)

with open('code/src/main/java/com/eternalclash2/service/TurnService.java', 'r', encoding='utf-8') as f:
    java = f.read()
java = java.replace('City::isActionUsedThisTurn', 'c -> Boolean.TRUE.equals(c.getActionUsedThisTurn())')
with open('code/src/main/java/com/eternalclash2/service/TurnService.java', 'w', encoding='utf-8') as f:
    f.write(java)
