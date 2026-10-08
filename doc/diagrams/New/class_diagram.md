# Class Diagram (Design Patterns Included)

> **คืออะไร (What is this?)**
> แผนภาพแสดงโครงสร้างเชิงลึกของโค้ดโปรแกรม ประกอบด้วยคลาส อินเตอร์เฟซ และความสัมพันธ์ระหว่างคลาสในเชิงเทคนิค
> 
> **ใช้เทคนิคอะไร (Techniques Used)**
> - **Architectural Patterns**: Layered Architecture, MVC, Repository, DTO, Dependency Injection
> - **GoF Design Patterns**: Strategy (GameClock), Facade (GameViewService), Command (PlayerAction), Builder (Entity)
> 
> **ใช้ทำไมและเพื่ออะไร (Why & Purpose)**
> ใช้เป็นคู่มือสำหรับนักพัฒนาในการเขียนโค้ดจริง (Implementation) แผนภาพนี้จะช่วยอธิบายว่าโค้ดโครงสร้างใหญ่นี้ถูกจัดระเบียบด้วย Design Pattern อย่างไร เพื่อให้อ่านง่าย ดูแลรักษาง่าย และขยายขีดความสามารถได้ง่ายในอนาคต (หลักการ SOLID)

แสดงภาพรวมของ Class Architecture และ Design Patterns ที่ใช้ในระบบ (ย่อเฉพาะคลาสสำคัญเพื่อความชัดเจน)

```mermaid
classDiagram
    %% Annotations for Design Patterns
    note for GameController "Controller (MVC/Layered)"
    note for GameService "Service Layer / Facade"
    note for GameRepository "Repository Pattern"
    note for GameDto "DTO Pattern"
    note for GameClock "Strategy (Time/Season)"
    note for Game "Builder Pattern"

    %% Layer 1: Presentation / Controller
    class GameController {
        +createGame(hostName) ResponseEntity
        +joinGame(code, playerName) ResponseEntity
        +getSnapshot() ResponseEntity
    }
    class TurnController {
        +submitAction(...) ResponseEntity
    }

    %% Layer 2: Service Layer (Business Logic)
    class GameService {
        +createGame() Game
        +joinGame() Player
        +startGame() void
    }
    class GameViewService {
        +getGameSnapshot(gameId, playerId) GameSnapshotDto
    }
    class TurnService {
        +resolveAndAdvance(gameId) Game
    }
    class CityService {
        +produceFood() City
        +recruitSoldiers() City
        +applySeasonUpkeep() void
    }
    class GameClock {
        <<Utility/Strategy>>
        +season(turnNumber) Season
        +isSeasonEnd(turnNumber) boolean
    }
    
    %% Command Pattern
    class PlayerActionCommand {
        <<Interface>>
        +execute() void
    }
    class ProduceCommand {
        +execute() void
    }
    class SendArmyCommand {
        +execute() void
    }

    %% Layer 3: Repository Layer (Data Access)
    class GameRepository {
        <<Interface>>
        +findById() Optional
        +save() Game
    }
    class CityRepository {
        <<Interface>>
        +findByGame_Id() List
    }

    %% Layer 4: Domain & DTO
    class Game {
        <<Entity>>
    }
    class GameDto {
        <<DTO>>
    }

    %% Relationships
    GameController --> GameService : Injects (Dependency Injection)
    GameController --> GameViewService : Injects
    TurnController --> TurnService : Injects
    
    GameService --> GameRepository : Uses
    CityService --> CityRepository : Uses
    TurnService --> CityService : Orchestrates
    
    GameViewService ..> GameDto : Maps Entity to DTO
    
    TurnService --> GameClock : Uses Strategy
    
    PlayerActionCommand <|.. ProduceCommand : Implements
    PlayerActionCommand <|.. SendArmyCommand : Implements
    ProduceCommand --> CityService : Uses
```
