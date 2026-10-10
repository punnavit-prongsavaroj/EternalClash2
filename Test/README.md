# เอกสาร Test Suite — EternalClash2

ไฟล์นี้เล่า 3 อย่าง: test suite เก็บอะไรไว้, รันยังไง, และผลล่าสุดตามตัวเลขที่บันทึกใน `TestSuiteSummary.xlsx` ตัวเลขทุกตัวในนี้อ่านซ้ำได้จากไฟล์นั้นไฟล์เดียว ไม่ต้องเปิดไฟล์อื่นเทียบ

## 1. โครงสร้าง

```
Test/
├─ README.md                       ← ไฟล์นี้
├─ TestSuiteSummary.xlsx           ← Summary (สรุปต่อคลาส + coverage), DefectSummary (defect ทุกไฟล์รวมกัน), CoverageGap (ช่องว่างการทดสอบ)
└─ Test Suite/
   ├─ TestPlanPrincipleService.xlsx      ← 11 class sheet + Summary
   ├─ TestPlanPrincipleController.xlsx   ← 7 class sheet + Summary
   ├─ TestPlanPrincipleCommand.xlsx      ← 5 class sheet + Summary
   └─ TestPlanPrincipleStrategy.xlsx     ← 4 class sheet + Summary
```

xlsx หนึ่งไฟล์ = หนึ่ง package ใน `code/src/test/java/com/eternalclash2` รวม 27 class sheet / 212 test case

## 2. ใน test suite เก็บอะไรไว้บ้าง

แต่ละ class sheet บันทึกการทดสอบหนึ่งคลาสไว้ดังนี้

- คลาสที่ถูกทดสอบ และ **Function Under Test** — เมธอดไหนของคลาสนั้น
- **TestData** — call ที่เรียกจริงพร้อมพารามิเตอร์ และ mock ที่ต้องตั้ง (`when(...)` / `verify(...)`)
- **Expected Result** — ผลที่ระบบควรได้ ตาม requirement ที่ใช้ออกแบบเทสต์ชุดนั้น
- **Actual Result** — ค่าที่ได้จริงตอนรัน (พร้อม `file:line` ของโค้ดหรือของเทสต์)
- **Status** และ **DefectID** — ผ่าน/ไม่ผ่าน และรหัส defect ที่เกี่ยวข้อง (DEF-SVC-nn, DEF-CTL-nn, DEF-CMD-nn, DEF-STR-nn)
- หัว sheet บอกบริบทของครั้งนั้น: ชื่อโปรเจกต์, Class under Test, Environment, วันที่ และ Requirement

sheet `Summary` ของแต่ละไฟล์รวมจำนวนเทสต์/Pass/Fail/defect **ต่อ function** ของคลาสในไฟล์นั้น และปิดท้ายด้วยแถว Total หนึ่งแถว

TestCaseID `TCnn` ผูก 1:1 กับ method `tcnn_<behaviour>` ในไฟล์เทสต์ เช่น `TC09` ↔ `tc09_resolveAndAdvance_beforeTheBattlePhase_isRejectedByTheStateMachine` ใน `TurnServiceTest.java` — 212 แถว = 212 method

**กติกาของ Status:** `Fail` ใน sheet หมายถึงค่าที่ได้จริงขัดกับ Expected Result ที่เขียนไว้เท่านั้น ไม่ได้แปลว่า JUnit ล้ม เพราะตัวเทสต์ถูกเขียนให้ assert ค่าตามที่โค้ดทำจริง (เพื่อไม่ให้ suite แดง) ผลคือ excel มีแถว Fail ได้ ขณะที่ `mvnw test` ยังผ่านครบทุกตัว

## 3. วิธีรัน

```
cd .\EternalClash2\code
set JAVA_HOME=C:\Program Files\Java\jdk-17
.\mvnw.cmd test
```

## 4. ผลรันล่าสุดและ defect

รันเมื่อ 2026-10-10 13:46 บน commit `f9308f5` — **Tests run: 213, Failures: 0, Errors: 0, Skipped: 0, BUILD SUCCESS**

213 ≠ 212 เพราะ `EternalClash2ApplicationTests` (เทสต์โหลด Spring context) ไม่มี class sheet ของตัวเอง: 212 แถว + 1 = 213

| folder | class sheet | test case | Pass | Fail | DefectID |
| --- | --- | --- | --- | --- | --- |
| service | 11 | 147 | 142 | 5 | DEF-SVC-01…06 |
| controller | 7 | 28 | 26 | 2 | DEF-CTL-01, DEF-CTL-02 |
| command | 5 | 17 | 17 | 0 | DEF-CMD-01 |
| strategy | 4 | 20 | 20 | 0 | — |
| **Total** | **27** | **212** | **205** | **7** | **9 ตัว** |

defect ทั้ง 9 ตัวแบ่งได้ 4 กลุ่ม

- **บังคับการสุ่มไม่ได้** — เรียก `ThreadLocalRandom` ตรง ๆ ไม่มี seam ให้ inject (DEF-SVC-01, DEF-CMD-01) ทางออกที่ใช้ในเทสต์คือรันซ้ำหลายรอบ (60 / 300 ครั้ง) แล้ว assert เฉพาะครั้งที่ branch เกิดขึ้นจริง
- **Expected Result ใน sheet เขียนไม่ตรงกับโค้ด** — DEF-SVC-02, DEF-SVC-03, DEF-SVC-04, DEF-SVC-05 กลุ่มนี้คือแถวใน sheet ที่ต้องแก้ ไม่ใช่โค้ด
- **โค้ดขาดการตรวจเงื่อนไข** — DEF-SVC-06 (roster ว่างแต่เกมเปลี่ยนเป็น IN_PROGRESS ได้)
- **API contract ไม่ตรงกับที่ระบุไว้ใน sheet** — DEF-CTL-01 (`BusinessLogicException` ตอบ 409 แต่ sheet เขียน 400), DEF-CTL-02 (`PlacementController` รับ `@RequestParam` แต่ sheet ส่งเป็น JSON body)

