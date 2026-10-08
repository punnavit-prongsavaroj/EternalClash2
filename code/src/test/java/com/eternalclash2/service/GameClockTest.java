package com.eternalclash2.service;

import com.eternalclash2.domain.enums.Season;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameClockTest {

    @Test
    void testSeasonCalculation() {
        assertEquals(Season.SUMMER, GameClock.season(1));
        assertEquals(Season.SUMMER, GameClock.season(4));
        assertEquals(Season.RAINY, GameClock.season(5));
        assertEquals(Season.RAINY, GameClock.season(8));
        assertEquals(Season.WINTER, GameClock.season(9));
        assertEquals(Season.WINTER, GameClock.season(12));
        assertEquals(Season.SUMMER, GameClock.season(13));
    }

    @Test
    void testDaytimeCalculation() {
        assertTrue(GameClock.isDaytime(1));
        assertTrue(GameClock.isDaytime(3));
        assertFalse(GameClock.isDaytime(2));
        assertFalse(GameClock.isDaytime(4));
    }

    @Test
    void testSeasonEnd() {
        assertTrue(GameClock.isSeasonEnd(4));
        assertTrue(GameClock.isSeasonEnd(8));
        assertFalse(GameClock.isSeasonEnd(3));
        assertFalse(GameClock.isSeasonEnd(5));
    }
}
