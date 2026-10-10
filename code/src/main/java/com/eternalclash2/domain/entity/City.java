package com.eternalclash2.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A game has many cities (nodes)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    // A player can own many cities. If null, it's a neutral city.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @Column(nullable = false, length = 50)
    private String name; // E.g., "City-1" or player's name if captured

    @Column(nullable = false)
    @Builder.Default
    private Integer food = 0; // Neutral cities start with 0 food

    @Column(nullable = false)
    @Builder.Default
    private Integer soldiers = 30; // Neutral cities start with 30 soldiers

    @Column
    private Double x;

    @Column
    private Double y;
    
    @Column
    @Builder.Default
    private Boolean actionUsedThisTurn = false;
}
