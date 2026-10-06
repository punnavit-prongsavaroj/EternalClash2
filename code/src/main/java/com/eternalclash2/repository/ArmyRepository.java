package com.eternalclash2.repository;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.enums.ArmyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArmyRepository extends JpaRepository<Army, Long> {
    java.util.List<Army> findByStatusAndArrivalTurnLessThanEqual(ArmyStatus status, Integer arrivalTurn);
    java.util.List<Army> findByOwner_IdAndStatus(Long ownerId, ArmyStatus status);
    java.util.List<Army> findByTarget_Game_IdAndStatus(Long gameId, ArmyStatus status);
}


