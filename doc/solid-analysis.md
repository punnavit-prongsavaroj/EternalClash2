# การวิเคราะห์ SOLID Principles

1. **S - Single Responsibility Principle**
   - **คลาส/ไฟล์:** code/src/main/java/com/eternalclash2/service/GameClock.java
   - **เหตุผล:** คลาสนี้มีหน้าที่เดียวคือคำนวณเวลาและฤดูกาลของเกมผ่านเลขเทิร์น โดยไม่ยุ่งเกี่ยวกับการต่อ Database หรือ Validation

2. **O - Open/Closed Principle**
   - **คลาส/ไฟล์:** TurnAction.java และ ActionType Enum
   - **เหตุผล:** การออกแบบ Action ของเกมใช้ Enum ร่วมกับการทำ Polymorphism แบบอ้อม (ตรวจสอบ type ใน Service) ทำให้สามารถเพิ่ม Action ใหม่ๆ ได้โดยไม่ต้องแก้โค้ดหลักของ Entity TurnAction

3. **L - Liskov Substitution Principle**
   - **คลาส/ไฟล์:** คลาสที่สืบทอดจาก RuntimeException (เช่น BusinessLogicException.java)
   - **เหตุผล:** คลาส Custom Exception สามารถนำไปใช้โยน (throw) แทน RuntimeException พื้นฐานของ Java ได้โดยไม่ทำให้โปรแกรมพังหรือเปลี่ยนพฤติกรรมการดักจับของ @RestControllerAdvice

4. **I - Interface Segregation Principle**
   - **คลาส/ไฟล์:** Repository Interfaces เช่น PlayerRepository.java, GameRepository.java
   - **เหตุผล:** มีการแยก Interface ของแต่ละ Entity อย่างชัดเจน (ไม่ได้รวมฟังก์ชันทุก Entity ไว้ใน Repository เดียว) ทำให้ Service เลือกใช้เฉพาะ Repository ที่ตัวเองจำเป็นต้องใช้เท่านั้น ลด Coupling

5. **D - Dependency Inversion Principle**
   - **คลาส/ไฟล์:** GameService.java
   - **เหตุผล:** ใช้ Constructor Injection (ผ่าน @RequiredArgsConstructor ของ Lombok) ในการรับค่า Repository Interface เข้ามาทำงาน ทำให้ Service ผูกติดกับ Abstraction ไม่ใช่ Concrete Class จึงเขียน Test (Mock) ได้ง่าย
