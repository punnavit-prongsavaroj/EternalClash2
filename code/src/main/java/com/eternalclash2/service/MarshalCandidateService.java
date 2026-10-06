package com.eternalclash2.service;

import com.eternalclash2.domain.entity.MarshalCandidate;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.MarshalCandidateRepository;
import com.eternalclash2.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarshalCandidateService {
    private final MarshalCandidateRepository candidateRepository;
    private final PlayerRepository playerRepository;
    private final MarshalDraftService draftService;

    @Transactional(readOnly = true)
    public List<MarshalCandidate> findForPlayer(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + playerId));
        List<MarshalCandidate> candidates = candidateRepository.findByPlayer_IdOrderBySlotNumber(playerId);
        if (candidates.isEmpty()) return List.of();
        int currentSlot = Math.min(player.getRerollCount() + 1, candidates.size());
        return List.of(candidates.get(currentSlot - 1));
    }

    @Transactional
    public MarshalCandidate reroll(Long playerId) { return draftService.reroll(playerId); }

    @Transactional
    public com.eternalclash2.domain.entity.Marshal chooseCurrent(Long playerId) {
        return draftService.selectCurrentCandidate(playerId);
    }
}
