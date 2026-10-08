import re

with open('code/src/main/java/com/eternalclash2/domain/entity/City.java', 'r', encoding='utf-8') as f:
    java = f.read()

new_content = '''package com.eternalclash2.domain.entity;

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
    @JoinColumn(name = "game_id", nullable = false)
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

    @Column(nullable = false)
    private Double x;

    @Column(nullable = false)
    private Double y;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean actionUsedThisTurn = false;
}
'''

with open('code/src/main/java/com/eternalclash2/domain/entity/City.java', 'w', encoding='utf-8') as f:
    f.write(new_content)
print("Refactored City entity")
