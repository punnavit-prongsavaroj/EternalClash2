package com.eternalclash2.command;

import com.eternalclash2.domain.enums.ActionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NoneCommandTest {

    @Test
    void tc01_execute_doesNothingAndThrowsNothing() {
        NoneCommand command = new NoneCommand();

        assertDoesNotThrow(command::execute);
    }

    @Test
    void tc02_getRecordedAction_isNone() {
        NoneCommand command = new NoneCommand();

        assertEquals(ActionType.NONE, command.getRecordedAction());
    }
}
