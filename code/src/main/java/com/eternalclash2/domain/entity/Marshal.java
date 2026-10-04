package com.eternalclash2.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "marshals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Marshal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "ability_name", nullable = false, length = 100)
    private String abilityName;

    @Column(name = "ability_description", nullable = false, columnDefinition = "TEXT")
    private String abilityDescription;

    @Column(name = "disadvantage_description", nullable = false, columnDefinition = "TEXT")
    private String disadvantageDescription;

    @Column(name = "food_production", nullable = false)
    private Integer foodProduction = 20;

    @Column(name = "soldier_production", nullable = false)
    private Integer soldierProduction = 20;

    @Column(name = "attack_kill_ratio", nullable = false, precision = 3, scale = 2)
    private Double attackKillRatio = 1.00;

    @Column(name = "reveals_attack_target", nullable = false)
    private Boolean revealsAttackTarget = false;

    @Column(name = "special_ability_type", length = 30)
    private String specialAbilityType;
}


