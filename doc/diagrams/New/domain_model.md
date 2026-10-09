# Domain Model / Conceptual Class Diagram

> **คืออะไร (What is this?)**
> แผนภาพคลาสในระดับแนวคิด ที่มุ่งเน้นไปที่การอธิบาย "คำศัพท์" (Ubiquitous Language) และวัตถุหลักๆ ในโลกของปัญหา (Domain) โดยไม่สนเรื่องเทคนิคเชิงลึก (เช่น ไม่มี Controller หรือ Repository)
> 
> **ใช้เทคนิคอะไร (Techniques Used)**
> แนวคิดเบื้องต้นของ Domain-Driven Design (DDD)
> 
> **ใช้ทำไมและเพื่ออะไร (Why & Purpose)**
> ใช้เพื่อทำความเข้าใจ Business Logic ของเกม EternalClash2 ในมุมมองของผู้เชี่ยวชาญ/ผู้เล่นเกม เพื่อให้โปรแกรมเมอร์และทีมงานอื่นๆ มองเห็นโครงสร้างของเกมตรงกัน

แสดงความสัมพันธ์ของ Entity หลักในระบบโดยเน้นที่ระดับแนวคิด (Conceptual Level)

```mermaid
classDiagram
    class Game {
        +String roomCode
        +GameStatus status
        +int currentTurnNumber
    }
    
    class Player {
        +String name
        +boolean isAlive
    }
    
    class PlayerStats {
        +int totalFoodProduced
        +int soldiersLost
    }
    
    class City {
        +String name
        +int food
        +int soldiers
        +boolean actionUsedThisTurn
    }
    
    class MapEdge {
        +int travelDistance
    }
    
    class Army {
        +int soldiers
        +int departureTurn
        +int arrivalTurn
        +ArmyStatus status
    }
    
    class Marshal {
        +String name
        +int foodProduction
        +int soldierProduction
    }
    
    class GameEvent {
        +int turnNumber
        +EventType eventType
        +String description
    }
    
    class TurnAction {
        +ActionType actionType
        +int soldiersCount
    }

    Game "1" *-- "many" Player : has
    Game "1" *-- "many" City : contains
    Game "1" *-- "many" MapEdge : contains
    Game "1" *-- "many" GameEvent : records
    
    Player "1" -- "1" PlayerStats : owns
    Player "1" -- "many" City : controls
    Player "1" -- "many" Army : commands
    Player "many" -- "1" Marshal : selects
    
    City "1" -- "many" MapEdge : connected_by
    City "1" -- "many" Army : targeted_by
    
    TurnAction "many" -- "1" Game : belongs_to
    TurnAction "many" -- "1" Player : issued_by
```
