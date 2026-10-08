import re

with open('code/src/main/java/com/eternalclash2/domain/entity/TurnAction.java', 'r', encoding='utf-8') as f:
    java = f.read()

# Update unique constraint
java = java.replace('"game_id", "turn_number", "player_id"', '"game_id", "turn_number", "city_id"')

# Add City field
java = java.replace('''    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;''', '''    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;''')

with open('code/src/main/java/com/eternalclash2/domain/entity/TurnAction.java', 'w', encoding='utf-8') as f:
    f.write(java)
print("Updated TurnAction.java")
