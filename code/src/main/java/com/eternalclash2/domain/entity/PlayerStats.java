package com.eternalclash2.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "player_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerStats {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "player_id")
    private Player player;

    @Column(name = "total_food_produced", nullable = false)
    @Builder.Default
    private Integer totalFoodProduced = 0;

    @Column(name = "total_soldiers_recruited", nullable = false)
    @Builder.Default
    private Integer totalSoldiersRecruited = 0;

    @Column(name = "total_battles_fought", nullable = false)
    @Builder.Default
    private Integer totalBattlesFought = 0;

    @Column(name = "cities_conquered", nullable = false)
    @Builder.Default
    private Integer citiesConquered = 0;
}
