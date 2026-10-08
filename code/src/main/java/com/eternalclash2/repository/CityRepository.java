package com.eternalclash2.repository;

import com.eternalclash2.domain.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByPlayer_Id(Long playerId);
    List<City> findByGame_Id(Long gameId);
}
