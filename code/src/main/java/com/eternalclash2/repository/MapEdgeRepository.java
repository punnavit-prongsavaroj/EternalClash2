package com.eternalclash2.repository;

import com.eternalclash2.domain.entity.MapEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MapEdgeRepository extends JpaRepository<MapEdge, Long> {
    List<MapEdge> findByGame_Id(Long gameId);
}
