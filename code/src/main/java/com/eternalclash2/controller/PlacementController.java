package com.eternalclash2.controller;

import com.eternalclash2.service.PlacementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games/{gameId}/placement")
@RequiredArgsConstructor
public class PlacementController {

    private final PlacementService placementService;

    @PostMapping
    public ResponseEntity<Void> selectBase(@PathVariable Long gameId,
                                           @RequestParam Long playerId,
                                           @RequestParam Long cityId) {
        placementService.selectBase(gameId, playerId, cityId);
        return ResponseEntity.ok().build();
    }
}
