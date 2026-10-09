package com.eternalclash2.service;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.MapEdge;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.ArmyStatus;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.domain.enums.Season;
import com.eternalclash2.service.GameClock;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.ArmyRepository;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.MapEdgeRepository;
import com.eternalclash2.repository.PlayerRepository;
import com.eternalclash2.strategy.marshal.MarshalAbilityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArmyService {
    private static final double MARCH_FOOD_PER_SOLDIER = 1.25;
    
    private final ArmyRepository armyRepository;
    private final PlayerRepository playerRepository;
    private final CityRepository cityRepository;
    private final CityService cityService;
    private final MapEdgeRepository mapEdgeRepository;

    @Transactional
    public Army sendArmy(Long sourceCityId, Long targetCityId, int soldierCount, int turnNumber) {
        City sourceCity = getCity(sourceCityId);
        City targetCity = getCity(targetCityId);
        Player owner = getPlayer(sourceCity.getPlayer().getId());
        
        if (!sourceCity.getGame().getId().equals(targetCity.getGame().getId())) throw new BusinessLogicException("Target must be in the same game");
        if (sourceCity.getGame().getStatus() != GameStatus.IN_PROGRESS) throw new BusinessLogicException("Game is not in progress");
        if (soldierCount <= 0) throw new BusinessLogicException("Army must contain at least one soldier");
        if (sourceCity.getSoldiers() < soldierCount) throw new BusinessLogicException("Not enough soldiers in the city");

        // Verify they are connected
        boolean isConnected = mapEdgeRepository.findByGame_Id(sourceCity.getGame().getId()).stream().anyMatch(e -> 
            (e.getCity1().getId().equals(sourceCityId) && e.getCity2().getId().equals(targetCityId)) ||
            (e.getCity2().getId().equals(sourceCityId) && e.getCity1().getId().equals(targetCityId))
        );
        if (!isConnected) throw new BusinessLogicException("You can only send an army to a directly connected city");

        MarshalAbilityContext context = new MarshalAbilityContext(owner);
        double foodMultiplier = context.getStrategy().getRecruitFoodMultiplier();
        int foodCost = (int) Math.floor(soldierCount * MARCH_FOOD_PER_SOLDIER * foodMultiplier);
        
        cityService.deductFoodFromNetwork(sourceCity, foodCost);

        int travelTurns = 3;
        if (context.getStrategy().travelsFaster()) travelTurns--;
        if (GameClock.season(turnNumber) == Season.RAINY) travelTurns++;
        travelTurns = Math.max(1, travelTurns);

        sourceCity.setSoldiers(sourceCity.getSoldiers() - soldierCount);
        sourceCity.setActionUsedThisTurn(true);
        cityRepository.save(sourceCity);
        
        return armyRepository.save(Army.builder().owner(owner).sourceCity(sourceCity).targetCity(targetCity).soldiers(soldierCount)
                .departureTurn(turnNumber).arrivalTurn(turnNumber + travelTurns).status(ArmyStatus.TRAVELING).build());
    }

    @Transactional
    public void cancelIfDestroyed(Army army) {
        if (army.getSoldiers() <= 0 && army.getStatus() == ArmyStatus.TRAVELING) {
            army.setSoldiers(0);
            army.setStatus(ArmyStatus.CANCELLED);
            armyRepository.save(army);
        }
    }

    @Transactional(readOnly = true)
    public List<Army> findDueArmies(Long gameId, int turnNumber) {
        return armyRepository.findByTargetCity_Game_IdAndStatus(gameId, ArmyStatus.TRAVELING).stream()
                .filter(a -> a.getArrivalTurn() <= turnNumber).toList();
    }

    @Transactional(readOnly = true)
    public List<Army> findByOwner(Long ownerId) {
        return armyRepository.findByOwner_IdAndStatus(ownerId, ArmyStatus.TRAVELING);
    }

    @Transactional(readOnly = true)
    public List<Army> findAll() { return armyRepository.findAll(); }

    @Transactional(readOnly = true)
    public Army findById(Long id) {
        return armyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Army not found with id: " + id));
    }

    @Transactional
    public Army save(Army army) { return armyRepository.save(army); }

    private Player getPlayer(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + playerId));
        if (!Boolean.TRUE.equals(player.getIsAlive())) throw new BusinessLogicException("Eliminated players cannot command armies");
        return player;
    }
    
    private City getCity(Long cityId) {
        return cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + cityId));
    }
}
