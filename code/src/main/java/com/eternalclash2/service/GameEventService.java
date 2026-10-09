package com.eternalclash2.service;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.GameEvent;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.ArmyStatus;
import com.eternalclash2.domain.enums.EventType;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.domain.enums.LocationType;
import com.eternalclash2.domain.enums.Season;
import com.eternalclash2.service.GameClock;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.ArmyRepository;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.GameEventRepository;
import com.eternalclash2.repository.GameRepository;
import com.eternalclash2.repository.PlayerRepository;
import com.eternalclash2.strategy.marshal.MarshalAbilityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class GameEventService {
    private static final int EVENT_CHANCE_PERCENT = 10;
    private static final int REBELLION_CHANCE_PERCENT = 5;
    
    private final GameEventRepository gameEventRepository;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final CityRepository cityRepository;
    private final ArmyRepository armyRepository;

    @Transactional
    public List<GameEvent> processTurn(Long gameId, int turnNumber) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new ResourceNotFoundException("Game not found: " + gameId));
        if (game.getStatus() != GameStatus.IN_PROGRESS) return List.of();
        Season season = GameClock.season(turnNumber);
        List<GameEvent> events = new ArrayList<>();
        
        List<City> cities = cityRepository.findByGame_Id(gameId);
        
        for (City city : cities) {
            Player player = city.getPlayer();
            if (player == null || !Boolean.TRUE.equals(player.getIsAlive())) continue;
            
            boolean eventOccurred = false;
            MarshalAbilityContext context = new MarshalAbilityContext(player);
            if (context.getStrategy().causesRebellions() && roll(REBELLION_CHANCE_PERCENT)) {
                int foodBefore = city.getFood(), soldiersBefore = city.getSoldiers();
                city.setFood(Math.max(0, foodBefore / 2));
                city.setSoldiers(Math.max(0, soldiersBefore / 2));
                cityRepository.save(city);
                events.add(save(game, turnNumber, EventType.REBELLION, player, null, LocationType.IN_CITY,
                        city.getFood() - foodBefore, city.getSoldiers() - soldiersBefore, 0,
                        "Rebellion halved food and soldiers at " + city.getName() + "."));
                eventOccurred = true;
            }
            if (!eventOccurred && roll(EVENT_CHANCE_PERCENT)) {
                GameEvent event = applyCityEvent(game, city, player, season, turnNumber);
                if (event != null) events.add(event);
            }
        }

        List<Army> armies = armyRepository.findByTargetCity_Game_IdAndStatus(gameId, ArmyStatus.TRAVELING).stream()
                .filter(a -> a.getDepartureTurn() <= turnNumber && a.getSoldiers() > 0).toList();
        for (Army army : armies) {
            if (roll(EVENT_CHANCE_PERCENT)) {
                GameEvent event = applyArmyEvent(game, army, season, turnNumber);
                if (event != null) events.add(event);
            }
        }
        return events;
    }

    private GameEvent applyCityEvent(Game game, City city, Player player, Season season, int turn) {
        List<EventType> choices = new ArrayList<>();
        if (season == Season.RAINY) choices.add(EventType.INSECT_DAMAGE);
        if (season == Season.WINTER) choices.add(EventType.FROSTBITE);
        if (season == Season.RAINY) choices.add(EventType.FLOOD);
        if (season == Season.SUMMER) choices.add(EventType.SUNBURN);
        if (choices.isEmpty()) return null;
        
        EventType type = choices.get(ThreadLocalRandom.current().nextInt(choices.size()));
        
        int foodImpact = 0, soldierImpact = 0;
        switch (type) {
            case INSECT_DAMAGE -> {
                int loss = randomLoss(city.getFood()); city.setFood(city.getFood() - loss); foodImpact = -loss;
            }
            case FROSTBITE, EPIDEMIC -> {
                int loss = randomLoss(city.getSoldiers()); city.setSoldiers(city.getSoldiers() - loss); soldierImpact = -loss;
            }
            case FLOOD -> {
                int foodLoss = randomLoss(city.getFood());
                int soldierLoss = randomLoss(city.getSoldiers());
                city.setFood(city.getFood() - foodLoss); city.setSoldiers(city.getSoldiers() - soldierLoss);
                foodImpact = -foodLoss; soldierImpact = -soldierLoss;
            }
            case SUNBURN -> {
                int loss = randomLoss(city.getFood()); city.setFood(city.getFood() - loss); foodImpact = -loss;
            }
            default -> { return null; }
        }
        cityRepository.save(city);
        return save(game, turn, type, player, null, LocationType.IN_CITY, foodImpact, soldierImpact, 0,
                "A seasonal event affected " + city.getName() + ".");
    }

    private GameEvent applyArmyEvent(Game game, Army army, Season season, int turn) {
        MarshalAbilityContext context = new MarshalAbilityContext(army.getOwner());
        if (context.getStrategy().preventsAccidents()) return null;
        List<EventType> choices = new ArrayList<>();
        choices.add(EventType.SINKHOLE);
        if (season == Season.SUMMER) { choices.add(EventType.SUN_GLARE); choices.add(EventType.SUNBURN); }
        if (season == Season.RAINY) choices.add(EventType.LIGHTNING);
        if (season == Season.WINTER) choices.add(EventType.AVALANCHE);
        if (season == Season.RAINY) choices.add(EventType.FLOOD);
        EventType type = choices.get(ThreadLocalRandom.current().nextInt(choices.size()));
        int soldierLoss = randomLoss(army.getSoldiers());
        army.setSoldiers(army.getSoldiers() - soldierLoss);
        int extraTurns = 0;
        if (type == EventType.SUN_GLARE) { extraTurns = 1; army.setArrivalTurn(army.getArrivalTurn() + extraTurns); }
        if (army.getSoldiers() == 0) army.setStatus(ArmyStatus.CANCELLED);
        armyRepository.save(army);
        return save(game, turn, type, null, army, LocationType.OUTSIDE_CITY, 0, -soldierLoss, extraTurns,
                "A travel event affected the army.");
    }

    private GameEvent save(Game game, int turn, EventType type, Player player, Army army, LocationType location,
                           int foodImpact, int soldierImpact, int extraTurns, String description) {
        return gameEventRepository.save(GameEvent.builder().game(game).turnNumber(turn).eventType(type)
                .affectedPlayer(player).affectedArmy(army).locationType(location).foodImpact(foodImpact)
                .soldierImpact(soldierImpact).extraTravelTurns(extraTurns).description(description).build());
    }

    private int randomLoss(int available) {
        return available <= 0 ? 0 : ThreadLocalRandom.current().nextInt(1, available + 1);
    }

    private boolean roll(int percent) { return ThreadLocalRandom.current().nextInt(100) < percent; }

    private boolean hasSpecial(Player player, String type) {
        return player.getMarshal() != null && type.equals(player.getMarshal().getSpecialAbilityType());
    }

    @Transactional(readOnly = true)
    public List<GameEvent> findAll() { return gameEventRepository.findAll(); }

    @Transactional(readOnly = true)
    public GameEvent findById(Long id) {
        return gameEventRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Game event not found: " + id));
    }
}
