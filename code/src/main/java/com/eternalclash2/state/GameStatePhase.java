package com.eternalclash2.state;

import com.eternalclash2.exception.BusinessLogicException;

public interface GameStatePhase {
    void validateActionSubmission() throws BusinessLogicException;
    void validateTurnResolution() throws BusinessLogicException;
}
