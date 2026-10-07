package com.eternalclash2.domain.entity;

import com.eternalclash2.domain.enums.ArmyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "armies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Army {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_player_id", nullable = false)
    private Player owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_player_id", nullable = false)
    private Player target;

    @Column(nullable = false)
    private Integer soldiers;

    @Column(name = "departure_turn", nullable = false)
    private Integer departureTurn;

    @Column(name = "arrival_turn", nullable = false)
    private Integer arrivalTurn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ArmyStatus status = ArmyStatus.TRAVELING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}


