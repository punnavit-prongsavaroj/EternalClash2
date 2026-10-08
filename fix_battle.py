with open('code/src/main/java/com/eternalclash2/domain/entity/Battle.java', 'r', encoding='utf-8') as f:
    java = f.read()
java = java.replace('@Column(name = "is_city_destroyed", nullable = false)', '@Column(name = "is_city_destroyed")')
with open('code/src/main/java/com/eternalclash2/domain/entity/Battle.java', 'w', encoding='utf-8') as f:
    f.write(java)
