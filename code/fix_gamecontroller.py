import re

with open('src/main/java/com/eternalclash2/controller/GameController.java', 'r', encoding='utf-8') as f:
    java = f.read()

new_endpoint = '''    @GetMapping("/{gameId}")
    public ResponseEntity<GameDto> getGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(GameDto.from(gameService.findById(gameId)));
    }

    @GetMapping("/code/{roomCode}")
    public ResponseEntity<GameDto> getGameByCode(@PathVariable String roomCode) {
        return ResponseEntity.ok(GameDto.from(gameService.findByRoomCode(roomCode)));
    }'''

java = java.replace('''    @GetMapping("/{gameId}")
    public ResponseEntity<GameDto> getGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(GameDto.from(gameService.findById(gameId)));
    }''', new_endpoint)

with open('src/main/java/com/eternalclash2/controller/GameController.java', 'w', encoding='utf-8') as f:
    f.write(java)
