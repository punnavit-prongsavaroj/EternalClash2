package com.eternalclash2.command;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.enums.ActionType;

public interface PlayerActionCommand {
    void execute();
    ActionType getRecordedAction();
    Army getArmy();
}
