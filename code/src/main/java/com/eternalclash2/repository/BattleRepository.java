package com.eternalclash2.repository;

import com.eternalclash2.domain.entity.Battle;
import com.eternalclash2.domain.enums.BattleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BattleRepository extends JpaRepository<Battle, Long> {
    boolean existsByBattleTypeAndAttackerArmy_IdAndDefenderArmy_Id(BattleType type, Long attackerId, Long defenderId);
    java.util.List<Battle> findByGame_IdAndTurnNumberAndDefenderPlayer_IdAndBattleType(Long gameId, Integer turnNumber, Long defenderId, BattleType type);
}


