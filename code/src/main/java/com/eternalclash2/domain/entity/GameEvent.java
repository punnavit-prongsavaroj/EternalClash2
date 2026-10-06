package com.eternalclash2.domain.entity;

import com.eternalclash2.domain.enums.EventType;
import com.eternalclash2.domain.enums.LocationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "game_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "turn_number", nullable = false)
    private Integer turnNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private EventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affected_player_id")
    private Player affectedPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affected_army_id")
    private Army affectedArmy;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false, length = 15)
    private LocationType locationType;

    @Column(name = "food_impact", nullable = false)
    @Builder.Default
    private Integer foodImpact = 0;

    @Column(name = "soldier_impact", nullable = false)
    @Builder.Default
    private Integer soldierImpact = 0;

    @Column(name = "extra_travel_turns", nullable = false)
    @Builder.Default
    private Integer extraTravelTurns = 0;

    @Column(columnDefinition = "TEXT")
    private String description;
}


