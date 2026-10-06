package com.eternalclash2.service;

import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.GameRepository;
import com.eternalclash2.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.Random;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 7;

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
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
        player.setCity(City.builder().player(player).name(playerName.trim() + "'s City").food(50).soldiers(0).build());
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
        game.setStatus(GameStatus.MARSHAL_SELECTION);
        game.setCurrentTurnNumber(0);
        gameRepository.save(game);
        marshalDraftService.prepareNextPlayer(gameId);
        return game;
    }

    @Transactional(readOnly = true)
    public List<Game> findAll() { return gameRepository.findAll(); }

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
