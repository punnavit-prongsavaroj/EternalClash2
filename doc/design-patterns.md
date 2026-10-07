# Design Patterns Checklist

## 1. Enterprise / Architectural Patterns
| Pattern | ไฟล์/คลาสที่ใช้ | ปัญหาที่แก้ / เหตุผลที่เลือกใช้ |
|---|---|---|
| **Layered Architecture** | โครงสร้างแพ็กเกจ controller, service, 
epository | แยกส่วน Presentation, Business Logic และ Data Access ออกจากกันเพื่อให้อ่านและดูแลโค้ดง่าย |
| **MVC** | pp.js (View), GameController.java (Controller) | แยกการจัดการฝั่งหน้าจอ (UI) ออกจากส่วนจัดการข้อมูล (API) ผ่านการ Response เป็น JSON |
| **Repository Pattern** | GameRepository.java ฯลฯ | ครอบการทำงานของการเข้าถึง Database ไม่ให้ SQL/JPA รั่วไหลไปถึงชั้น Service |
| **Service Layer Pattern** | TurnService.java, BattleService.java | เป็นจุดศูนย์กลางรวม Business Logic และ Transaction Boundary ที่ซับซ้อน เช่น การคำนวณผลสู้รบ |
| **DTO Pattern + Mapper** | GameDto.java, PlayerDto.java | ป้องกันการส่ง Entity เต็มๆ กลับไปยัง Client ซึ่งอาจมีข้อมูลลับหรือก่อให้เกิด Infinite Recursion จาก Hibernate |
| **Dependency Injection** | แทบทุก Service (@RequiredArgsConstructor) | ควบคุมการสร้าง Object จากภายนอก ลดการผูกติด (Coupling) ทำให้สลับไปใช้ Mock ตอน Test ได้ง่าย |

## 2. GoF Patterns
| Pattern | ไฟล์/คลาสที่ใช้ | ปัญหาที่แก้ / เหตุผลที่เลือกใช้ |
|---|---|---|
| **Builder (Creational)** | Game.java, Player.java (@Builder) | แก้ปัญหา Constructor ที่มีพารามิเตอร์เยอะเกินไป ทำให้การสร้าง Object ได้สะอาด ลดข้อผิดพลาด |
| **Facade (Structural)** | GameViewService.java | รวบรวมข้อมูลจากหลาย Repository (Game, Player, City, Army) ออกมาเป็น Snapshot ก้อนเดียวจบ ลดภาระ Client |
| **Strategy (Behavioral)** | GameClock.java (พฤติกรรมตามเวลา) | ใช้คำนวณลอจิกสภาพอากาศตามเทิร์น เป็นการแยกอัลกอริทึมออกมาจากตัว Entity อย่างชัดเจน |
