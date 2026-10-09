# Sequence Diagrams

> **คืออะไร (What is this?)**
> แผนภาพแสดงลำดับการทำงาน (Flow of Messages) ระหว่าง Object หรือ Layer ต่างๆ ตามเส้นเวลา (Timeline) เพื่อทำภารกิจหนึ่งๆ ให้สำเร็จ
> 
> **ใช้เทคนิคอะไร (Techniques Used)**
> แสดงให้เห็นถึงการสื่อสารในรูปแบบ Layered Architecture (Client -> Controller -> Service -> Repository -> Database) ในแต่ละ Request ที่เข้ามา
> 
> **ใช้ทำไมและเพื่ออะไร (Why & Purpose)**
> ใช้เพื่อออกแบบลอจิกการทำงานเชิงลึกในแต่ละ Use Case ทำให้รู้ว่าเวลามี Request เข้ามา จะต้องเรียกคลาสไหน เมธอดอะไร ใครคุยกับใครบ้าง และส่งข้อมูลอะไรกลับไป ช่วยให้เห็นภาพรวมของ Data Flow และลดบั๊กระหว่างการเขียนโค้ดจริง

## Scenario 1: Create Game and Join
```mermaid
sequenceDiagram
    actor Host
    actor Guest
    participant Client as Frontend (App.js)
    participant GameCtrl as GameController
    participant GameSvc as GameService
    participant DB as Database

    Host->>Client: Click "Create Game"
    Client->>GameCtrl: POST /api/games
    GameCtrl->>GameSvc: createGame(name)
    GameSvc->>DB: save(Game)
    DB-->>GameSvc: Game (id, code)
    GameSvc-->>GameCtrl: GameDto
    GameCtrl-->>Client: 200 OK (GameDto)
    Client-->>Host: Show Game Room (Code)

    Guest->>Client: Enter Code & Join
    Client->>GameCtrl: POST /api/games/code/{code}/players
    GameCtrl->>GameSvc: joinGame(code, name)
    GameSvc->>DB: save(Player)
    GameSvc-->>GameCtrl: PlayerDto
    GameCtrl-->>Client: 200 OK
    Client-->>Guest: Enter Game Room
```

## Scenario 2: Produce Food Action
```mermaid
sequenceDiagram
    actor Player
    participant Client as Frontend (api.js)
    participant TurnCtrl as TurnController
    participant ActionSvc as TurnActionService
    participant CitySvc as CityService
    participant DB as Database

    Player->>Client: Select City -> "Produce Food"
    Client->>TurnCtrl: POST /api/games/{id}/actions
    TurnCtrl->>ActionSvc: submitAction(...)
    ActionSvc->>CitySvc: produceFood(cityId)
    CitySvc->>DB: findById(cityId)
    DB-->>CitySvc: City
    CitySvc->>CitySvc: Calculate bonus (Marshal/Season)
    CitySvc->>CitySvc: city.setFood(food + bonus)
    CitySvc->>DB: save(City)
    DB-->>CitySvc: Updated City
    ActionSvc-->>TurnCtrl: Action Result
    TurnCtrl-->>Client: 200 OK
```

## Scenario 3: Send Army to Attack
```mermaid
sequenceDiagram
    actor Player
    participant Client as Frontend
    participant TurnCtrl as TurnController
    participant ActionSvc as TurnActionService
    participant ArmySvc as ArmyService
    participant DB as Database

    Player->>Client: Select Target City -> "Send Army"
    Client->>TurnCtrl: POST /api/games/{id}/actions (SEND_ARMY)
    TurnCtrl->>ActionSvc: submitAction(...)
    ActionSvc->>ArmySvc: sendArmy(from, to, count)
    ArmySvc->>DB: find City (from), City (to)
    ArmySvc->>ArmySvc: Calculate travel turns (Graph distance)
    ArmySvc->>ArmySvc: Deduct soldiers from source city
    ArmySvc->>DB: save(City)
    ArmySvc->>DB: save(Army Entity [Status=TRAVELING])
    ArmySvc-->>ActionSvc: Army Created
    ActionSvc-->>TurnCtrl: Success
    TurnCtrl-->>Client: 200 OK
```
