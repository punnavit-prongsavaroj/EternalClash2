# State Diagrams

> **คืออะไร (What is this?)**
> แผนภาพแสดงการเปลี่ยนแปลงสถานะ (State Transition) ของออบเจกต์หนึ่งๆ ตลอดวงจรชีวิต (Lifecycle)
> 
> **ใช้เทคนิคอะไร (Techniques Used)**
> Finite State Machine (FSM) ในการควบคุมสถานะไม่ให้ข้ามขั้นตอนอย่างผิดกฎ เช่น กองทัพต้องเริ่มเดินทางก่อน ถึงจะเข้าปะทะได้
> 
> **ใช้ทำไมและเพื่ออะไร (Why & Purpose)**
> ใช้เพื่อออกแบบลอจิกควบคุม Flow ของเกม (Game Status) เช่น ต้องรอให้ผู้เล่นสุ่มขุนพล (MARSHAL_SELECTION) ให้ครบก่อน ถึงจะไปสเตปวางกำลัง (PLACEMENT) ได้ ช่วยป้องกันบั๊กการทำแอ็กชันข้ามขั้นตอน

## Game State Machine
แสดงการเปลี่ยนสถานะของเกม (Game Entity) ตั้งแต่เริ่มสร้างห้องจนจบเกม

```mermaid
stateDiagram-v2
    [*] --> WAITING : Game Created
    WAITING --> MARSHAL_SELECTION : Players Ready / Start Game
    MARSHAL_SELECTION --> PLACEMENT : All Players Drafted Marshal
    PLACEMENT --> IN_PROGRESS : All Players Placed Initial Army
    IN_PROGRESS --> IN_PROGRESS : Resolve Turn / Next Turn
    IN_PROGRESS --> FINISHED : Win Condition Met (Only 1 player alive)
    FINISHED --> [*]
```

## Army State Machine
แสดงการเปลี่ยนสถานะของกองทัพ (Army Entity) เมื่อถูกส่งออกไปโจมตีหรือสนับสนุน

```mermaid
stateDiagram-v2
    [*] --> TRAVELING : Send Army Command
    TRAVELING --> TRAVELING : Turn Advances (Distance decreases)
    TRAVELING --> CANCELLED : Hit by Event (All soldiers died during travel)
    TRAVELING --> ARRIVED : Reached Target City (Battle/Reinforce)
    ARRIVED --> DESTROYED : Battle Resolved (Army consumed)
    CANCELLED --> [*]
    DESTROYED --> [*]
```
