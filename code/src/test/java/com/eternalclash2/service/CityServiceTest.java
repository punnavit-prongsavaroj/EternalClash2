package com.eternalclash2.service;

import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Marshal;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.entity.PlayerStats;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.MapEdgeRepository;
import com.eternalclash2.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerService playerService;

    @Mock
    private MapEdgeRepository mapEdgeRepository;

    @InjectMocks
    private CityService cityService;

    @Test
    void produceFood_Success() {
        Game game = Game.builder().id(1L).build();
        PlayerStats stats = new PlayerStats();
        Player player = Player.builder().id(100L).game(game).isAlive(true).playerStats(stats).build();
        City city = City.builder().id(10L).food(100).player(player).build();

        when(cityRepository.findById(10L)).thenReturn(Optional.of(city));
        when(playerService.getAlivePlayerValidated(100L)).thenReturn(player);
        when(cityRepository.save(any(City.class))).thenReturn(city);

        City result = cityService.produceFood(10L, 1);

        assertEquals(120, result.getFood());
        assertTrue(result.getActionUsedThisTurn());
        assertEquals(20, stats.getTotalFoodProduced());
    }

    @Test
    void recruitSoldiers_Success() {
        Game game = Game.builder().id(1L).build();
        PlayerStats stats = new PlayerStats();
        Player player = Player.builder().id(100L).game(game).isAlive(true).playerStats(stats).build();
        City city = City.builder().id(10L).game(game).food(100).soldiers(30).player(player).build();

        when(cityRepository.findById(10L)).thenReturn(Optional.of(city));
        when(playerService.getAlivePlayerValidated(100L)).thenReturn(player);
        when(cityRepository.save(any(City.class))).thenReturn(city);

        City result = cityService.recruitSoldiers(10L, 1);

        assertEquals(45, result.getSoldiers());
        assertEquals(80, result.getFood()); // 100 - 20 cost
        assertTrue(result.getActionUsedThisTurn());
        assertEquals(15, stats.getTotalSoldiersRecruited());
    }

    @Test
    void recruitSoldiers_ThrowsWhenNotEnoughFood() {
        Game game = Game.builder().id(1L).build();
        Player player = Player.builder().id(100L).game(game).isAlive(true).build();
        City city = City.builder().id(10L).game(game).food(10).soldiers(30).player(player).build();

        when(cityRepository.findById(10L)).thenReturn(Optional.of(city));
        when(playerService.getAlivePlayerValidated(100L)).thenReturn(player);

        assertThrows(BusinessLogicException.class, () -> cityService.recruitSoldiers(10L, 1));
    }
}
