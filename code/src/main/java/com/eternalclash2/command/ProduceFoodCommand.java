package com.eternalclash2.command;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.enums.ActionType;
import com.eternalclash2.service.CityService;

public class ProduceFoodCommand implements PlayerActionCommand {
    private final CityService cityService;
    private final Long playerId;
    private final int turn;

    public ProduceFoodCommand(CityService cityService, Long playerId, int turn) {
        this.cityService = cityService;
        this.playerId = playerId;
        this.turn = turn;
    }

    @Override
    public void execute() {
        cityService.produceFood(playerId, turn);
    }

    @Override
    public ActionType getRecordedAction() {
        return ActionType.PRODUCE_FOOD;
    }

    @Override
    public Army getArmy() {
        return null;
    }
}
