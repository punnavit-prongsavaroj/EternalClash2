package com.eternalclash2.domain.entity;

import com.eternalclash2.domain.enums.ActionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "turn_actions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "turn_number", "player_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "turn_number", nullable = false)
    private Integer turnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20)
    private ActionType actionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "army_id")
    private Army army;

    @Column(name = "food_before", nullable = false)
    private Integer foodBefore;

    @Column(name = "food_after", nullable = false)
    private Integer foodAfter;

    @Column(name = "soldiers_before", nullable = false)
    private Integer soldiersBefore;

    @Column(name = "soldiers_after", nullable = false)
    private Integer soldiersAfter;
}


