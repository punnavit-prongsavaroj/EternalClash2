package com.eternalclash2.service;

import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.Season;
import com.eternalclash2.service.GameClock;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {
    private static final int RECRUIT_FOOD_COST = 20;
    private static final double ZHOU_YU_FOOD_MULTIPLIER = 1.25;

    private final CityRepository cityRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    public City produceFood(Long playerId, int turnNumber) {
        Player player = getPlayer(playerId);
        City city = getCity(playerId);
        int production = player.getMarshal() == null ? 20 : player.getMarshal().getFoodProduction();
        if (GameClock.season(turnNumber) == Season.WINTER) production = (int) Math.floor(production / 2.0);
        city.setFood(city.getFood() + production);
        return cityRepository.save(city);
    }

    @Transactional
    public City recruitSoldiers(Long playerId, int turnNumber) {
        Player player = getPlayer(playerId);
        City city = getCity(playerId);
        double multiplier = hasSpecial(player, "NO_ACCIDENT") ? ZHOU_YU_FOOD_MULTIPLIER : 1.0;
        int cost = (int) Math.floor(RECRUIT_FOOD_COST * multiplier);
        if (city.getFood() < cost) throw new BusinessLogicException("Not enough food to recruit soldiers");
        int production = player.getMarshal() == null ? 20 : player.getMarshal().getSoldierProduction();
        if (GameClock.season(turnNumber) == Season.SUMMER) production = Math.max(0, production - 5);
        city.setFood(city.getFood() - cost);
        city.setSoldiers(city.getSoldiers() + production);
        return cityRepository.save(city);
    }

    /** Apply season upkeep to the garrison. Traveling armies already paid their one-time march cost. */
    @Transactional
    public City applySeasonUpkeep(Long playerId) {
        City city = getCity(playerId);
        int availableFood = Math.max(0, city.getFood());
        int availableSoldiers = Math.max(0, city.getSoldiers());
        int casualties = Math.max(0, availableSoldiers - availableFood);
        city.setSoldiers(availableSoldiers - casualties);
        city.setFood(Math.max(0, availableFood - availableSoldiers));
        return cityRepository.save(city);
    }

    @Transactional(readOnly = true)
    public List<City> findAll() { return cityRepository.findAll(); }

    @Transactional(readOnly = true)
    public City findById(Long id) {
        return cityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + id));
    }

    @Transactional
    public City save(City city) { return cityRepository.save(city); }

    private Player getPlayer(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + playerId));
        if (!Boolean.TRUE.equals(player.getIsAlive())) throw new BusinessLogicException("Eliminated players cannot take actions");
        return player;
    }

    private City getCity(Long playerId) {
        return cityRepository.findByPlayer_Id(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found for player: " + playerId));
    }

    private boolean hasSpecial(Player player, String type) {
        return player.getMarshal() != null && type.equals(player.getMarshal().getSpecialAbilityType());
    }
}
