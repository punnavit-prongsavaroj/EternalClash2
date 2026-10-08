package com.eternalclash2.state;

import com.eternalclash2.exception.BusinessLogicException;

public class WaitingPhaseState implements GameStatePhase {
    @Override
    public void validateActionSubmission() {
        throw new BusinessLogicException("ไม่สามารถทำแอคชันได้ เกมกำลังรอผู้เล่นอื่น");
    }

    @Override
    public void validateTurnResolution() {
        throw new BusinessLogicException("ไม่สามารถจบเทิร์นได้ เกมกำลังรอผู้เล่นอื่น");
    }
}
