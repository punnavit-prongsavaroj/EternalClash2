package com.eternalclash2.controller;

import com.eternalclash2.dto.PlayerDto;
import com.eternalclash2.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping("/{playerId}")
    public ResponseEntity<PlayerDto> getPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok(PlayerDto.from(playerService.findById(playerId)));
    }
}
