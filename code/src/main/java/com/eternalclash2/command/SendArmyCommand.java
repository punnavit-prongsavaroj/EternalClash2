package com.eternalclash2.command;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.ActionType;
import com.eternalclash2.exception.BusinessLogicException;
import com.eternalclash2.service.ArmyService;
import com.eternalclash2.repository.GameEventRepository;
import com.eternalclash2.domain.entity.GameEvent;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.enums.EventType;
import com.eternalclash2.domain.enums.LocationType;
import java.util.concurrent.ThreadLocalRandom;

public class SendArmyCommand implements PlayerActionCommand {
    private final ArmyService armyService;
    private final Player player;
    private final Long targetPlayerId;
    private final Integer soldierCount;
    private final int turn;
    private ActionType recordedAction = ActionType.SEND_ARMY;
    private Army army;

    private final GameEventRepository gameEventRepository;
    private final Game game;

    public SendArmyCommand(ArmyService armyService, Player player, Long targetPlayerId, Integer soldierCount, int turn, GameEventRepository gameEventRepository, Game game) {
        this.gameEventRepository = gameEventRepository;
        this.game = game;
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
            // สร้าง Event แจ้งเตือนผู้เล่นว่าโดนสกิลขัดขวาง
            if (gameEventRepository != null) {
                gameEventRepository.save(GameEvent.builder().game(game).turnNumber(turn).eventType(EventType.REBELLION)
                        .affectedPlayer(player).locationType(LocationType.IN_CITY).foodImpact(0).soldierImpact(0)
                        .extraTravelTurns(0).description("ขงเบ้งลังเล! กองทัพไม่ได้ถูกส่งออกไป (เสีย Action)").build());
            }
        } else {
            army = armyService.sendArmy(player.getId(), targetPlayerId, soldierCount, turn);
            if (player.getMarshal() != null && Boolean.TRUE.equals(player.getMarshal().getRevealsAttackTarget())) {
                if (gameEventRepository != null) {
                    gameEventRepository.save(GameEvent.builder().game(game).turnNumber(turn).eventType(EventType.REBELLION)
                        .affectedPlayer(army.getTarget()).locationType(LocationType.IN_CITY).foodImpact(0).soldierImpact(0).extraTravelTurns(0)
                        .description("ประกาศศึก! กองทัพของ " + player.getName() + " กำลังมุ่งหน้าไปโจมตีเมืองของ " + army.getTarget().getName() + " !!").build());
                }
            }
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
