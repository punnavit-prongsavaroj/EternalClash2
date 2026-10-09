# Design Patterns Checklist

## 1. Enterprise / Architectural Patterns
| Pattern | ปัญหาที่แก้ / เหตุผลที่เลือกใช้ | ไฟล์/คลาสที่ใช้ | Class Diagram ประกอบ |
|---|---|---|---|
| **Layered Architecture** | แยกส่วน Presentation, Business Logic และ Data Access ออกจากกันเพื่อให้อ่านและดูแลโค้ดง่าย | โครงสร้างแพ็กเกจ controller, service, repository | ![Class Diagram](diagrams/ClassDiagram.png) |
| **MVC** | แยกการจัดการฝั่งหน้าจอ (UI) ออกจากส่วนจัดการข้อมูล (API) ผ่านการ Response เป็น JSON | App.js (View), GameController.java (Controller) | ![Class Diagram](diagrams/ClassDiagram.png) |
| **Repository Pattern** | ครอบการทำงานของการเข้าถึง Database ไม่ให้ SQL/JPA รั่วไหลไปถึงชั้น Service | GameRepository.java ฯลฯ | ![Class Diagram](diagrams/ClassDiagram.png) |
| **Service Layer Pattern** | เป็นจุดศูนย์กลางรวม Business Logic และ Transaction Boundary ที่ซับซ้อน เช่น การคำนวณผลสู้รบ | TurnService.java, BattleService.java | ![Class Diagram](diagrams/ClassDiagram.png) |
| **DTO Pattern + Mapper** | ป้องกันการส่ง Entity เต็มๆ กลับไปยัง Client ซึ่งอาจมีข้อมูลลับหรือก่อให้เกิด Infinite Recursion จาก Hibernate | GameDto.java, PlayerDto.java | ![Class Diagram](diagrams/ClassDiagram.png) |
| **Dependency Injection** | ควบคุมการสร้าง Object จากภายนอก ลดการผูกติด (Coupling) ทำให้สลับไปใช้ Mock ตอน Test ได้ง่าย | แทบทุก Service (@RequiredArgsConstructor) | ![Class Diagram](diagrams/ClassDiagram.png) |

## 2. GoF Patterns
| Pattern | ปัญหาที่แก้ / เหตุผลที่เลือกใช้ | ไฟล์/คลาสที่ใช้ | Class Diagram ประกอบ |
|---|---|---|---|
| **Builder (Creational)** | แก้ปัญหา Constructor ที่มีพารามิเตอร์เยอะเกินไป ทำให้การสร้าง Object ได้สะอาด ลดข้อผิดพลาด | Game.java, Player.java (@Builder) | ![Class Diagram](diagrams/ClassDiagram.png) |
| **Facade (Structural)** | รวบรวมข้อมูลจากหลาย Repository (Game, Player, City, Army) ออกมาเป็น Snapshot ก้อนเดียวจบ ลดภาระ Client | GameViewService.java | ![Class Diagram](diagrams/ClassDiagram.png) |
| **Strategy (Behavioral)** | ใช้คำนวณลอจิกสภาพอากาศตามเทิร์น เป็นการแยกอัลกอริทึมออกมาจากตัว Entity อย่างชัดเจน | GameClock.java (พฤติกรรมตามเวลา) | ![Class Diagram](diagrams/ClassDiagram.png) |
