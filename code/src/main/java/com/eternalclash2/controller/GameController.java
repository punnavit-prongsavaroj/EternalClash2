package com.eternalclash2.controller;

import com.eternalclash2.dto.AddPlayerRequest;
import com.eternalclash2.dto.GameDto;
import com.eternalclash2.dto.PlayerDto;
import com.eternalclash2.service.GameService;
import com.eternalclash2.service.GameViewService;
import com.eternalclash2.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final PlayerService playerService;
    private final GameViewService gameViewService;

    @PostMapping
    public ResponseEntity<GameDto> createGame() {
        return new ResponseEntity<>(GameDto.from(gameService.createGame()), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<GameDto>> getAllGames() {
        return ResponseEntity.ok(gameService.findAll().stream().map(GameDto::from).toList());
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameDto> getGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(GameDto.from(gameService.findById(gameId)));
    }

    @GetMapping("/code/{roomCode}")
    public ResponseEntity<GameDto> getGameByCode(@PathVariable String roomCode) {
        return ResponseEntity.ok(GameDto.from(gameService.findByRoomCode(roomCode)));
    }

    @PostMapping("/{gameId}/players")
    public ResponseEntity<PlayerDto> addPlayer(@PathVariable Long gameId,
                                               @Valid @RequestBody AddPlayerRequest request) {
        return new ResponseEntity<>(PlayerDto.from(gameService.addPlayer(gameId, request.name())), HttpStatus.CREATED);
    }

    @GetMapping("/{gameId}/players")
    public ResponseEntity<List<PlayerDto>> getPlayers(@PathVariable Long gameId,
            @RequestParam(name = "aliveOnly", defaultValue = "false") boolean aliveOnly) {
        return ResponseEntity.ok((aliveOnly ? playerService.findAliveByGame(gameId) : playerService.findByGame(gameId))
                .stream().map(PlayerDto::from).toList());
    }

    @PostMapping("/{gameId}/start")
    public ResponseEntity<GameDto> startGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(GameDto.from(gameService.startGame(gameId)));
    }

    // snapshot เป็นมุมมองตามกฎ Hidden Information: ผู้เล่นเห็นเฉพาะข้อมูลของตนและเป้าหมายที่เปิดเผยแล้ว
    @GetMapping("/{gameId}/snapshot")
    public ResponseEntity<GameViewService.GameSnapshot> getSnapshot(@PathVariable Long gameId,
                                                                   @RequestParam(name = "viewerPlayerId") Long viewerPlayerId) {
        return ResponseEntity.ok(gameViewService.getSnapshot(gameId, viewerPlayerId));
    }
}
