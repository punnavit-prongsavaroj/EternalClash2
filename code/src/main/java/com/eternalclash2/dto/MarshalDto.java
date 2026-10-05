package com.eternalclash2.dto;

import com.eternalclash2.domain.entity.Marshal;

public record MarshalDto(Long id, String name, String abilityName, String abilityDescription,
                         String disadvantageDescription, Integer foodProduction, Integer soldierProduction,
                         Double attackKillRatio, Boolean revealsAttackTarget, String specialAbilityType) {

    public static MarshalDto from(Marshal marshal) {
        if (marshal == null) {
            return null;
        }
        return new MarshalDto(marshal.getId(), marshal.getName(), marshal.getAbilityName(),
                marshal.getAbilityDescription(), marshal.getDisadvantageDescription(),
                marshal.getFoodProduction(), marshal.getSoldierProduction(), marshal.getAttackKillRatio(),
                marshal.getRevealsAttackTarget(), marshal.getSpecialAbilityType());
    }
}
