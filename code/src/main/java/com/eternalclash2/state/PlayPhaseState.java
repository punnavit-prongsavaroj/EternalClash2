package com.eternalclash2.state;

public class PlayPhaseState implements GameStatePhase {
    @Override
    public void validateActionSubmission() {
        // อนุญาตให้ส่งแอคชันได้
    }

    @Override
    public void validateTurnResolution() {
        // อนุญาตให้ประมวลผลเทิร์นได้
    }
}
