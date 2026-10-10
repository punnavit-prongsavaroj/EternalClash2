package com.eternalclash2.repository;

import com.eternalclash2.domain.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from Game g where g.id = :id")
    Optional<Game> findByIdForUpdate(@Param("id") Long id);

    Optional<Game> findByRoomCode(String roomCode);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"players"})
    org.springframework.data.domain.Page<Game> findAll(org.springframework.data.domain.Pageable pageable);

    java.util.List<Game> findByUpdatedAtBefore(java.time.LocalDateTime date);
}


