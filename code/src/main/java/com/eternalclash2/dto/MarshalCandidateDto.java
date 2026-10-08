package com.eternalclash2.dto;

import com.eternalclash2.domain.entity.MarshalCandidate;

public record MarshalCandidateDto(Long id, Long playerId, Integer slotNumber, Boolean isSelected,
                                  MarshalDto marshal) {

    public static MarshalCandidateDto from(MarshalCandidate candidate) {
        if (candidate == null) {
            return null;
        }
        return new MarshalCandidateDto(candidate.getId(), candidate.getPlayer().getId(), candidate.getSlotNumber(),
                candidate.getIsSelected(), MarshalDto.from(candidate.getMarshal()));
    }
}
