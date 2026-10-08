import re

with open('code/src/main/java/com/eternalclash2/domain/entity/Army.java', 'r', encoding='utf-8') as f:
    java = f.read()

java = java.replace('''    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_player_id", nullable = false)
    private Player target;''', '''    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_city_id", nullable = false)
    private City sourceCity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_city_id", nullable = false)
    private City targetCity;''')

with open('code/src/main/java/com/eternalclash2/domain/entity/Army.java', 'w', encoding='utf-8') as f:
    f.write(java)
print("Updated Army.java")
