package com.eternalclash2.service;

import com.eternalclash2.domain.entity.*;
import com.eternalclash2.domain.enums.*;
import com.eternalclash2.exception.ResourceNotFoundException;
import com.eternalclash2.repository.*;
import com.eternalclash2.strategy.CombatContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BattleService {
    private final ArmyRepository armyRepository;
    private final BattleRepository battleRepository;
    private final CityRepository cityRepository;
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final GameEventRepository gameEventRepository;

    @Transactional
    public void resolveBattles(Long gameId, int turnNumber) {
        resolveReciprocalFieldBattles(gameId, turnNumber);
        
        List<Army> due = armyRepository.findByTargetCity_Game_IdAndStatus(gameId, ArmyStatus.TRAVELING).stream()
                .filter(a -> a.getArrivalTurn() <= turnNumber).toList();
                
        // Process each arriving army sequentially
        for (Army army : due) {
            if (army.getSoldiers() <= 0) continue;
            resolveArrival(army, turnNumber);
            
            Game game = army.getOwner().getGame();
            checkWinCondition(game);
            if (game.getStatus() == GameStatus.FINISHED) break;
        }
    }

    private void resolveReciprocalFieldBattles(Long gameId, int turnNumber) {
        List<Army> traveling = armyRepository.findByTargetCity_Game_IdAndStatus(gameId, ArmyStatus.TRAVELING);
        for (int i = 0; i < traveling.size(); i++) {
            Army first = traveling.get(i);
            if (first.getDepartureTurn() >= turnNumber || first.getSoldiers() <= 0) continue;
            for (int j = i + 1; j < traveling.size(); j++) {
                Army second = traveling.get(j);
                if (second.getDepartureTurn() >= turnNumber || second.getSoldiers() <= 0) continue;
                
                // If they cross paths on the same edge in opposite directions
                if (first.getSourceCity().getId().equals(second.getTargetCity().getId()) &&
                    second.getSourceCity().getId().equals(first.getTargetCity().getId()) &&
                    !first.getOwner().getId().equals(second.getOwner().getId())) {
                    
                    if (hasFieldBattle(first, second)) continue;

                    int firstBefore = first.getSoldiers();
                    int secondBefore = second.getSoldiers();
                    int firstKills = kills(first.getOwner(), firstBefore);
                    int secondKills = kills(second.getOwner(), secondBefore);
                    int secondLosses = Math.min(secondBefore, firstKills);
                    int firstLosses = Math.min(firstBefore, secondKills);
                    
                    first.setSoldiers(firstBefore - firstLosses);
                    second.setSoldiers(secondBefore - secondLosses);
                    if (first.getSoldiers() <= 0) first.setStatus(ArmyStatus.CANCELLED);
                    if (second.getSoldiers() <= 0) second.setStatus(ArmyStatus.CANCELLED);

                    if (first.getOwner().getPlayerStats() != null) {
                        first.getOwner().getPlayerStats().setTotalBattlesFought(first.getOwner().getPlayerStats().getTotalBattlesFought() + 1);
                    }
                    if (second.getOwner().getPlayerStats() != null) {
                        second.getOwner().getPlayerStats().setTotalBattlesFought(second.getOwner().getPlayerStats().getTotalBattlesFought() + 1);
                    }
                    
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
    }

    private boolean hasFieldBattle(Army first, Army second) {
        return battleRepository.existsByBattleTypeAndAttackerArmy_IdAndDefenderArmy_Id(BattleType.FIELD_ENCOUNTER, first.getId(), second.getId())
                || battleRepository.existsByBattleTypeAndAttackerArmy_IdAndDefenderArmy_Id(BattleType.FIELD_ENCOUNTER, second.getId(), first.getId());
    }

    private void resolveArrival(Army army, int turnNumber) {
        City targetCity = army.getTargetCity();
        Player owner = army.getOwner();
        
        // 1. Reinforcement (Target is owned by the sender)
        if (targetCity.getPlayer() != null && targetCity.getPlayer().getId().equals(owner.getId())) {
            targetCity.setSoldiers(targetCity.getSoldiers() + army.getSoldiers());
            army.setSoldiers(0);
            army.setStatus(ArmyStatus.DESTROYED); // Mark as arrived/consumed
            cityRepository.save(targetCity);
            armyRepository.save(army);
            return;
        }
        
        // 2. Combat (Target is enemy or neutral)
        int defendersBefore = Math.max(0, targetCity.getSoldiers());
        int attackersBefore = army.getSoldiers();
        
        Player defender = targetCity.getPlayer(); // Can be null if neutral

        if (owner.getPlayerStats() != null) {
            owner.getPlayerStats().setTotalBattlesFought(owner.getPlayerStats().getTotalBattlesFought() + 1);
        }
        if (defender != null && defender.getPlayerStats() != null) {
            defender.getPlayerStats().setTotalBattlesFought(defender.getPlayerStats().getTotalBattlesFought() + 1);
        }

        int attackerKills = kills(owner, attackersBefore);
        int defenderKills = defender != null ? kills(defender, defendersBefore) : defendersBefore; // Neutral deals 1-to-1 damage
        
        int defenderLosses = Math.min(defendersBefore, attackerKills);
        int attackerLosses = Math.min(attackersBefore, defenderKills);
        
        int defendersRemaining = defendersBefore - defenderLosses;
        int attackersRemaining = attackersBefore - attackerLosses;
        
        boolean isCityCaptured = defendersRemaining <= 0 && attackersRemaining > 0;
        
        if (isCityCaptured) {
            // Check survival skill for the defender
            boolean survived = false;
            if (defender != null) {
                survived = new CombatContext(defender).executeSurvival();
                if (survived && defender.getMarshal() != null && "SURVIVE_DESTRUCTION".equals(defender.getMarshal().getSpecialAbilityType())) {
                    gameEventRepository.save(GameEvent.builder().game(defender.getGame()).turnNumber(turnNumber)
                            .eventType(EventType.REBELLION).affectedPlayer(defender).locationType(LocationType.IN_CITY)
                            .foodImpact(0).soldierImpact(0).extraTravelTurns(0)
                            .description("ปาฏิหาริย์! กลยุทธ์ขงเบ้งทำให้เมืองรอดพ้นจากการถูกยึดครองอย่างหวุดหวิด!").build());
                }
            }
            
            if (survived) {
                targetCity.setSoldiers(1); // Barely survived
                army.setSoldiers(0);
                army.setStatus(ArmyStatus.DESTROYED);
            } else {
                // Capture the city!
                targetCity.setPlayer(owner);
                String oldName = targetCity.getName();
                if (oldName.contains("'s ")) {
                    oldName = oldName.substring(oldName.lastIndexOf("'s ") + 3);
                }
                targetCity.setName(owner.getName() + "'s " + oldName);
                targetCity.setSoldiers(attackersRemaining);
                targetCity.setFood(0); // Optionally zero out food, or keep it
                
                army.setSoldiers(0);
                army.setStatus(ArmyStatus.DESTROYED);

                if (owner.getPlayerStats() != null) {
                    owner.getPlayerStats().setCitiesConquered(owner.getPlayerStats().getCitiesConquered() + 1);
                }
                
                if (defender != null) {
                    checkPlayerElimination(defender, turnNumber);
                }
            }
        } else {
            // Attack failed or wiped out
            targetCity.setSoldiers(defendersRemaining);
            army.setSoldiers(attackersRemaining);
            army.setStatus(attackersRemaining > 0 ? ArmyStatus.TRAVELING : ArmyStatus.DESTROYED); // If they survive but fail, they bounce back?
            
            // Rules say: "ถ้าตีเมืองแตก ทหารเหลือจะไปอยู่ที่เมืองพึ่งตีแตก". It didn't mention failing. 
            // Usually, attackers fight to the death in sieges in this game.
            if (attackersRemaining > 0) {
                 // But wait, earlier mechanics just deleted them. Let's say they fight to the death.
                 // Actually, if attackers remain but didn't capture (defenders remaining > 0), they are wiped out.
                 attackerLosses = attackersBefore;
                 army.setSoldiers(0);
                 army.setStatus(ArmyStatus.DESTROYED);
            }
        }
        
        cityRepository.save(targetCity);
        armyRepository.save(army);
        
        battleRepository.save(Battle.builder().game(owner.getGame()).turnNumber(turnNumber)
                .battleType(BattleType.CITY_SIEGE).attackerArmy(army).defenderPlayer(defender)
                .attackerSoldiers(attackersBefore).defenderSoldiers(defendersBefore)
                .attackerCasualties(attackerLosses).defenderCasualties(defenderLosses)
                .isCityDestroyed(isCityCaptured).result(isCityCaptured ? BattleResult.ATTACKER_WIN : BattleResult.DEFENDER_WIN).build());
    }

    private void checkPlayerElimination(Player player, int turnNumber) {
        long citiesOwned = cityRepository.findByGame_Id(player.getGame().getId()).stream()
                .filter(c -> c.getPlayer() != null && c.getPlayer().getId().equals(player.getId())).count();
                
        if (citiesOwned == 0) {
            player.setIsAlive(false);
            player.setEliminatedAtTurn(turnNumber);
            playerRepository.save(player);
        }
    }

    private void checkWinCondition(Game game) {
        List<Player> alive = playerRepository.findByGame_IdOrderById(game.getId()).stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsAlive())).toList();
        if (alive.size() <= 1 && game.getStatus() != GameStatus.WAITING && game.getStatus() != GameStatus.PLACEMENT && game.getStatus() != GameStatus.MARSHAL_SELECTION) {
            game.setStatus(GameStatus.FINISHED);
            game.setWinner(alive.isEmpty() ? null : alive.get(0));
            gameRepository.save(game);
        }
    }

    private int kills(Player player, int soldiers) {
        return new CombatContext(player).executeKills(player, soldiers);
    }

    @Transactional(readOnly = true)
    public List<Battle> findAll() { return battleRepository.findAll(); }

    @Transactional(readOnly = true)
    public Battle findById(Long id) {
        return battleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Battle not found with id: " + id));
    }
}
