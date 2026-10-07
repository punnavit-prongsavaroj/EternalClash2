package com.eternalclash2.service;

import com.eternalclash2.domain.enums.Season;

public final class GameClock {
    private GameClock() {}

    public static Season season(int turnNumber) {
        int index = Math.floorMod(turnNumber - 1, 12);
        if (index < 4) return Season.SUMMER;
        if (index < 8) return Season.RAINY;
        return Season.WINTER;
    }

    public static boolean isDaytime(int turnNumber) {
        return turnNumber % 2 == 1;
    }

    public static boolean isSeasonEnd(int turnNumber) {
        return turnNumber > 0 && turnNumber % 4 == 0;
    }
}
