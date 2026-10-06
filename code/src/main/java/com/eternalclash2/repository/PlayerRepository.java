package com.eternalclash2.repository;

import com.eternalclash2.domain.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByGameId(Long gameId);
    List<Player> findByGame_IdOrderById(Long gameId);
    long countByGame_Id(Long gameId);
}
