package com.eternalclash2.controller;

import com.eternalclash2.dto.GameDto;
import com.eternalclash2.dto.TurnActionDto;
import com.eternalclash2.dto.TurnActionRequest;
import com.eternalclash2.service.TurnActionService;
import com.eternalclash2.service.TurnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games/{gameId}")
@RequiredArgsConstructor
public class TurnController {

    private final TurnService turnService;
    private final TurnActionService turnActionService;

    // หนึ่ง Turn เลือกได้เพียงหนึ่ง Action: Service จะปฏิเสธเมื่อผู้เล่นนี้ส่งไปแล้ว
    @PostMapping("/players/{playerId}/actions")
    public ResponseEntity<TurnActionDto> submitAction(@PathVariable Long gameId, @PathVariable Long playerId,
                                                      @Valid @RequestBody TurnActionRequest request) {
        return new ResponseEntity<>(TurnActionDto.from(turnActionService.performAction(gameId, playerId,
                request.actionType(), request.targetPlayerId(), request.soldierCount())), HttpStatus.CREATED);
    }

    @GetMapping("/actions")
    public ResponseEntity<List<TurnActionDto>> getActions(@PathVariable Long gameId,
            @RequestParam(name = "turnNumber", required = false) Integer turnNumber) {
        int turn = turnNumber != null ? turnNumber : turnService.getCurrentGame(gameId).getCurrentTurnNumber();
        return ResponseEntity.ok(turnActionService.findTurnActions(gameId, turn).stream()
                .map(TurnActionDto::from).toList());
    }

    @PostMapping("/resolve-turn")
    public ResponseEntity<GameDto> resolveTurn(@PathVariable Long gameId) {
        return ResponseEntity.ok(GameDto.from(turnService.resolveAndAdvance(gameId)));
    }
}
