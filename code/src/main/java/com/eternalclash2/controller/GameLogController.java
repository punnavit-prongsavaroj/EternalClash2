package com.eternalclash2.controller;

import com.eternalclash2.dto.BattleDto;
import com.eternalclash2.dto.GameEventDto;
import com.eternalclash2.service.BattleService;
import com.eternalclash2.service.GameEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games/{gameId}")
@RequiredArgsConstructor
public class GameLogController {

    private final BattleService battleService;
    private final GameEventService gameEventService;

    @GetMapping("/battles")
    public ResponseEntity<List<BattleDto>> getBattles(@PathVariable Long gameId,
            @RequestParam(name = "turnNumber", required = false) Integer turnNumber) {
        return ResponseEntity.ok(battleService.findAll().stream()
                .filter(battle -> battle.getGame().getId().equals(gameId))
                .filter(battle -> turnNumber == null || turnNumber.equals(battle.getTurnNumber()))
                .map(BattleDto::from).toList());
    }

    @GetMapping("/events")
    public ResponseEntity<List<GameEventDto>> getEvents(@PathVariable Long gameId,
            @RequestParam(name = "turnNumber", required = false) Integer turnNumber) {
        return ResponseEntity.ok(gameEventService.findAll().stream()
                .filter(event -> event.getGame().getId().equals(gameId))
                .filter(event -> turnNumber == null || turnNumber.equals(event.getTurnNumber()))
                .map(GameEventDto::from).toList());
    }
}
