package com.eternalclash2.domain.entity;

import com.eternalclash2.domain.enums.BattleResult;
import com.eternalclash2.domain.enums.BattleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "battles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Battle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "turn_number", nullable = false)
    private Integer turnNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "battle_type", nullable = false, length = 20)
    private BattleType battleType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attacker_army_id", nullable = false)
    private Army attackerArmy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defender_player_id", nullable = false)
    private Player defenderPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defender_army_id")
    private Army defenderArmy;

    @Column(name = "attacker_soldiers", nullable = false)
    private Integer attackerSoldiers;

    @Column(name = "defender_soldiers", nullable = false)
    private Integer defenderSoldiers;

    @Column(name = "attacker_casualties", nullable = false)
    private Integer attackerCasualties;

    @Column(name = "defender_casualties", nullable = false)
    private Integer defenderCasualties;

    @Column(name = "is_city_destroyed", nullable = false)
    @Builder.Default
    private Boolean isCityDestroyed = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BattleResult result;
}


