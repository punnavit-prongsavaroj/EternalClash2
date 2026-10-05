package com.eternalclash2.service;

import com.eternalclash2.domain.entity.Army;
import com.eternalclash2.domain.entity.Battle;
import com.eternalclash2.domain.entity.City;
import com.eternalclash2.domain.entity.Game;
import com.eternalclash2.domain.entity.Player;
import com.eternalclash2.domain.enums.ArmyStatus;
import com.eternalclash2.domain.enums.BattleResult;
import com.eternalclash2.domain.enums.BattleType;
import com.eternalclash2.domain.enums.GameStatus;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.ArmyRepository;
import com.eternalclash2.repository.BattleRepository;
import com.eternalclash2.repository.CityRepository;
import com.eternalclash2.repository.GameRepository;
import com.eternalclash2.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class BattleService {
    private final BattleRepository battleRepository;
    private final ArmyRepository armyRepository;
    private final CityRepository cityRepository;
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;

    @Transactional
    public void resolveTurnBattles(Long gameId, int turnNumber) {
        resolveReciprocalFieldBattles(gameId, turnNumber);
        List<Army> due = armyRepository.findByTarget_Game_IdAndStatus(gameId, ArmyStatus.TRAVELING).stream()
                .filter(a -> a.getArrivalTurn() <= turnNumber).toList();
        Set<Long> resolvedTargets = new HashSet<>();
        for (Army army : due) {
            Long targetId = army.getTarget().getId();
            if (resolvedTargets.add(targetId)) resolveSiegeGroup(gameId, targetId, turnNumber);
            Game game = army.getOwner().getGame();
            if (game.getStatus() == GameStatus.FINISHED) break;
        }
    }

    private void resolveReciprocalFieldBattles(Long gameId, int turnNumber) {
        List<Army> traveling = armyRepository.findByTarget_Game_IdAndStatus(gameId, ArmyStatus.TRAVELING);
        for (int i = 0; i < traveling.size(); i++) {
            Army first = traveling.get(i);
            if (first.getDepartureTurn() >= turnNumber || first.getSoldiers() <= 0) continue;
            for (int j = i + 1; j < traveling.size(); j++) {
                Army second = traveling.get(j);
                if (second.getDepartureTurn() >= turnNumber || second.getSoldiers() <= 0) continue;
                if (!first.getOwner().getId().equals(second.getTarget().getId())
                        || !second.getOwner().getId().equals(first.getTarget().getId())) continue;
                if (hasFieldBattle(first, second)) continue;

                int firstBefore = first.getSoldiers();
                int secondBefore = second.getSoldiers();
                int firstKills = kills(first.getOwner(), firstBefore);
                int secondKills = kills(second.getOwner(), secondBefore);
                int secondLosses = Math.min(secondBefore, firstKills);
                int firstLosses = Math.min(firstBefore, secondKills);
                first.setSoldiers(firstBefore - firstLosses);
                second.setSoldiers(secondBefore - secondLosses);
                if (first.getSoldiers() == 0) first.setStatus(ArmyStatus.CANCELLED);
                if (second.getSoldiers() == 0) second.setStatus(ArmyStatus.CANCELLED);
                armyRepository.save(first);
                armyRepository.save(second);

                battleRepository.save(Battle.builder().game(first.getOwner().getGame()).turnNumber(turnNumber)
                        .battleType(BattleType.FIELD_ENCOUNTER).attackerArmy(first).defenderPlayer(second.getOwner())
                        .defenderArmy(second).attackerSoldiers(firstBefore).defenderSoldiers(secondBefore)
                        .attackerCasualties(firstLosses).defenderCasualties(secondLosses)
                        .isCityDestroyed(false).result(first.getSoldiers() > 0 ? BattleResult.ATTACKER_WIN : BattleResult.DEFENDER_WIN).build());
            }
        }
    }

    private boolean hasFieldBattle(Army first, Army second) {
        return battleRepository.existsByBattleTypeAndAttackerArmy_IdAndDefenderArmy_Id(BattleType.FIELD_ENCOUNTER, first.getId(), second.getId())
                || battleRepository.existsByBattleTypeAndAttackerArmy_IdAndDefenderArmy_Id(BattleType.FIELD_ENCOUNTER, second.getId(), first.getId());
    }

    private void resolveSiegeGroup(Long gameId, Long targetId, int turnNumber) {
        List<Army> attackers = armyRepository.findByTarget_Game_IdAndStatus(gameId, ArmyStatus.TRAVELING).stream()
                .filter(a -> a.getTarget().getId().equals(targetId) && a.getArrivalTurn() <= turnNumber && a.getSoldiers() > 0).toList();
        if (attackers.isEmpty()) return;
        Player defender = playerRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("Target player not found: " + targetId));
        if (!Boolean.TRUE.equals(defender.getIsAlive())) {
            for (Army army : attackers) {
                army.setSoldiers(0);
                army.setStatus(ArmyStatus.CANCELLED);
                armyRepository.save(army);
            }
            return;
        }
        City city = cityRepository.findByPlayer_Id(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found for player: " + targetId));
        int defendersBefore = Math.max(0, city.getSoldiers());
        int totalKills = attackers.stream().mapToInt(a -> kills(a.getOwner(), a.getSoldiers())).sum();
        int defenderLosses = Math.min(defendersBefore, totalKills);
        city.setSoldiers(defendersBefore - defenderLosses);
        cityRepository.save(city);

        int remainingDefenderLosses = defenderLosses;
        for (Army army : attackers) {
            int contribution = kills(army.getOwner(), army.getSoldiers());
            int creditedLosses = Math.min(remainingDefenderLosses, contribution);
            remainingDefenderLosses -= creditedLosses;
            int attackerBefore = army.getSoldiers();
            army.setSoldiers(0);
            army.setStatus(ArmyStatus.DESTROYED);
            armyRepository.save(army);
            battleRepository.save(Battle.builder().game(army.getOwner().getGame()).turnNumber(turnNumber)
                    .battleType(BattleType.CITY_SIEGE).attackerArmy(army).defenderPlayer(defender)
                    .attackerSoldiers(attackerBefore).defenderSoldiers(defendersBefore)
                    .attackerCasualties(attackerBefore).defenderCasualties(creditedLosses)
                    .isCityDestroyed(false).result(BattleResult.DEFENDER_WIN).build());
        }

        if (defendersBefore == 0 || defenderLosses >= defendersBefore) {
            boolean survived = hasSpecial(defender, "SURVIVE_DESTRUCTION") && ThreadLocalRandom.current().nextInt(100) < 50;
            if (!survived) eliminatePlayer(defender, turnNumber);
            for (Battle battle : battleRepository.findByGame_IdAndTurnNumberAndDefenderPlayer_IdAndBattleType(
                    gameId, turnNumber, targetId, BattleType.CITY_SIEGE)) {
                battle.setIsCityDestroyed(!survived);
                battle.setResult(!survived ? BattleResult.ATTACKER_WIN : BattleResult.DEFENDER_WIN);
                battleRepository.save(battle);
            }
        }
    }

    private void eliminatePlayer(Player player, int turnNumber) {
        player.setIsAlive(false);
        player.setEliminatedAtTurn(turnNumber);
        playerRepository.save(player);
        Game game = player.getGame();
        List<Player> alive = playerRepository.findByGame_IdOrderById(game.getId()).stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsAlive())).toList();
        if (alive.size() <= 1) {
            game.setStatus(GameStatus.FINISHED);
            game.setWinner(alive.isEmpty() ? null : alive.get(0));
            gameRepository.save(game);
        }
    }

    private int kills(Player player, int soldiers) {
        double ratio = player.getMarshal() == null || player.getMarshal().getAttackKillRatio() == null
                ? 1.0 : player.getMarshal().getAttackKillRatio();
        return Math.max(0, (int) Math.floor(soldiers * ratio));
    }

    private boolean hasSpecial(Player player, String type) {
        return player.getMarshal() != null && type.equals(player.getMarshal().getSpecialAbilityType());
    }

    @Transactional(readOnly = true)
    public List<Battle> findAll() { return battleRepository.findAll(); }

    @Transactional(readOnly = true)
    public Battle findById(Long id) {
        return battleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Battle not found with id: " + id));
    }
}
