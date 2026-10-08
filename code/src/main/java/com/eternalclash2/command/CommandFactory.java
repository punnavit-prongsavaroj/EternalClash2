package com.eternalclash2.command;

import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.enums.ActionType;
import com.eternalclash2.repository.GameEventRepository;
import com.eternalclash2.service.ArmyService;
import com.eternalclash2.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandFactory {
    private final CityService cityService;
    private final ArmyService armyService;
    private final GameEventRepository gameEventRepository;

    public PlayerActionCommand createCommand(ActionType requestedAction, Long cityId, City city, Long targetCityId, Integer soldierCount, int turn, Game game) {
        if (requestedAction == null) return new NoneCommand();
        
        return switch (requestedAction) {
            case PRODUCE_FOOD -> new ProduceFoodCommand(cityService, cityId, turn);
            case RECRUIT_SOLDIERS -> new RecruitSoldiersCommand(cityService, cityId, turn);
            case SEND_ARMY -> new SendArmyCommand(armyService, city, targetCityId, soldierCount, turn, gameEventRepository, game);
            case NONE -> new NoneCommand();
        };
    }
}
