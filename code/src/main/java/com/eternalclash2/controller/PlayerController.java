package com.eternalclash2.controller;

import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.repository.PlayerRepository;
import com.eternalclash2.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerRepository playerRepository;

    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers() {
        return ResponseEntity.ok(playerRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + id));
        return ResponseEntity.ok(player);
    }

    @PostMapping
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        return new ResponseEntity<>(playerRepository.save(player), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable Long id, @RequestBody Player playerDetails) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + id));

        if (playerDetails.getName() != null) {
            player.setName(playerDetails.getName());
        }
        if (playerDetails.getIsAlive() != null) {
            player.setIsAlive(playerDetails.getIsAlive());
        }
        if (playerDetails.getRerollCount() != null) {
            player.setRerollCount(playerDetails.getRerollCount());
        }
        
        return ResponseEntity.ok(playerRepository.save(player));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + id));
        
        playerRepository.delete(player);
        return ResponseEntity.noContent().build();
    }
}
