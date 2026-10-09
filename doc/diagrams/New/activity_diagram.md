# Activity Diagram

> **คืออะไร (What is this?)**
> แผนภาพแสดงลำดับขั้นตอนการทำงานของระบบ (Workflow/Algorithm) ที่มีความซับซ้อนและมีเงื่อนไขการตัดสินใจ (Decision)
> 
> **ใช้ทำไมและเพื่ออะไร (Why & Purpose)**
> ใช้อธิบายลอจิกส่วนที่ซับซ้อนที่สุดของเกม คือช่วงประมวลผลเมื่อจบเทิร์น (Resolve Turn) เพื่อให้ทีมพัฒนาเห็นภาพรวมว่าต้องคำนวณอะไรก่อน-หลัง (เช่น สุ่มอีเวนต์ -> เดินทัพ -> หักเสบียง) ป้องกันการเขียนลำดับโค้ดผิดพลาด

## Turn Resolution Process
แสดงการทำงานของระบบเมื่อผู้เล่นทุกคนกดส่งคำสั่งครบ และระบบทำการคำนวณผลลัพธ์ของเทิร์น (Resolve Turn)

```mermaid
flowchart TD
    Start([Start Turn Resolution]) --> CheckPlayers{Are all active players ready?}
    CheckPlayers -- No --> End([Wait for players])
    CheckPlayers -- Yes --> GenerateEvents[Process Random Events for Cities & Armies]
    
    GenerateEvents --> MoveArmies[Advance traveling armies]
    MoveArmies --> Arrived{Any armies arrived?}
    
    Arrived -- Yes --> ResolveBattles[Resolve Battles / Reinforcements]
    ResolveBattles --> EndGameCheck
    Arrived -- No --> EndGameCheck
    
    EndGameCheck{Is only 1 player alive?}
    EndGameCheck -- Yes --> SetWinner[Set Game Status to FINISHED]
    SetWinner --> SaveGame[Save Game]
    SaveGame --> End
    
    EndGameCheck -- No --> CheckSeasonEnd{Is it end of Season?}
    
    CheckSeasonEnd -- Yes --> ApplyUpkeep[Apply Season Upkeep / Food Consumption]
    CheckSeasonEnd -- No --> ResetActions
    
    ApplyUpkeep --> CheckStarvation{Enough Food?}
    CheckStarvation -- Yes --> DeductFood[Deduct Food from Network]
    CheckStarvation -- No --> StarveSoldiers[Starve Soldiers Proportionally & Log Event]
    DeductFood --> ResetActions
    StarveSoldiers --> ResetActions
    
    ResetActions[Reset action_used_this_turn flags] --> IncrementTurn[Increment Turn Number]
    IncrementTurn --> SaveGame2[Save Game]
    SaveGame2 --> End
```
