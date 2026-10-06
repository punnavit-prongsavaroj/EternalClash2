package com.eternalclash2.service;

import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;

    @Transactional(readOnly = true)
    public Player findById(Long id) { return getPlayer(id); }

    @Transactional(readOnly = true)
    public List<Player> findByGame(Long gameId) { return playerRepository.findByGame_IdOrderById(gameId); }

    @Transactional(readOnly = true)
    public List<Player> findAliveByGame(Long gameId) {
        return playerRepository.findByGame_IdOrderById(gameId).stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsAlive())).toList();
    }

    private Player getPlayer(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + id));
    }
}
