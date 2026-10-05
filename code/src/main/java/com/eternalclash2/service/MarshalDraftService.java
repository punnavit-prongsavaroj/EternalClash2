package com.eternalclash2.service;

import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Marshal;
import com.eternalclash2.domain.entity.MarshalCandidate;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.GameRepository;
import com.eternalclash2.repository.MarshalCandidateRepository;
import com.eternalclash2.repository.MarshalRepository;
import com.eternalclash2.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarshalDraftService {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final MarshalRepository marshalRepository;
    private final MarshalCandidateRepository candidateRepository;

    @Transactional
    public List<MarshalCandidate> prepareNextPlayer(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));
        if (game.getStatus() != GameStatus.MARSHAL_SELECTION) {
            throw new BusinessLogicException("Game is not in marshal selection");
        }
        List<Player> players = playerRepository.findByGame_IdOrderById(gameId);
        Player current = players.stream().filter(p -> p.getMarshal() == null).findFirst().orElse(null);
        if (current == null) {
            game.setStatus(GameStatus.IN_PROGRESS);
            game.setCurrentTurnNumber(1);
            gameRepository.save(game);
            return List.of();
        }

        List<MarshalCandidate> existing = candidateRepository.findByPlayer_IdOrderBySlotNumber(current.getId());
        if (!existing.isEmpty()) return existing;

        List<Long> usedMarshalIds = players.stream().filter(p -> p.getMarshal() != null)
                .map(p -> p.getMarshal().getId()).toList();
        List<Marshal> pool = new ArrayList<>(marshalRepository.findAll().stream()
                .filter(m -> !usedMarshalIds.contains(m.getId())).toList());
        Collections.shuffle(pool);
        int offerCount = Math.min(3, pool.size());
        List<MarshalCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < offerCount; i++) {
            candidates.add(candidateRepository.save(MarshalCandidate.builder()
                    .player(current).marshal(pool.get(i)).slotNumber(i + 1).isSelected(false).build()));
        }
        if (candidates.isEmpty()) throw new BusinessLogicException("No marshals are available for this draft");
        return candidates;
    }

    @Transactional
    public MarshalCandidate reroll(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + playerId));
        ensureCurrentDraftPlayer(player);
        List<MarshalCandidate> candidates = candidateRepository.findByPlayer_IdOrderBySlotNumber(playerId);
        int nextSlot = player.getRerollCount() + 2;
        if (nextSlot > candidates.size()) throw new BusinessLogicException("No rerolls remain");
        player.setRerollCount(player.getRerollCount() + 1);
        playerRepository.save(player);
        return candidates.get(nextSlot - 1);
    }

    @Transactional
    public Marshal selectCurrentCandidate(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + playerId));
        ensureCurrentDraftPlayer(player);
        List<MarshalCandidate> candidates = candidateRepository.findByPlayer_IdOrderBySlotNumber(playerId);
        int slot = Math.min(player.getRerollCount() + 1, candidates.size());
        if (candidates.isEmpty()) throw new BusinessLogicException("No marshal candidates are available");
        MarshalCandidate selected = candidates.get(slot - 1);
        selected.setIsSelected(true);
        candidateRepository.save(selected);
        player.setMarshal(selected.getMarshal());
        playerRepository.save(player);

        Game game = player.getGame();
        boolean allSelected = playerRepository.findByGame_IdOrderById(game.getId()).stream()
                .allMatch(p -> p.getMarshal() != null);
        if (allSelected) {
            game.setStatus(GameStatus.IN_PROGRESS);
            game.setCurrentTurnNumber(1);
            gameRepository.save(game);
        } else {
            prepareNextPlayer(game.getId());
        }
        return selected.getMarshal();
    }

    private void ensureCurrentDraftPlayer(Player player) {
        if (player.getGame().getStatus() != GameStatus.MARSHAL_SELECTION) {
            throw new BusinessLogicException("Game is not in marshal selection");
        }
        Player current = playerRepository.findByGame_IdOrderById(player.getGame().getId()).stream()
                .filter(p -> p.getMarshal() == null).findFirst().orElse(null);
        if (current == null || !current.getId().equals(player.getId())) {
            throw new BusinessLogicException("It is not this player's draft turn");
        }
    }
}
