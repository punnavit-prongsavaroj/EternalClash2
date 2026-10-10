package com.eternalclash2.command;

import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.enums.ActionType;
import com.eternalclash2.repository.GameEventRepository;
import com.eternalclash2.service.ArmyService;
import com.eternalclash2.service.CityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@ExtendWith(MockitoExtension.class)
class CommandFactoryTest {

    @Mock
    private CityService cityService;

    @Mock
    private ArmyService armyService;

    @Mock
    private GameEventRepository gameEventRepository;

    private final Game game = Game.builder().id(1L).build();
    private final City city = City.builder().id(10L).name("City-10").build();

    private PlayerActionCommand create(ActionType action, Long targetCityId, Integer soldierCount) {
        return new CommandFactory(cityService, armyService, gameEventRepository)
                .createCommand(action, 10L, city, targetCityId, soldierCount, 1, game);
    }

    @Test
    void tc01_createCommand_produceFoodReturnsProduceFoodCommand() {
        assertInstanceOf(ProduceFoodCommand.class, create(ActionType.PRODUCE_FOOD, null, null));
    }

    @Test
    void tc02_createCommand_recruitSoldiersReturnsRecruitSoldiersCommand() {
        assertInstanceOf(RecruitSoldiersCommand.class, create(ActionType.RECRUIT_SOLDIERS, null, null));
    }

    @Test
    void tc03_createCommand_sendArmyReturnsSendArmyCommand() {
        assertInstanceOf(SendArmyCommand.class, create(ActionType.SEND_ARMY, 20L, 20));
    }

    @Test
    void tc04_createCommand_noneReturnsNoneCommand() {
        assertInstanceOf(NoneCommand.class, create(ActionType.NONE, null, null));
    }

    @Test
    void tc05_createCommand_withNullActionReturnsNoneCommand() {
        assertInstanceOf(NoneCommand.class, create(null, null, null));
    }
}
