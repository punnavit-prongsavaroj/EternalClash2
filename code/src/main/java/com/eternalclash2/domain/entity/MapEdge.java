package com.eternalclash2.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "map_edges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MapEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city1_id", nullable = false)
    private City city1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city2_id", nullable = false)
    private City city2;
}
