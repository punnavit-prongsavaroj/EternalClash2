package com.eternalclash2.service;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.ArmyStatus;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.domain.enums.Season;
import com.eternalclash2.domain.service.GameClock;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.ArmyRepository;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArmyService {
    private static final double MARCH_FOOD_PER_SOLDIER = 1.25;
    private static final double ZHOU_YU_FOOD_MULTIPLIER = 1.25;
    private final ArmyRepository armyRepository;
    private final PlayerRepository playerRepository;
    private final CityRepository cityRepository;

    @Transactional
    public Army sendArmy(Long ownerId, Long targetId, int soldierCount, int turnNumber) {
        Player owner = getPlayer(ownerId);
        Player target = getPlayer(targetId);
        if (!owner.getGame().getId().equals(target.getGame().getId())) throw new BusinessLogicException("Target must be in the same game");
        if (ownerId.equals(targetId)) throw new BusinessLogicException("A player cannot attack their own city");
        if (owner.getGame().getStatus() != GameStatus.IN_PROGRESS) throw new BusinessLogicException("Game is not in progress");
        if (soldierCount <= 0) throw new BusinessLogicException("Army must contain at least one soldier");

        City city = cityRepository.findByPlayer_Id(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found for player: " + ownerId));
        if (city.getSoldiers() < soldierCount) throw new BusinessLogicException("Not enough soldiers in the city");
        double foodMultiplier = hasSpecial(owner, "NO_ACCIDENT") ? ZHOU_YU_FOOD_MULTIPLIER : 1.0;
        int foodCost = (int) Math.floor(soldierCount * MARCH_FOOD_PER_SOLDIER * foodMultiplier);
        if (city.getFood() < foodCost) throw new BusinessLogicException("Not enough food to send the army");

        int travelTurns = 3;
        if (hasSpecial(owner, "FAST_MARCH")) travelTurns--;
        if (GameClock.season(turnNumber) == Season.RAINY) travelTurns++;
        travelTurns = Math.max(1, travelTurns);

        city.setSoldiers(city.getSoldiers() - soldierCount);
        city.setFood(city.getFood() - foodCost);
        cityRepository.save(city);
        return armyRepository.save(Army.builder().owner(owner).target(target).soldiers(soldierCount)
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
        return armyRepository.findByTarget_Game_IdAndStatus(gameId, ArmyStatus.TRAVELING).stream()
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

    private boolean hasSpecial(Player player, String type) {
        return player.getMarshal() != null && type.equals(player.getMarshal().getSpecialAbilityType());
    }
}
