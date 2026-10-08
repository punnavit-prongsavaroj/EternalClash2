import re

with open('code/src/main/java/com/eternalclash2/domain/entity/Player.java', 'r', encoding='utf-8') as f:
    java = f.read()

new_field = '''
    @Column(name = "starting_city_id")
    private Long startingCityId;
'''

java = java.replace('private LocalDateTime createdAt;', 'private LocalDateTime createdAt;' + new_field)

with open('code/src/main/java/com/eternalclash2/domain/entity/Player.java', 'w', encoding='utf-8') as f:
    f.write(java)
