with open('code/src/main/java/com/eternalclash2/domain/entity/Army.java', 'r', encoding='utf-8') as f:
    java = f.read()
java = java.replace('@JoinColumn(name = "source_city_id", nullable = false)', '@JoinColumn(name = "source_city_id")')
java = java.replace('@JoinColumn(name = "target_city_id", nullable = false)', '@JoinColumn(name = "target_city_id")')
with open('code/src/main/java/com/eternalclash2/domain/entity/Army.java', 'w', encoding='utf-8') as f:
    f.write(java)

with open('code/src/main/java/com/eternalclash2/domain/entity/TurnAction.java', 'r', encoding='utf-8') as f:
    java = f.read()
java = java.replace('@JoinColumn(name = "city_id", nullable = false)', '@JoinColumn(name = "city_id")')
with open('code/src/main/java/com/eternalclash2/domain/entity/TurnAction.java', 'w', encoding='utf-8') as f:
    f.write(java)
