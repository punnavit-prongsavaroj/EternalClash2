package com.eternalclash2.service;

import com.eternalclash2.domain.enums.Season;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameClockTest {

    @Test
    void tc01_season_followsTheTwelveTurnCycle() {
        assertEquals(Season.SUMMER, GameClock.season(1));
        assertEquals(Season.SUMMER, GameClock.season(4));
        assertEquals(Season.RAINY, GameClock.season(5));
        assertEquals(Season.RAINY, GameClock.season(8));
        assertEquals(Season.WINTER, GameClock.season(9));
        assertEquals(Season.WINTER, GameClock.season(12));
        assertEquals(Season.SUMMER, GameClock.season(13));
    }

    @Test
    void tc02_isDaytime_isTrueOnOddTurns() {
        assertTrue(GameClock.isDaytime(1));
        assertTrue(GameClock.isDaytime(3));
        assertFalse(GameClock.isDaytime(2));
        assertFalse(GameClock.isDaytime(4));
    }

    @Test
    void tc03_isSeasonEnd_isTrueEveryFourthTurn() {
        assertTrue(GameClock.isSeasonEnd(4));
        assertTrue(GameClock.isSeasonEnd(8));
        assertFalse(GameClock.isSeasonEnd(3));
        assertFalse(GameClock.isSeasonEnd(5));
    }

    @Test
    void tc04_season_ofTurnZero_wrapsBackIntoWinter() {
        assertEquals(Season.WINTER, GameClock.season(0));
    }

    @Test
    void tc05_season_ofANegativeTurn_staysInsideTheCycle() {
        assertEquals(Season.SUMMER, GameClock.season(-11));
    }

    @Test
    void tc06_isDaytime_isNeverTrueForZeroOrNegativeTurns() {
        assertFalse(GameClock.isDaytime(0));
        assertFalse(GameClock.isDaytime(-1));
        assertFalse(GameClock.isDaytime(-3));
    }

    @Test
    void tc07_isSeasonEnd_isFalseForTurnZero() {
        assertFalse(GameClock.isSeasonEnd(0));
    }

    @Test
    void tc08_extremeTurnNumber_doesNotBreakTheCycle() {
        assertEquals(Season.RAINY, GameClock.season(Integer.MAX_VALUE));
        assertFalse(GameClock.isSeasonEnd(Integer.MAX_VALUE));
    }
}
