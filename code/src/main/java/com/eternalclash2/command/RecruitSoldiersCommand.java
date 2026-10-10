package com.eternalclash2.command;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.enums.ActionType;
import com.eternalclash2.service.CityService;

public class RecruitSoldiersCommand implements PlayerActionCommand {
    private final CityService cityService;
    private final Long cityId;
    private final int turn;

    public RecruitSoldiersCommand(CityService cityService, Long cityId, int turn) {
        this.cityService = cityService;
        this.cityId = cityId;
        this.turn = turn;
    }

    @Override
    public void execute() {
        cityService.recruitSoldiers(cityId, turn);
    }

    @Override
    public ActionType getRecordedAction() {
        return ActionType.RECRUIT_SOLDIERS;
    }

    @Override
    public Army getArmy() {
        return null;
    }
}
