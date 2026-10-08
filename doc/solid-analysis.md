# การวิเคราะห์ SOLID Principles

1. **S - Single Responsibility Principle (SRP)**
   - **คลาส/ไฟล์:** code/src/main/java/com/eternalclash2/service/GameClock.java
   - **เหตุผล:** คลาสนี้มีหน้าที่เดียวคือคำนวณเวลาและฤดูกาลของเกมผ่านเลขเทิร์น โดยไม่ยุ่งเกี่ยวกับการต่อ Database หรือ Validation
   - **ตัวอย่างโค้ด:**
`java
public class GameClock {
    public static Season season(int turnNumber) {
        int seasonIndex = (turnNumber / 4) % 3;
        return switch (seasonIndex) {
            case 0 -> Season.SUMMER;
            case 1 -> Season.RAINY;
            default -> Season.WINTER;
        };
    }
}
`

2. **O - Open/Closed Principle (OCP)**
   - **คลาส/ไฟล์:** PlayerActionCommand.java และคลาสที่ Implement (เช่น ProduceCommand, SendArmyCommand)
   - **เหตุผล:** ระบบใช้ Command Pattern ทำให้สามารถเพิ่ม Action ใหม่ๆ เข้ามาได้ (เปิดรับการขยาย - Open for extension) โดยการสร้างคลาสใหม่ที่ Implement PlayerActionCommand โดยที่ไม่ต้องไปตามแก้โค้ดหลักเดิม (ปิดการแก้ไข - Closed for modification)
   - **ตัวอย่างโค้ด:**
`java
public interface PlayerActionCommand {
    void execute();
    ActionType getRecordedAction();
}

public class ProduceCommand implements PlayerActionCommand {
    private final CityService cityService;
    private final Long playerId;
    
    @Override
    public void execute() {
        cityService.produceFood(playerId);
    }
    
    @Override
    public ActionType getRecordedAction() {
        return ActionType.PRODUCE;
    }
}
`

3. **L - Liskov Substitution Principle (LSP)**
   - **คลาส/ไฟล์:** BusinessLogicException.java (สืบทอดจาก RuntimeException)
   - **เหตุผล:** คลาส Custom Exception สามารถนำไปใช้โยน (throw) และดักจับใน Global Exception Handler แทนที่ RuntimeException พื้นฐานของ Java ได้อย่างสมบูรณ์แบบ โดยไม่ทำให้โปรแกรมพังหรือเปลี่ยนพฤติกรรมที่ควรจะเป็น
   - **ตัวอย่างโค้ด:**
`java
public class BusinessLogicException extends RuntimeException {
    public BusinessLogicException(String message) {
        super(message);
    }
}

// การใช้งาน (ทำงานได้เหมือน RuntimeException พื้นฐาน)
if (city.getFood() < cost) {
    throw new BusinessLogicException("Not enough food");
}
`

4. **I - Interface Segregation Principle (ISP)**
   - **คลาส/ไฟล์:** PlayerRepository.java, GameRepository.java
   - **เหตุผล:** โครงสร้าง Spring Data JPA ออกแบบโดยแยก Interface ของแต่ละ Entity ออกจากกันอย่างชัดเจน ไม่ได้จับฉ่ายรวมฟังก์ชันเซฟหรือค้นหาของทุกระบบไว้ใน Interface เดียว Service จึงเรียกใช้เฉพาะสิ่งที่ต้องใช้จริงๆ เท่านั้น
   - **ตัวอย่างโค้ด:**
`java
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByGame_IdAndSessionId(Long gameId, String sessionId);
}
`

5. **D - Dependency Inversion Principle (DIP)**
   - **คลาส/ไฟล์:** GameService.java
   - **เหตุผล:** คลาส Service ต่างๆ รับ Dependency ผ่าน Constructor Injection ทำให้ Service ขึ้นต่อ Abstraction (Interface เช่น Repository) ไม่ใช่ Concrete Class (การทำงานรูปธรรม) ทำให้โค้ดยืดหยุ่นและทดสอบได้ง่าย
   - **ตัวอย่างโค้ด:**
`java
@Service
@RequiredArgsConstructor
public class GameService {
    // ขึ้นกับ Interface ไม่ใช่คลาสรูปธรรม (Dependency Inversion)
    private final GameRepository gameRepository; 
    private final PlayerRepository playerRepository;
    
    public Game createGame(String hostName) {
        Game game = new Game();
        game.setStatus(GameStatus.WAITING);
        return gameRepository.save(game);
    }
}
`
