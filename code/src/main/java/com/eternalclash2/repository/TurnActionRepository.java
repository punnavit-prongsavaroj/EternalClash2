package com.eternalclash2.repository;

import com.eternalclash2.domain.entity.TurnAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TurnActionRepository extends JpaRepository<TurnAction, Long> {
    boolean existsByGame_IdAndTurnNumberAndPlayer_Id(Long gameId, Integer turnNumber, Long playerId);
    java.util.List<TurnAction> findByGame_IdAndTurnNumber(Long gameId, Integer turnNumber);
}


