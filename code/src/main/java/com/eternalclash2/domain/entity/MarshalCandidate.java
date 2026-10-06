package com.eternalclash2.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "marshal_candidates", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "slot_number"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarshalCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marshal_id", nullable = false)
    private Marshal marshal;

    @Column(name = "slot_number", nullable = false)
    private Integer slotNumber;

    @Column(name = "is_selected", nullable = false)
    @Builder.Default
    private Boolean isSelected = false;
}


