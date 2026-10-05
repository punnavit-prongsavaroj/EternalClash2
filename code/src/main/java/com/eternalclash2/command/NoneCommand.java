package com.eternalclash2.command;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.enums.ActionType;

public class NoneCommand implements PlayerActionCommand {
    @Override
    public void execute() {}

    @Override
    public ActionType getRecordedAction() {
        return ActionType.NONE;
    }

    @Override
    public Army getArmy() {
        return null;
    }
}
