package com.eternalclash2.service;

import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.MapEdge;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.entity.PlayerStats;
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
        Player player = Player.builder().game(game).name(playerName.trim()).isAlive(true).rerollCount(0).build();
        PlayerStats stats = PlayerStats.builder().player(player).totalFoodProduced(0).totalSoldiersRecruited(0).totalBattlesFought(0).citiesConquered(0).build();
        player.setPlayerStats(stats);
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
        
        // 1. Create nodes with random positions
        List<String> cityNames = new ArrayList<>(Arrays.asList("ซือลี่", "อิวจิ๋ว", "กีจิ๋ว", "เป้งจิ๋ว", "เฉงจิ๋ว", "กุนจิ๋ว", "ชีจิ๋ว", "อื้อจิ๋ว", "สูจิ๋ว", "เหลียงจิ๋ว", "อี้จิ๋ว", "เกงจิ๋ว", "แยว่จิ๋ว", "เกาจิ๋ว", "ลิหยาง", "ลำกุ๋น", "เลนเหลง", "ฮุยแฮ", "บูเหล็ง", "ปาเสียว", "เองเฉวียน"));
        Collections.shuffle(cityNames, rand);
        List<Double> xCoords = new ArrayList<>();
        List<Double> yCoords = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) {
            String cName = i < cityNames.size() ? cityNames.get(i) : "Node " + (i + 1);
            
            double x = 0, y = 0;
            boolean valid = false;
            int attempts = 0;
            while (!valid && attempts < 100) {
                x = 10.0 + rand.nextDouble() * 80.0;
                y = 10.0 + rand.nextDouble() * 80.0;
                valid = true;
                for (int j = 0; j < xCoords.size(); j++) {
                    double dx = xCoords.get(j) - x;
                    double dy = yCoords.get(j) - y;
                    if (Math.sqrt(dx * dx + dy * dy) < 12.0) { // minimum distance of 12% between nodes
                        valid = false;
                        break;
                    }
                }
                attempts++;
            }
            xCoords.add(x);
            yCoords.add(y);

            City city = City.builder()
                .game(game)
                .name(cName)
                .food(0)
                .soldiers(30)
                .x(x)
                .y(y)
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
                    final City c1 = nodes.get(i);
                    final City c2 = nodes.get(target);
                    boolean exists = edges.stream().anyMatch(e -> 
                        (e.getCity1().getId().equals(c1.getId()) && e.getCity2().getId().equals(c2.getId())) ||
                        (e.getCity2().getId().equals(c1.getId()) && e.getCity1().getId().equals(c2.getId()))
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

    @Transactional
    public void deleteGame(Long gameId) {
        Game game = getGame(gameId);
        gameRepository.delete(game);
    }

    // Cleanup abandoned or old games
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 3600000) // Run every 1 hour
    @Transactional
    public void cleanupOldGames() {
        java.time.LocalDateTime threshold = java.time.LocalDateTime.now().minusHours(24);
        java.util.List<Game> oldGames = gameRepository.findByUpdatedAtBefore(threshold);
        gameRepository.deleteAll(oldGames);
    }
}
