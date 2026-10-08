import re

with open('code/src/main/java/com/eternalclash2/service/GameService.java', 'r', encoding='utf-8') as f:
    java = f.read()

new_content = '''package com.eternalclash2.service;

import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.MapEdge;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.MapEdgeRepository;
import com.eternalclash2.repository.GameRepository;
import com.eternalclash2.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class GameService {
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 7;

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final CityRepository cityRepository;
    private final MapEdgeRepository mapEdgeRepository;
    private final MarshalService marshalService;
    private final MarshalDraftService marshalDraftService;

    @Transactional
    public Game createGame() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 6; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return gameRepository.save(Game.builder().roomCode(sb.toString()).status(GameStatus.WAITING).currentTurnNumber(0).build());
    }
    
    @Transactional(readOnly = true)
    public Game findByRoomCode(String roomCode) {
        return gameRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with room code: " + roomCode));
    }

    @Transactional
    public Player addPlayer(Long gameId, String playerName) {
        Game game = getGame(gameId);
        if (game.getStatus() != GameStatus.WAITING) throw new BusinessLogicException("Game is no longer accepting players");
        if (playerName == null || playerName.isBlank()) throw new BusinessLogicException("Player name is required");
        if (playerRepository.countByGame_Id(gameId) >= MAX_PLAYERS) throw new BusinessLogicException("A game can have at most 7 players");
        Player player = playerRepository.save(Player.builder().game(game).name(playerName.trim()).isAlive(true).rerollCount(0).build());
        return playerRepository.save(player);
    }

    @Transactional
    public Game startGame(Long gameId) {
        Game game = getGame(gameId);
        if (game.getStatus() != GameStatus.WAITING) throw new BusinessLogicException("Game has already started");
        long players = playerRepository.countByGame_Id(gameId);
        if (players < MIN_PLAYERS || players > MAX_PLAYERS) {
            throw new BusinessLogicException("A game requires between 2 and 7 players");
        }
        if (marshalService.ensureDefaultMarshals().size() < players) throw new BusinessLogicException("Not enough marshal records are configured");
        
        generateMap(game, (int) players);
        
        game.setStatus(GameStatus.MARSHAL_SELECTION);
        game.setCurrentTurnNumber(0);
        gameRepository.save(game);
        marshalDraftService.prepareNextPlayer(gameId);
        return game;
    }
    
    private void generateMap(Game game, int playerCount) {
        int nodeCount = playerCount * 3;
        List<City> nodes = new ArrayList<>();
        Random rand = new Random();
        
        // 1. Create nodes with random positions (using a simple grid-like logic to spread them out, or just random)
        for (int i = 0; i < nodeCount; i++) {
            City city = City.builder()
                .game(game)
                .name("Node " + (i + 1))
                .food(0)
                .soldiers(30)
                .x(10.0 + rand.nextDouble() * 80.0) // 10% to 90% width
                .y(10.0 + rand.nextDouble() * 80.0) // 10% to 90% height
                .build();
            nodes.add(cityRepository.save(city));
        }
        
        // 2. Create connections (Cycle for guaranteed connectivity)
        int[] degrees = new int[nodeCount];
        List<MapEdge> edges = new ArrayList<>();
        
        for (int i = 0; i < nodeCount; i++) {
            int next = (i + 1) % nodeCount;
            edges.add(MapEdge.builder().game(game).city1(nodes.get(i)).city2(nodes.get(next)).build());
            degrees[i]++;
            degrees[next]++;
        }
        
        // 3. Add random cross connections
        for (int i = 0; i < nodeCount; i++) {
            if (degrees[i] < 3 && rand.nextBoolean()) {
                int target = rand.nextInt(nodeCount);
                if (target != i && target != (i+1)%nodeCount && target != (i-1+nodeCount)%nodeCount && degrees[target] < 3) {
                    // Check if edge already exists
                    boolean exists = edges.stream().anyMatch(e -> 
                        (e.getCity1().getId().equals(nodes.get(i).getId()) && e.getCity2().getId().equals(nodes.get(target).getId())) ||
                        (e.getCity2().getId().equals(nodes.get(i).getId()) && e.getCity1().getId().equals(nodes.get(target).getId()))
                    );
                    if (!exists) {
                        edges.add(MapEdge.builder().game(game).city1(nodes.get(i)).city2(nodes.get(target)).build());
                        degrees[i]++;
                        degrees[target]++;
                    }
                }
            }
        }
        
        mapEdgeRepository.saveAll(edges);
    }

    @Transactional(readOnly = true)
    public List<Game> findAll() { return gameRepository.findAll(); }

    @Transactional(readOnly = true)
    public Page<Game> findAll(Pageable pageable) { return gameRepository.findAll(pageable); }

    @Transactional(readOnly = true)
    public Game findById(Long id) { return getGame(id); }

    @Transactional(readOnly = true)
    public List<Player> findPlayers(Long gameId) {
        getGame(gameId);
        return playerRepository.findByGame_IdOrderById(gameId);
    }

    private Game getGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));
    }
}
'''

with open('code/src/main/java/com/eternalclash2/service/GameService.java', 'w', encoding='utf-8') as f:
    f.write(new_content)
