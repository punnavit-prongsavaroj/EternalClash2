# Design Patterns Checklist

## 1. Enterprise / Architectural Patterns
| Pattern | ปัญหาที่แก้ / เหตุผลที่เลือกใช้ | ไฟล์/คลาสที่ใช้ |
|---|---|---|
| **Layered Architecture** | แยกส่วน Presentation, Business Logic และ Data Access ออกจากกันเพื่อให้อ่านและดูแลโค้ดง่าย | โครงสร้างแพ็กเกจ `controller`, `service`, `repository` |
| **MVC** | แยกการจัดการฝั่งหน้าจอ (UI) ออกจากส่วนจัดการข้อมูล (API) ผ่านการ Response เป็น JSON | `App.jsx` (View), `GameController.java` (Controller) |
| **Repository Pattern** | ครอบการทำงานของการเข้าถึง Database ไม่ให้ SQL/JPA รั่วไหลไปถึงชั้น Service | `GameRepository.java` ฯลฯ |
| **Service Layer Pattern** | เป็นจุดศูนย์กลางรวม Business Logic และ Transaction Boundary ที่ซับซ้อน เช่น การคำนวณผลสู้รบ | `TurnService.java`, `BattleService.java` |
| **DTO Pattern** | ป้องกันการส่ง Entity เต็มๆ กลับไปยัง Client ซึ่งอาจมีข้อมูลลับหรือก่อให้เกิด Infinite Recursion จาก Hibernate | `GameDto.java`, `GameSnapshotDto.java` |
| **Dependency Injection** | ควบคุมการสร้าง Object จากภายนอก ลดการผูกติด (Coupling) ทำให้สลับไปใช้ Mock ตอน Test ได้ง่าย | แทบทุก Service (`@RequiredArgsConstructor`) |

## 2. GoF Patterns (Behavioral & Creational & Structural)
| Pattern | ปัญหาที่แก้ / เหตุผลที่เลือกใช้ | ไฟล์/คลาสที่ใช้ |
|---|---|---|
| **State (Behavioral)** | จัดการลอจิกที่เปลี่ยนไปตามสถานะของเกม (เช่น รอคนเข้าห้อง vs เริ่มเกมแล้ว) ช่วยกำจัด `if-else` ที่ซ้อนกันหลายชั้น | โฟลเดอร์ `state/` (`GameStateContext`, `WaitingPhaseState`, `PlayPhaseState`) |
| **Command (Behavioral)** | แพ็กเกจคำสั่งต่างๆ ของผู้เล่น (เช่น เกณฑ์ทหาร, โจมตี) เป็นคลาสๆ ไป เพื่อให้ระบบรองรับ Action ใหม่ๆ ได้ง่ายโดยไม่กระทบโค้ดเดิม | โฟลเดอร์ `command/` (`PlayerActionCommand`, `SendArmyCommand`) |
| **Strategy (Behavioral)** | แยกพฤติกรรมที่มีหลายรูปแบบออกจากคลาสหลัก เช่น สกิลการโจมตีที่ต่างกันของแต่ละขุนพล | โฟลเดอร์ `strategy/` (`CombatStrategy`, `KongmingCombatStrategy`) |
| **Factory (Creational)** | ใช้ร่วมกับ Command Pattern เพื่อทำหน้าที่เป็น "โรงงาน" คอยปั้นคลาส Command ให้ตรงกับ Request ที่ส่งเข้ามา | `CommandFactory.java` |
| **Builder (Creational)** | แก้ปัญหา Constructor ที่มีพารามิเตอร์เยอะเกินไป ทำให้การสร้าง Object ได้สะอาด ลดข้อผิดพลาด | `Game.java`, `Player.java` (`@Builder` ของ Lombok) |
| **Facade (Structural)** | รวบรวมข้อมูลจากหลาย Repository (Game, Player, City, Army) ออกมาเป็น Snapshot ก้อนเดียวจบ ลดภาระ Client | `GameSnapshotDto.java` และ `GameViewService.java` (ถ้ามี) |
