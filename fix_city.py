with open('code/src/main/java/com/eternalclash2/domain/entity/City.java', 'r', encoding='utf-8') as f:
    java = f.read()

java = java.replace('@JoinColumn(name = "game_id", nullable = false)', '@JoinColumn(name = "game_id")')
java = java.replace('@Column(nullable = false)\n    private Double x;', '@Column\n    private Double x;')
java = java.replace('@Column(nullable = false)\n    private Double y;', '@Column\n    private Double y;')
java = java.replace('@Column(nullable = false)\n    @Builder.Default\n    private boolean actionUsedThisTurn = false;', '@Column\n    @Builder.Default\n    private Boolean actionUsedThisTurn = false;')

with open('code/src/main/java/com/eternalclash2/domain/entity/City.java', 'w', encoding='utf-8') as f:
    f.write(java)
