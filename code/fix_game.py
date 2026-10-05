import re

with open('src/main/java/com/eternalclash2/domain/entity/Game.java', 'r', encoding='utf-8') as f:
    java = f.read()

old = '''    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;'''
new = '''    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_code", unique = true, length = 10)
    private String roomCode;'''
java = java.replace(old, new)

with open('src/main/java/com/eternalclash2/domain/entity/Game.java', 'w', encoding='utf-8') as f:
    f.write(java)
