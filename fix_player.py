import re

with open('code/src/main/java/com/eternalclash2/domain/entity/Player.java', 'r', encoding='utf-8') as f:
    java = f.read()

java = java.replace('''    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private City city;''', '')

with open('code/src/main/java/com/eternalclash2/domain/entity/Player.java', 'w', encoding='utf-8') as f:
    f.write(java)
print("Updated Player.java")
