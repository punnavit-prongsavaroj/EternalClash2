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
class RecruitSoldiersCommandTest {

    @Mock
    private CityService cityService;

    @Test
    void tc01_execute_delegatesToCityServiceRecruitSoldiers() {
        RecruitSoldiersCommand command = new RecruitSoldiersCommand(cityService, 10L, 6);

        command.execute();

        verify(cityService).recruitSoldiers(10L, 6);
    }

    @Test
    void tc02_getRecordedAction_isRecruitSoldiers() {
        RecruitSoldiersCommand command = new RecruitSoldiersCommand(cityService, 10L, 6);

        assertEquals(ActionType.RECRUIT_SOLDIERS, command.getRecordedAction());
    }
}
