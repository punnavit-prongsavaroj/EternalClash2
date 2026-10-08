package com.eternalclash2.service;

import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.MapEdge;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.Season;
import com.eternalclash2.service.GameClock;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.MapEdgeRepository;
import com.eternalclash2.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CityService {
    private static final int RECRUIT_FOOD_COST = 20;
    private static final double ZHOU_YU_FOOD_MULTIPLIER = 1.25;

    private final CityRepository cityRepository;
    private final PlayerRepository playerRepository;
    private final MapEdgeRepository mapEdgeRepository;

    @Transactional
    public City produceFood(Long cityId, int turnNumber) {
        City city = getCity(cityId);
        Player player = getPlayer(city.getPlayer().getId());
        
        int production = player.getMarshal() == null ? 20 : player.getMarshal().getFoodProduction();
        if (GameClock.season(turnNumber) == Season.WINTER) production = (int) Math.floor(production / 2.0);
        
        city.setFood(city.getFood() + production);
        city.setActionUsedThisTurn(true);
        return cityRepository.save(city);
    }

    @Transactional
    public City recruitSoldiers(Long cityId, int turnNumber) {
        City city = getCity(cityId);
        Player player = getPlayer(city.getPlayer().getId());
        
        double multiplier = hasSpecial(player, "NO_ACCIDENT") ? ZHOU_YU_FOOD_MULTIPLIER : 1.0;
        int totalCost = (int) Math.floor(RECRUIT_FOOD_COST * multiplier);
        
        deductFoodFromNetwork(city, totalCost);
        
        int production = player.getMarshal() == null ? 20 : player.getMarshal().getSoldierProduction();
        if (GameClock.season(turnNumber) == Season.SUMMER) production = Math.max(0, production - 5);
        
        city.setSoldiers(city.getSoldiers() + production);
        city.setActionUsedThisTurn(true);
        return cityRepository.save(city);
    }

    @Transactional
    public void deductFoodFromNetwork(City sourceCity, int totalCost) {
        List<City> network = getConnectedNetwork(sourceCity);
        
        int totalAvailable = network.stream().mapToInt(City::getFood).sum();
        if (totalAvailable < totalCost) {
            throw new BusinessLogicException("Not enough food in the connected network. Need " + totalCost + " but have " + totalAvailable);
        }
        
        int perCityCost = totalCost / network.size();
        int remainder = totalCost % network.size();
        
        // Wait, if a city in the network doesn't have enough to pay its share (perCityCost), the user said:
        // "หารกันจ่าย ... หากมีเมืองที่ไม่ติดก็จะต้องใช้ของเมืองตัวเอง"
        // What if city A has 100 food, city B has 0 food. They are connected. Cost is 50.
        // If we naively deduct 25 from A and 25 from B, B will have -25 food. 
        // We should first try to deduct fairly, but if some cities don't have enough, the others must cover it!
        // To keep it simple: pool all food, distribute remaining food fairly? Or just deduct the exact amount required.
        // Let's do a greedy deduction: first deduct the fair share. If someone goes negative, distribute debt to others.
        
        int remainingCost = totalCost;
        
        // Attempt fair deduction first
        for (City c : network) {
            int toDeduct = perCityCost + (c.getId().equals(sourceCity.getId()) ? remainder : 0);
            c.setFood(c.getFood() - toDeduct);
        }
        
        // Resolve negative balances
        while (network.stream().anyMatch(c -> c.getFood() < 0)) {
            int debt = 0;
            for (City c : network) {
                if (c.getFood() < 0) {
                    debt += -c.getFood();
                    c.setFood(0);
                }
            }
            
            // Distribute debt among those who still have food
            List<City> solventCities = network.stream().filter(c -> c.getFood() > 0).toList();
            if (solventCities.isEmpty()) break; // Should not happen due to initial total check
            
            int splitDebt = debt / solventCities.size();
            int debtRemainder = debt % solventCities.size();
            
            for (int i = 0; i < solventCities.size(); i++) {
                City c = solventCities.get(i);
                int toDeduct = splitDebt + (i == 0 ? debtRemainder : 0); // Give remainder to the first solvent city
                c.setFood(c.getFood() - toDeduct);
            }
        }
        
        cityRepository.saveAll(network);
    }
    
    public List<City> getConnectedNetwork(City sourceCity) {
        if (sourceCity.getPlayer() == null) return List.of(sourceCity);
        
        Long gameId = sourceCity.getGame().getId();
        Long playerId = sourceCity.getPlayer().getId();
        
        List<MapEdge> edges = mapEdgeRepository.findByGame_Id(gameId);
        
        // Build adjacency list for this player's cities
        Map<Long, List<City>> adj = new HashMap<>();
        for (MapEdge edge : edges) {
            City c1 = edge.getCity1();
            City c2 = edge.getCity2();
            
            if (c1.getPlayer() != null && c1.getPlayer().getId().equals(playerId) &&
                c2.getPlayer() != null && c2.getPlayer().getId().equals(playerId)) {
                
                adj.computeIfAbsent(c1.getId(), k -> new ArrayList<>()).add(c2);
                adj.computeIfAbsent(c2.getId(), k -> new ArrayList<>()).add(c1);
            }
        }
        
        // BFS
        List<City> network = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Queue<City> queue = new LinkedList<>();
        
        queue.add(sourceCity);
        visited.add(sourceCity.getId());
        
        while (!queue.isEmpty()) {
            City current = queue.poll();
            network.add(current);
            
            List<City> neighbors = adj.getOrDefault(current.getId(), Collections.emptyList());
            for (City n : neighbors) {
                if (!visited.contains(n.getId())) {
                    visited.add(n.getId());
                    queue.add(n);
                }
            }
        }
        
        return network;
    }

    @Transactional
    public void applySeasonUpkeep(Long gameId) {
        // Upkeep applies to all cities
        List<City> cities = cityRepository.findByGame_Id(gameId);
        for (City city : cities) {
            if (city.getPlayer() == null) continue; // Neutral cities don't starve
            
            int availableFood = Math.max(0, city.getFood());
            int availableSoldiers = Math.max(0, city.getSoldiers());
            int casualties = Math.max(0, availableSoldiers - availableFood);
            city.setSoldiers(availableSoldiers - casualties);
            city.setFood(Math.max(0, availableFood - availableSoldiers));
        }
        cityRepository.saveAll(cities);
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

    private City getCity(Long cityId) {
        return cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found: " + cityId));
    }

    private boolean hasSpecial(Player player, String type) {
        return player.getMarshal() != null && type.equals(player.getMarshal().getSpecialAbilityType());
    }
}
