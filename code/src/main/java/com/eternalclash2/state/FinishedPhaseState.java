package com.eternalclash2.state;

import com.eternalclash2.exception.BusinessLogicException;

public class FinishedPhaseState implements GameStatePhase {
    @Override
    public void validateActionSubmission() {
        throw new BusinessLogicException("ไม่สามารถทำแอคชันได้ เกมจบลงแล้ว");
    }

    @Override
    public void validateTurnResolution() {
        throw new BusinessLogicException("ไม่สามารถจบเทิร์นได้ เกมจบลงแล้ว");
    }
}
