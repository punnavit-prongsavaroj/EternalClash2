package com.eternalclash2.command;

import com.eternalclash2.domain.enums.ActionType;
import com.eternalclash2.service.CityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProduceFoodCommandTest {

    @Mock
    private CityService cityService;

    @Test
    void tc01_execute_delegatesToCityServiceProduceFood() {
        ProduceFoodCommand command = new ProduceFoodCommand(cityService, 10L, 1);

        command.execute();

        verify(cityService).produceFood(10L, 1);
    }

    @Test
    void tc02_getRecordedAction_isProduceFood() {
        ProduceFoodCommand command = new ProduceFoodCommand(cityService, 10L, 1);

        assertEquals(ActionType.PRODUCE_FOOD, command.getRecordedAction());
    }
}
