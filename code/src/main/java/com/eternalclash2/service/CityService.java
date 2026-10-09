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
import com.eternalclash2.repository.GameEventRepository;
import com.eternalclash2.domain.entity.GameEvent;
import com.eternalclash2.domain.enums.EventType;
import com.eternalclash2.domain.enums.LocationType;
import com.eternalclash2.strategy.marshal.MarshalAbilityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityService {
    private static final int RECRUIT_FOOD_COST = 20;

    private final CityRepository cityRepository;
    private final PlayerRepository playerRepository;
    private final PlayerService playerService;
    private final MapEdgeRepository mapEdgeRepository;
    private final GameEventRepository gameEventRepository;

    @Transactional
    public City produceFood(Long cityId, int turnNumber) {
        City city = getCity(cityId);
        Player player = playerService.getAlivePlayerValidated(city.getPlayer().getId());
        
        int production = player.getMarshal() == null ? 20 : player.getMarshal().getFoodProduction();
        if (GameClock.season(turnNumber) == Season.WINTER) production = (int) Math.floor(production / 2.0);
        
        city.setFood(city.getFood() + production);
        city.setActionUsedThisTurn(true);

        if (player.getPlayerStats() != null) {
            player.getPlayerStats().setTotalFoodProduced(player.getPlayerStats().getTotalFoodProduced() + production);
            playerRepository.save(player);
        }

        return cityRepository.save(city);
    }

    @Transactional
    public City recruitSoldiers(Long cityId, int turnNumber) {
        City city = getCity(cityId);
        Player player = playerService.getAlivePlayerValidated(city.getPlayer().getId());
        
        MarshalAbilityContext context = new MarshalAbilityContext(player);
        double multiplier = context.getStrategy().getRecruitFoodMultiplier();
        int totalCost = (int) Math.floor(RECRUIT_FOOD_COST * multiplier);
        
        deductFoodFromNetwork(city, totalCost);
        
        int production = player.getMarshal() == null ? 20 : player.getMarshal().getSoldierProduction();
        if (GameClock.season(turnNumber) == Season.SUMMER) production = Math.max(0, production - 5);
        
        city.setSoldiers(city.getSoldiers() + production);
        city.setActionUsedThisTurn(true);

        if (player.getPlayerStats() != null) {
            player.getPlayerStats().setTotalSoldiersRecruited(player.getPlayerStats().getTotalSoldiersRecruited() + production);
            playerRepository.save(player);
        }

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
        List<City> cities = cityRepository.findByGame_Id(gameId);
        Set<Long> processedNetworkIds = new HashSet<>();
        
        for (City city : cities) {
            if (city.getPlayer() == null) continue; // Neutral cities don't starve
            if (processedNetworkIds.contains(city.getId())) continue;
            
            List<City> network = getConnectedNetwork(city);
            network.forEach(c -> processedNetworkIds.add(c.getId()));
            
            int totalFood = network.stream().mapToInt(City::getFood).sum();
            int totalSoldiers = network.stream().mapToInt(City::getSoldiers).sum();
            
            if (totalFood >= totalSoldiers) {
                // Enough food in the network
                int perCityCost = totalSoldiers / network.size();
                int remainder = totalSoldiers % network.size();
                
                // Attempt fair deduction first
                for (City c : network) {
                    int toDeduct = perCityCost + (c.getId().equals(city.getId()) ? remainder : 0);
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
                    
                    List<City> solventCities = network.stream().filter(c -> c.getFood() > 0).toList();
                    if (solventCities.isEmpty()) break;
                    
                    int splitDebt = debt / solventCities.size();
                    int debtRemainder = debt % solventCities.size();
                    
                    for (int i = 0; i < solventCities.size(); i++) {
                        City c = solventCities.get(i);
                        int toDeduct = splitDebt + (i == 0 ? debtRemainder : 0);
                        c.setFood(c.getFood() - toDeduct);
                    }
                }
            } else {
                // Not enough food: distribute the total excess food to the cities with deficit proportionally
                Map<Long, Integer> initialSoldiers = network.stream().collect(Collectors.toMap(City::getId, City::getSoldiers));
                Map<Long, Integer> initialFood = network.stream().collect(Collectors.toMap(City::getId, City::getFood));
                
                int totalExcess = 0;
                int totalDeficit = 0;
                List<City> deficitCities = new ArrayList<>();
                
                for (City c : network) {
                    int s = c.getSoldiers();
                    int f = c.getFood();
                    if (f >= s) {
                        totalExcess += (f - s);
                    } else {
                        totalDeficit += (s - f);
                        deficitCities.add(c);
                    }
                    c.setFood(0); // All food will be consumed
                }
                
                int remainingExcess = totalExcess;
                for (int i = 0; i < deficitCities.size(); i++) {
                    City c = deficitCities.get(i);
                    int deficit = initialSoldiers.get(c.getId()) - initialFood.get(c.getId());
                    int share = (int) Math.round((double) deficit / totalDeficit * totalExcess);
                    if (i == deficitCities.size() - 1) {
                        share = remainingExcess;
                    }
                    share = Math.min(share, remainingExcess);
                    remainingExcess -= share;
                    
                    int casualties = deficit - share;
                    c.setSoldiers(Math.max(0, c.getSoldiers() - casualties));
                }
                
                // Log starvation events
                for (City c : network) {
                    int lost = initialSoldiers.get(c.getId()) - c.getSoldiers();
                    if (lost > 0) {
                        gameEventRepository.save(GameEvent.builder()
                            .game(c.getGame())
                            .turnNumber(c.getGame().getCurrentTurnNumber())
                            .eventType(EventType.STARVATION)
                            .affectedPlayer(c.getPlayer())
                            .locationType(LocationType.IN_CITY)
                            .soldierImpact(-lost)
                            .description("Starvation! " + lost + " soldiers starved at " + c.getName() + " due to lack of food.")
                            .build());
                    }
                }
            }
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

    private City getCity(Long cityId) {
        return cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found: " + cityId));
    }
}
