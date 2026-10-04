# State Diagrams

ในระบบเกม EternalClash2 มี Entity หลักที่มีการเปลี่ยนแปลงสถานะ (State) อย่างชัดเจนระหว่างการทำงานของระบบ จำนวน 2 Entity ได้แก่ `Game` และ `Army`

## 1. Game State Diagram
วงจรชีวิตของ "ห้องเกม" ตั้งแต่เริ่มต้นสร้างห้อง ไปจนถึงหาผู้ชนะได้

```mermaid
stateDiagram-v2
    direction LR
    
    [*] --> WAITING : สร้างเกมใหม่
    
    WAITING --> MARSHAL_SELECTION : ผู้เล่นครบ / กดเริ่มเกม
    
    MARSHAL_SELECTION --> IN_PROGRESS : ผู้เล่นทุกคนเลือกจอมพลเสร็จ
    
    IN_PROGRESS --> FINISHED : เหลือเมืองสุดท้ายเพียง 1 เมือง
    
    FINISHED --> [*]
    
    note right of WAITING
      รอผู้เล่น Join (2-7 คน)
    end note
    
    note right of MARSHAL_SELECTION
      สุ่ม Reroll เลือกจอมพลทีละคน
    end note
    
    note right of IN_PROGRESS
      เข้าสู่ระบบ Turn-based
      ส่งคำสั่ง Action และเกิด Event
    end note
```

---

## 2. Army State Diagram
วงจรชีวิตของ "กองทัพ" ตั้งแต่ถูกส่งออกจากเมือง จนถึงจุดหมายหรือถูกทำลายกลางทาง

```mermaid
stateDiagram-v2
    direction TB
    
    [*] --> TRAVELING : ผู้เล่นใช้ Action "ส่งกองทัพ"
    
    TRAVELING --> DESTROYED : โดน Event ทำลายหมด / แพ้การปะทะกลางทาง
    TRAVELING --> CANCELLED : เมืองเป้าหมายถูกทำลายไปก่อน / เกมจบ
    TRAVELING --> ARRIVED : เดินทางครบกำหนด Turn
    
    ARRIVED --> [*] : เข้าสู่กระบวนการต่อสู้ (ถูกลบออกจากกระดาน)
    DESTROYED --> [*]
    CANCELLED --> [*]
    
    note right of TRAVELING
      หักเสบียงระหว่างทาง
      เสี่ยงโดน Event นอกเมือง 10%
    end note
    
    note right of ARRIVED
      นำจำนวนทหารไปคำนวณ Combat 
      ร่วมกับกองทัพอื่นที่มาถึงพร้อมกัน
    end note
```
