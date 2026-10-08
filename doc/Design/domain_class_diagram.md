# Domain Class Diagram (Entity Model)

แผนภาพ Class Diagram นี้นำเสนอโครงสร้างข้อมูล (Entity) และ Enumeration ทั้งหมดในระบบเกม EternalClash2 ซึ่งแมป (Map) กับโครงสร้างของฐานข้อมูลผ่าน Spring Data JPA โดยตรง

## แผนภาพโครงสร้างรวม (Full System Entities)

```mermaid
classDiagram
    direction TB

    %% ---------------- ENUMS ----------------
    class GameStatus {
        <<enumeration>>
        WAITING
        MARSHAL_SELECTION
        PLACEMENT
        IN_PROGRESS
        FINISHED
    }
    
    class ArmyStatus {
        <<enumeration>>
        TRAVELING
        ARRIVED
        DESTROYED
        CANCELLED
    }
    
    class ActionType {
        <<enumeration>>
        PRODUCE_FOOD
        RECRUIT_SOLDIERS
        SEND_ARMY
        NONE
    }
    
    class EventType {
        <<enumeration>>
        SINKHOLE, SUN_GLARE, LIGHTNING, 
        AVALANCHE, FOOD_SPOILAGE, INSECT_DAMAGE, 
        FROSTBITE, EPIDEMIC, SLOW, FLOOD, 
        SUNBURN, SNOW_COVER, REBELLION
    }
    
    class BattleResult {
        <<enumeration>>
        ATTACKER_WIN
        DEFENDER_WIN
    }

    class LocationType {
        <<enumeration>>
        IN_CITY
        OUTSIDE_CITY
    }

    %% ---------------- ENTITIES ----------------
    class Game {
        +Long id
        +Integer currentTurnNumber
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }

    class Player {
        +Long id
        +String name
        +Boolean isAlive
        +Integer eliminatedAtTurn
        +Integer rerollCount
    }

    class City {
        +Long id
        +String name
        +Integer food
        +Integer soldiers
        +Double x
        +Double y
        +Boolean actionUsedThisTurn
    }

    class MapEdge {
        +Long id
        +City city1
        +City city2
        +Integer distance
    }

    class Marshal {
        +Long id
        +String name
        +String abilityName
        +Integer foodProduction
        +Integer soldierProduction
        +Double attackKillRatio
        +Boolean revealsAttackTarget
    }

    class Army {
        +Long id
        +Integer soldiers
        +Integer departureTurn
        +Integer arrivalTurn
    }

    class MarshalCandidate {
        +Long id
        +Integer slotNumber
        +Boolean isSelected
    }

    class TurnAction {
        +Long id
        +Integer turnNumber
        +Integer foodBefore
        +Integer foodAfter
        +Integer soldiersBefore
        +Integer soldiersAfter
    }

    class GameEvent {
        +Long id
        +Integer turnNumber
        +Integer foodImpact
        +Integer soldierImpact
        +Integer extraTravelTurns
        +String description
    }

    class Battle {
        +Long id
        +Integer turnNumber
        +Integer attackerSoldiers
        +Integer defenderSoldiers
        +Integer attackerCasualties
        +Integer defenderCasualties
        +Boolean isCityDestroyed
    }

    %% ---------------- RELATIONSHIPS ----------------
    
    %% Game & Core
    Game --> GameStatus : status
    Game "1" *-- "*" Player : contains
    Game "1" *-- "*" TurnAction : has
    Game "1" *-- "*" GameEvent : has
    Game "1" *-- "*" Battle : has

    %% Player & Assets
    Player "1" -- "*" City : owns
    Player "*" --> "1" Marshal : commands
    Player "1" *-- "*" MarshalCandidate : drafts
    
    %% Army
    Army --> ArmyStatus : status
    Player "1" *-- "*" Army : sends (owner)
    Player "1" <-- "*" Army : targeted by

    %% Action & Event Log
    TurnAction --> ActionType : actionType
    TurnAction "*" --> "1" Player : performed by
    TurnAction "*" --> "0..1" Army : spawns
    
    GameEvent --> EventType : eventType
    GameEvent --> LocationType : locationType
    GameEvent "*" --> "0..1" Player : affects (City)
    GameEvent "*" --> "0..1" Army : affects (Army)

    %% Battle Log
    Battle --> BattleResult : result
    Battle "*" --> "1" Army : attacker
    Battle "*" --> "1" Player : defender (City)
```
