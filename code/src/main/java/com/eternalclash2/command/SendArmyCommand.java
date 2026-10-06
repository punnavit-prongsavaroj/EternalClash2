package com.eternalclash2.command;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.ActionType;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.service.ArmyService;
import java.util.concurrent.ThreadLocalRandom;

public class SendArmyCommand implements PlayerActionCommand {
    private final ArmyService armyService;
    private final Player player;
    private final Long targetPlayerId;
    private final Integer soldierCount;
    private final int turn;
    private ActionType recordedAction = ActionType.SEND_ARMY;
    private Army army;

    public SendArmyCommand(ArmyService armyService, Player player, Long targetPlayerId, Integer soldierCount, int turn) {
        this.armyService = armyService;
        this.player = player;
        this.targetPlayerId = targetPlayerId;
        this.soldierCount = soldierCount;
        this.turn = turn;
    }

    @Override
    public void execute() {
        if (targetPlayerId == null || soldierCount == null) {
            throw new BusinessLogicException("Target and soldier count are required to send an army");
        }
        if (hasSpecial(player, "SURVIVE_DESTRUCTION") && ThreadLocalRandom.current().nextInt(100) < 20) {
            recordedAction = ActionType.NONE;
        } else {
            army = armyService.sendArmy(player.getId(), targetPlayerId, soldierCount, turn);
        }
    }

    @Override
    public ActionType getRecordedAction() {
        return recordedAction;
    }

    @Override
    public Army getArmy() {
        return army;
    }

    private boolean hasSpecial(Player player, String type) {
        return player.getMarshal() != null && type.equals(player.getMarshal().getSpecialAbilityType());
    }
}