รายละเอียดแต่ละตัวพร้อม `file:line` และค่าที่ observed จริงรวมไว้ที่ sheet `DefectSummary` ของ `TestSuiteSummary.xlsx` (9 แถว) ส่วน sheet `DefectSummary` ในไฟล์ราย package มีเฉพาะ defect ของตัวเอง

## 5. Coverage

ตัวเลข coverage ที่อยู่ใน `TestSuiteSummary.xlsx` เป็น **line coverage จาก JaCoCo** — นับจำนวนบรรทัดโค้ดจริงที่มีอย่างน้อยหนึ่งเทสต์วิ่งผ่าน ไม่ใช่ % จำนวนเทสต์ที่ผ่าน และไม่ใช่ตัวเลขที่ VS Code แสดงใน panel (อันนับเป็น instruction)

เรียก line coverage ตามที่เครื่องมือวัดจริง เพราะ JaCoCo ไม่มีตัวนับที่ชื่อว่า statement — สิ่งที่รายงานมีเพียง instruction, branch, line, method, class และการนับ statement แบบเข้มงวดต้องใช้เครื่องมือ instrument คนละแบบ (ในโค้ดโปรเจกต์นี้มีทั้งบรรทัดที่ใส่หลาย statement และ statement ที่พาดหลายบรรทัด) ตัวเลขนี้จึงเป็นค่าที่ใกล้เคียง statement coverage ไม่ใช่ค่าเดียวกัน

ตัวเลขทั้งหมดยืนยันซ้ำได้ใน `TestSuiteSummary.xlsx` (ต่อคลาส + ทั้งโปรเจกต์) และบน sheet `Summary` ของไฟล์ราย package (ต่อ folder) มี 3 ชั้น

- **ต่อคลาส** — หนึ่งค่าต่อหนึ่งคลาสที่ถูกทดสอบ เช่น `ArmyService` 95.12 คือจาก 41 บรรทัด มี 39 บรรทัดที่เทสต์วิ่งผ่าน; 18 คลาสได้ 100 และต่ำสุดคือ `TurnController` 50
- **ต่อ folder** — command 96.55, controller 90.00, service 93.03, strategy 100.00 คิดจากจำนวนบรรทัดจริงของคลาสใน folder นั้น ไม่ใช่ค่าเฉลี่ยของตัวเลขต่อคลาส
- **ทั้งโปรเจกต์** — 77.42% คือ 792 บรรทัดจาก 1,023 บรรทัดที่ JaCoCo วิเคราะห์ใน 66 คลาส

เฉพาะ 26 คลาสที่ suite นี้เล็งไว้ ได้ line coverage รวม 93.48% (659/705 บรรทัด) — ต่างจาก 77.42 เพราะอีก 40 types ใน `src/main/java` (DTO, enum, entity, repository, snapshot ของ `GameViewService`) ถูกรวมเข้าในการคำนวณทั้งโปรเจกต์ด้วย

- `GameEndpointTest` ไม่มีตัวเลข เพราะหนึ่งคลาสเทสต์ครอบคลุม controller 5 ตัวพร้อมกัน
- ถ้าต้องการ **branch coverage** ของรอบเดียวกัน: ทั้งโปรเจกต์ 59.66%, เฉพาะ 26 คลาสที่เทสต์ 79.47% — ยังไม่ได้บันทึกไว้ในไฟล์

ที่มาของตัวเลข: `.\mvnw.cmd test` รัน `jacoco-maven-plugin` 0.8.12 พร้อมสร้างรายงานเป็นไฟล์ใน `code\target\site\jacoco\` (`jacoco.csv` ตารางต่อคลาส, `index.html` เปิดดูทีละคลาสแบบไฮไลต์บรรทัด) — target ไม่ถูกเก็บลง git ดังนั้นตัวเลขที่คัดมาไว้ใน excel คือสิ่งที่ใช้ส่งงาน

หมายเหตุสำหรับคนในทีม: การเปิด JaCoCo ต้องแก้ `argLine` ของ `maven-surefire-plugin` เป็น `@{argLine} -XX:+EnableDynamicAgentLoading -Xshare:off` ด้วย เพราะถ้า hardcode ไว้แบบเดิม agent ของ JaCoCo จะไม่ถูก attach และรายงานจะออกมานิ่ง ๆ เป็น 0% ทุกคลาส

## 6. ช่องว่างการทดสอบ

ช่องว่างทั้งหมดถูกวิเคราะห์ไว้ใน sheet `CoverageGap` ของ `TestSuiteSummary.xlsx` — ใน 86 types ของ `src/main/java` มี 27 ตัวที่มี `*Test` ตรงชื่อ แต่อีก 59 ตัวที่ไม่มีไม่ได้แปลว่าทดสอบไม่ถึงทั้งหมด: ส่วนเป็น DTO, enum, entity, repository และ interface ที่ไม่มี branch ให้ทดสอบ เหลือ **5 ตัวที่โค้ดจริงไม่เคยทำงาน** — service 4 ตัว (`GameViewService`, `GameEventService`, `MarshalService`, `MarshalCandidateService`) ที่โผล่ในเทสต์ในฐานะ `@Mock`/`@MockBean` เท่านั้น และ `FinishedPhaseState` ที่ branch `throw` ไม่เคยถูกเรียก เพราะ `TurnService.java:36` สร้าง state machine จากเกมที่โหลดด้วย `findByIdForUpdate` ส่วนเกมสถานะ FINISHED ถูกโหลดทีหลัง
