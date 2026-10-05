package com.eternalclash2.state;

import com.eternalclash2.domain.enums.GameStatus;

public class GameStateContext {
    private final GameStatePhase currentState;

    public GameStateContext(GameStatus status) {
        if (status == null) {
            this.currentState = new WaitingPhaseState();
            return;
        }
        this.currentState = switch (status) {
            case WAITING -> new WaitingPhaseState();
            case IN_PROGRESS -> new PlayPhaseState();
            case FINISHED -> new FinishedPhaseState();
            default -> new WaitingPhaseState();
        };
    }

    public GameStatePhase getCurrentState() {
        return currentState;
    }
}
