package com.eternalclash2.command;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.City;
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
    private final City sourceCity;
    private final Long targetCityId;
    private final Integer soldierCount;
    private final int turn;
    private ActionType recordedAction = ActionType.SEND_ARMY;
    private Army army;

    private final GameEventRepository gameEventRepository;
    private final Game game;

    public SendArmyCommand(ArmyService armyService, City sourceCity, Long targetCityId, Integer soldierCount, int turn, GameEventRepository gameEventRepository, Game game) {
        this.gameEventRepository = gameEventRepository;
        this.game = game;
        this.armyService = armyService;
        this.sourceCity = sourceCity;
        this.targetCityId = targetCityId;
        this.soldierCount = soldierCount;
        this.turn = turn;
    }

    @Override
    public void execute() {
        if (targetCityId == null || soldierCount == null) {
            throw new BusinessLogicException("Target and soldier count are required to send an army");
        }
        Player player = sourceCity.getPlayer();
        if (hasSpecial(player, "SURVIVE_DESTRUCTION") && ThreadLocalRandom.current().nextInt(100) < 20) {
            recordedAction = ActionType.NONE;
            if (gameEventRepository != null) {
                gameEventRepository.save(GameEvent.builder().game(game).turnNumber(turn).eventType(EventType.REBELLION)
                        .affectedPlayer(player).locationType(LocationType.IN_CITY).foodImpact(0).soldierImpact(0)
                        .extraTravelTurns(0).description("ทหารไม่ฟัง! กองทัพไม่ได้ถูกส่งออกไป (เสีย Action)").build());
            }
            sourceCity.setActionUsedThisTurn(true);
        } else {
            army = armyService.sendArmy(sourceCity.getId(), targetCityId, soldierCount, turn);
            if (player.getMarshal() != null && Boolean.TRUE.equals(player.getMarshal().getRevealsAttackTarget())) {
                if (gameEventRepository != null) {
                    gameEventRepository.save(GameEvent.builder().game(game).turnNumber(turn).eventType(EventType.REBELLION)
                        .affectedPlayer(army.getTargetCity().getPlayer()).locationType(LocationType.IN_CITY).foodImpact(0).soldierImpact(0).extraTravelTurns(0)
                        .description("ประกาศศึก! กองทัพของ " + player.getName() + " กำลังมุ่งหน้าไปโจมตีเป้าหมาย: " + army.getTargetCity().getName() + " !!").build());
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
