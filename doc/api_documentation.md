# API Documentation — EternalClash2

> เอกสารอ้างอิง REST API ของฝั่ง Backend (Spring Boot) เรียบเรียงจากโค้ดจริงใน
> `code/src/main/java/com/eternalclash2/controller` และ service ที่ controller เรียกใช้
> ทุก path, parameter, status code และข้อความ error ในเอกสารนี้คัดมาจากโค้ด ไม่ใช่การคาดเดา
>
> \*\*สถานะเอกสาร:\*\* อ้างอิงตาม implementation (as-built)
> \*\*เวอร์ชันโค้ด:\*\* commit `f9308f5` · \*\*วันที่:\*\* 2026-10-10

\---

## 1\. ภาพรวม

|เรื่อง|ค่า|
|-|-|
|Base URL|`http://localhost:8080/api` (`server.port=8080` ใน `application.properties`)|
|รูปแบบ|REST, request/response เป็น JSON (UTF-8)|
|Authentication|**ไม่มี** ในเวอร์ชันนี้ — ไม่มีการตรวจสอบตัวตนหรือสิทธิ์ในทุก endpoint|
|Controller|`GameController` · `PlayerController` · `MarshalDraftController` · `PlacementController` · `TurnController` · `GameLogController`|
|จำนวน endpoint|18|
|OpenAPI spec|เปิดดูได้ที่ `http://localhost:8080/swagger-ui/index.html` และ `/v3/api-docs`|

ข้อมูลที่ฝั่ง client ต้องถือไว้ตลอดคือ `gameId`, `playerId`, `cityId` (ทั้งหมดเป็น `Long` ของฐานข้อมูล) และ `roomCode` (string 6 ตัวอักษร A–Z, 0–9)

\---

## 2\. กติการ่วม

### 2.1 โครงสร้างคำขอผิดพลาด (error response)

exception ที่หลุดจาก service จะถูก `GlobalExceptionHandler` (`@RestControllerAdvice`) จัดเป็น JSON โครงเดียวกันเสมอ:

```json
{
  "timestamp": "2026-10-10T13:46:02.113",
  "status": 409,
  "error": "Conflict",
  "message": "Game is not in placement phase",
  "path": "/api/games/12/placement"
}
```

|สถานะ HTTP|ต้นเหตุในโค้ด|ความหมาย|ตัวอย่าง `message`|
|-|-|-|-|
|404 Not Found|`ResourceNotFoundException`|ไม่พบข้อมูล (game, player, city, room code)|`Game not found with id: 12`|
|409 Conflict|`BusinessLogicException`|พบข้อมูลแต่กติกาเกมไม่อนุญาตให้ทำ เช่น ผิดเฟส, ไม่ใช่ตาของผู้เล่นนี้|`You do not own this city`|
|400 Bad Request|`MethodArgumentNotValidException` (Jakarta Validation ที่ `@Valid @RequestBody`)|body ไม่ผ่าน validation — `message` รวมทุก field เป็น `field: ข้อความ, field: ข้อความ`|`name: Player name is required`|
|500 Internal Server Error|`Exception` (handler ตัวสุดท้าย)|ข้อผิดพลาดที่ไม่ได้ระบุไว้ รวมถึง query parameter บังคับที่ขาดไป|`Required request parameter 'viewerPlayerId' ...`|

### 2.2 เฟสของเกมกับ endpoint ที่ใช้ได้

`GameStateContext.java:13-20` ผูก `GameStatus` เข้ากับ state object ที่ควบคุมว่าทำอะไรได้บ้าง — state object นี้ถูกเรียกจาก service (`TurnActionService`, `TurnService`) ไม่ใช่จาก controller โดยตรง ค่าที่นอกเหนือจาก `WAITING`, `IN\_PROGRESS`, `FINISHED` (รวมทั้ง `MARSHAL\_SELECTION` และ `PLACEMENT`) ตกเข้า `default` คือ `WaitingPhaseState` ทั้งหมด

|`GameStatus`|เข้าสู่สถานะด้วย|ส่ง action / resolve-turn|endpoint ประจำเฟส|
|-|-|-|-|
|`WAITING`|`POST /api/games`|**409** (`WaitingPhaseState`)|`POST /api/games/{gameId}/players`, `POST .../start`|
|`MARSHAL\_SELECTION`|`POST /api/games/{gameId}/start` (`GameService.startGame`)|**409** (`WaitingPhaseState`)|`GET`/`POST /api/players/{playerId}/marshal-candidates\*`|
|`PLACEMENT`|เลือกจอมพลครบทุกคน (`MarshalDraftService.java:94`, `:40`)|**409** (`WaitingPhaseState`)|`POST /api/games/{gameId}/placement`|
|`IN\_PROGRESS`|เลือกเมืองครบและไม่ชนกัน (`PlacementService.selectBase`)|ใช้ได้ (`PlayPhaseState`)|actions, resolve-turn, snapshot, logs|
|`FINISHED`|เงื่อนไขจบเกม (การรบ/อดอยาก)|**409** (`FinishedPhaseState`)|snapshot, battles, events|

ข้อความ 409 ที่เกิดจาก state machine (เขียนเป็นภาษาไทยใน `state/WaitingPhaseState.java` และ `state/FinishedPhaseState.java`):

* `ไม่สามารถทำแอคชันได้ เกมกำลังรอผู้เล่นอื่น` — ส่ง action ตอนเกมยังไม่ `IN\_PROGRESS`
* `ไม่สามารถจบเทิร์นได้ เกมกำลังรอผู้เล่นอื่น` — `resolve-turn` ตอนเกมยังไม่ `IN\_PROGRESS`
* `ไม่สามารถทำแอคชันได้ เกมจบลงแล้ว` / `ไม่สามารถจบเทิร์นได้ เกมจบลงแล้ว` — ทำหลังเกม `FINISHED`

### 2.3 ประเด็นเรื่องความลับของข้อมูล (Hidden Information)

`GET /api/games/{gameId}/snapshot` เป็น endpoint เดียวที่กรองข้อมูลตามกฎ Hidden Information — ผู้เรียกต้องระบุ `viewerPlayerId` และจะได้เฉพาะข้อมูลของตนกับสิ่งที่เปิดเผยแล้ว (ดูข้อ 4.1) ส่วน `GET /api/games/{gameId}/players`, `/actions`, `/battles`, `/events` **ไม่กรอง** คืนข้อมูลทั้งหมดของเกมที่ขอ

\---

## 3\. ตารางสรุป endpoint (18 ตัว)

|#|Method|Path|Controller method|เมื่อสำเร็จ|ใช้ในเฟส|
|-|-|-|-|-|-|
|1|POST|`/api/games`|`GameController.createGame`|201|–|
|2|GET|`/api/games`|`GameController.getAllGames`|200|ทุกเฟส|
|3|GET|`/api/games/{gameId}`|`GameController.getGame`|200|ทุกเฟส|
|4|GET|`/api/games/code/{roomCode}`|`GameController.getGameByCode`|200|ทุกเฟส|
|5|POST|`/api/games/{gameId}/players`|`GameController.addPlayer`|201|`WAITING`|
|6|GET|`/api/games/{gameId}/players`|`GameController.getPlayers`|200|ทุกเฟส|
|7|POST|`/api/games/{gameId}/start`|`GameController.startGame`|200|`WAITING`|
|8|GET|`/api/games/{gameId}/snapshot`|`GameController.getSnapshot`|200|ทุกเฟส|
|9|GET|`/api/players/{playerId}`|`PlayerController.getPlayer`|200|ทุกเฟส|
|10|GET|`/api/players/{playerId}/marshal-candidates`|`MarshalDraftController.getCurrentCandidate`|200|`MARSHAL\_SELECTION`|
|11|POST|`/api/players/{playerId}/marshal-candidates/reroll`|`MarshalDraftController.reroll`|200|`MARSHAL\_SELECTION`|
|12|POST|`/api/players/{playerId}/marshal-candidates/choose`|`MarshalDraftController.chooseCurrent`|200|`MARSHAL\_SELECTION`|
|13|POST|`/api/games/{gameId}/placement`|`PlacementController.selectBase`|200|`PLACEMENT`|
|14|POST|`/api/games/{gameId}/players/{playerId}/actions`|`TurnController.submitAction`|201|`IN\_PROGRESS`|
|15|GET|`/api/games/{gameId}/actions`|`TurnController.getActions`|200|ทุกเฟส|
|16|POST|`/api/games/{gameId}/resolve-turn`|`TurnController.resolveTurn`|200|`IN\_PROGRESS`|
|17|GET|`/api/games/{gameId}/battles`|`GameLogController.getBattles`|200|ทุกเฟส|
|18|GET|`/api/games/{gameId}/events`|`GameLogController.getEvents`|200|ทุกเฟส|

ไม่มี endpoint nào ใช้ `PUT`, `PATCH` หรือ `DELETE` — เกมที่สร้างแล้วลบผ่าน API ไม่ได้ และในโค้ดทั้งหมดยังไม่มี scheduler ตัวใดลบเกม (method ลบเดียวใน repository layer คือ `MarshalCandidateRepository.deleteByPlayer\_Id`) เกมทั้งหมดจึงค้างอยู่ในฐานข้อมูลร่วมจนกว่าจะลบด้วยมือ

\---

## 4\. รายละเอียดราย endpoint

### 4.1 GameController — `/api/games` (`GameController.java:23`)

#### (1) สร้างเกม — `POST /api/games`

ไม่รับ request body และไม่มี parameter ใด ๆ

* คืน `GameDto` ของเกมใหม่: `status = WAITING`, `currentTurnNumber = 0`, `roomCode` สุ่ม 6 ตัวอักษร, `playerCount = 0`
* **Status:** 201 Created

```json
{ "id": 41, "roomCode": "K3T9AB", "status": "WAITING", "currentTurnNumber": 0,
  "playerCount": 0, "alivePlayerCount": 0, "winnerId": null, "winnerName": null,
  "createdAt": "2026-10-10T13:46:02", "updatedAt": "2026-10-10T13:46:02" }
```

> หมายเหตุฝั่ง client: เกมใหม่มี `playerCount = 0` เสมอ เพราะ `GameDto.from` นับจาก field `players` ของ entity ซึ่ง `Game.builder()` ข้ามค่าตั้งต้นไว้ (`GameDto.java:17-18` ระบุเหตุผลไว้ในคอมเมนต์)

#### (2) ดึงรายชื่อเกมแบบแบ่งหน้า — `GET /api/games`

|Query|ชนิด|ค่าเริ่มต้น|ความหมาย|
|-|-|-|-|
|`page`|int|0|หน้าที่ต้องการ (0-based)|
|`size`|int|10|จำนวนต่อหน้า (`@PageableDefault(size = 10)`)|
|`sort`|string|`createdAt`|เรียงตาม `createdAt`|

* คืนวัตถุ `Page<GameDto>` ของ Spring Data — รายการอยู่ใน `content\[]` พร้อม `page`, `size`, `totalElements`, `totalPages`
* **Status:** 200 OK
* ไม่มีการกรองตามเฟส คืนทุกเกมในฐานข้อมูล

#### (3) ดูเกมตาม id — `GET /api/games/{gameId}`

* คืน `GameDto` · **Status:** 200 OK · 404 `Game not found with id: {gameId}`

#### (4) ดูเกมตามรหัสห้อง — `GET /api/games/code/{roomCode}`

* ใช้ตอนเข้าร่วมห้องด้วยรหัสที่เพื่อนส่งมา · คืน `GameDto` · **Status:** 200 OK · 404 `Game not found with room code: {roomCode}`
* path นี้คือ `code/{roomCode}` ไม่ใช่ `?roomCode=` — ต้องใส่ `/code/` นำหน้ารหัส

#### (5) เพิ่มผู้เล่น — `POST /api/games/{gameId}/players`

Request body (`AddPlayerRequest`):

|field|ชนิด|บังคับ|เกณฑ์|
|-|-|-|-|
|`name`|string|ใช่|ไม่เว้นว่าง, ไม่เกิน 50 ตัวอักษร|

* คืน `PlayerDto` ของผู้เล่นที่เพิ่งสร้าง (`alive = true`, `marshal = null`, `rerollCount = 0`) · **Status:** 201 Created

|สถานะ|เงื่อนไข|`message` จากโค้ด|
|-|-|-|
|404|ไม่พบเกม|`Game not found with id: {gameId}`|
|409|เกมไม่ได้อยู่ใน `WAITING`|`Game is no longer accepting players`|
|409|ชื่อว่าง (ตรวจซ้ำใน service)|`Player name is required`|
|409|เกมมีผู้เล่นครบ 7 คนแล้ว|`A game can have at most 7 players`|
|400|`name` ไม่ผ่าน `@Valid` ก่อนเข้า service|`name: Player name is required` · `name: Player name must not exceed 50 characters`|

#### (6) ดูผู้เล่นในเกม — `GET /api/games/{gameId}/players`

|Query|ชนิด|ค่าเริ่มต้น|
|-|-|-|
|`aliveOnly`|boolean|`false`|

* คืน `PlayerDto\[]` · **Status:** 200 OK
* **พฤติกรรมที่ควรระวัง:** path นี้เรียก `PlayerService.findByGame` / `findAliveByGame` ซึ่งไม่ตรวจสอบ existence ของเกม (`PlayerService.java:28-34`) ถ้า `gameId` ไม่มีอยู่จริงจะได้ **200 พร้อม `\[]` ไม่ใช่ 404** — message `Game not found with id:` ใช้กับ endpoint (3), (5), (7) ที่ผ่าน `GameService` เท่านั้น
* endpoint นี้ไม่กรองข้อมูลตามผู้ที่เรียก (ต่างจาก snapshot ในข้อ (8))

#### (7) เริ่มเกม — `POST /api/games/{gameId}/start`

ไม่รับ body เมื่อผ่านจะสร้างแผนที่ (`nodeCount = playerCount × 3`), ตั้ง `status = MARSHAL\_SELECTION`, `currentTurnNumber = 0` แล้วสุ่มชุดจอมพล candidate ชุดแรกให้ผู้เล่นคนแรก

* คืน `GameDto` สถานะใหม่ · **Status:** 200 OK (ไม่ใช่ 201)

|สถานะ|เงื่อนไข|`message`|
|-|-|-|
|404|ไม่พบเกม|`Game not found with id: {gameId}`|
|409|เริ่มไปแล้ว|`Game has already started`|
|409|ผู้เล่นน้อยกว่า 2 หรือมากกว่า 7|`A game requires between 2 and 7 players`|
|409|ตาราง marshal ในฐานข้อมูลมีไม่พอต่อจำนวนผู้เล่น|`Not enough marshal records are configured`|

#### (8) Snapshot ของผู้เล่นคนหนึ่ง — `GET /api/games/{gameId}/snapshot`

|Query|ชนิด|บังคับ|
|-|-|-|
|`viewerPlayerId`|Long|**ใช่** — ไม่ตั้ง `defaultValue`|

* คืน `GameViewService.GameSnapshot` (โครงสร้างอยู่ในข้อ 5) · **Status:** 200 OK
* เป็น endpoint เดียวที่ซ่อนข้อมูลของผู้เล่นอื่น ใช้ตอนวาดกระดานและตอนเติมตา
* กฎการซ่อนข้อมูลจาก `GameViewService.java:51-106`: `food` คืนเฉพาะเมืองของตน (อื่นเป็น `null`); `soldiers` ของเมืองคนอื่นเห็นเฉพาะตอนเป็นกลางวัน; `visibleActions` คือแอคชันของ **เทิร์นก่อนหน้า** (`prevTurn = turn - 1`) ไม่ใช่เทิร์นปัจจุบัน; `visibleTargetCityId` และ `arrivalTurn` ของกองทัพเปิดเผยต่อเจ้าของ, ต่อเมืองเป้าหมายที่อยู่ใกล้ (ภายใน 2 เทิร์น) และต่อทุกฝ่ายถ้าจอมพลของเจ้าของมี `revealsAttackTarget = true`; `armies` มีเฉพาะสถานะ `TRAVELING`
* **Status ผิดพลาด:** 404 `Game not found` และ 404 `Viewer not found` — สังเกตว่า message สองตัวนี้ *ไม่* มี `with id:` ต่อท้าย (ต่างจาก endpoint อื่น) เพราะ `GameViewService.java:40,42` ตั้ง string ไว้แบบนั้น ส่วนการลืมส่ง `viewerPlayerId` จะไม่เข้า handler ใด ๆ โดยตรง แต่ตกไปที่ handler `Exception` → **500**

### 4.2 PlayerController — `/api/players` (`PlayerController.java:15`)

#### (9) ดูผู้เล่นตาม id — `GET /api/players/{playerId}`

* คืน `PlayerDto` รวม `marshal` ที่เลือกแล้ว · **Status:** 200 OK · 404 `Player not found with id: {playerId}`

### 4.3 MarshalDraftController — `/api/players/{playerId}` (`MarshalDraftController.java:15`)

ทั้งสาม path อยู่ใต้ id ผู้เล่น ทำงานเฉพาะตอนเกม `MARSHAL\_SELECTION` และเฉพาะกับ **ผู้เล่นคนที่ถึงตาเลือก** (คนแรกที่ยังมี `marshal == null` ตาม `MarshalDraftService.java:106-110`) ไม่ใช่ผู้เล่นทุกคน draft พร้อมกัน

#### (10) ดู candidate ที่ผู้เล่นกำลังเห็น — `GET /api/players/{playerId}/marshal-candidates`

* คืน `MarshalCandidateDto\[]` ที่มี **1 องค์ประกอบเสมอ** (slot ปัจจุบัน = `rerollCount + 1` จำกัดด้วยจำนวน slot ที่มี — `MarshalCandidateService.java:26-28`) หรือ `\[]` ถ้ายังไม่มีการ draft — endpoint นี้ไม่ได้คืน candidate ทั้ง 3 ตัวพร้อมกัน
* ผลข้างเคียง: path นี้ผ่าน `MarshalCandidateService.findForPlayer` ซึ่งเรียกแค่ `playerRepository.findById` จึง 404 `Player not found with id: {playerId}` ได้เท่านั้น — ไม่มีการตรวจสอบเฟสของเกมนี้ จึงเรียกได้แม้เกมจะเลย `MARSHAL\_SELECTION` ไปแล้ว
* **Status:** 200 OK

#### (11) Re-roll — `POST /api/players/{playerId}/marshal-candidates/reroll`

ไม่รับ body · เลื่อนไป slot ถัดไป · คืน `MarshalCandidateDto` ของ slot ใหม่ · **Status:** 200 OK

|สถานะ|เงื่อนไขในโค้ด|`message`|
|-|-|-|
|404|ไม่พบผู้เล่น|`Player not found with id: {playerId}`|
|409|เกมไม่ได้อยู่ใน `MARSHAL\_SELECTION` (`MarshalDraftService.java:103`)|`Game is not in marshal selection`|
|409|ไม่ใช่ตาของผู้เล่นนี้ (`:108`)|`It is not this player's draft turn`|
|409|`nextSlot = rerollCount + 2` เกินจำนวน slot ที่มี (`:69-70`)|`No rerolls remain`|

> ปกติแต่ละผู้เล่นได้ 3 slot (`offerCount = Math.min(3, pool.size())` ที่ `:53`) reroll จึงใช้ได้ 2 ครั้งตามคอมเมนต์ใน `MarshalDraftController.java:19` แต่โค้ดไม่ได้ประกาศค่า 2 ไว้เป็น constant — จำนวนครั้งจริงผูกกับขนาด pool

#### (12) เลือกจอมพลปัจจุบัน — `POST /api/players/{playerId}/marshal-candidates/choose`

ไม่รับ body · คืน `MarshalDto` ที่เลือก · **Status:** 200 OK

* ผลข้างเคียง: ถ้ายังเหลือผู้เล่นคนอื่นที่ยังไม่เลือก → สุ่ม candidate ชุดถัดไปให้คนต่อไป (`prepareNextPlayer`); ถ้าเลือกครบทุกคน → เกมเปลี่ยนเป็น `PLACEMENT` (client ต้องอ่าน `status` ของเกมหรือ polling เพื่อรู้ว่าถึงตาใครแล้ว)
* ผิดพลาด: 404/409 เหมือนข้อ (11) และ 409 `No marshal candidates are available` เมื่อผู้เล่นนี้ยังไม่เคยถูกสุ่ม candidate (`MarshalDraftService.java:83`)

### 4.4 PlacementController — `/api/games/{gameId}/placement` (`PlacementController.java:11`)

#### (13) เลือกเมืองเริ่มต้น — `POST /api/games/{gameId}/placement`

**ส่งค่าเป็น query string ไม่ใช่ JSON body:**

|Query|ชนิด|บังคับ|
|-|-|-|
|`playerId`|Long|ใช่|
|`cityId`|Long|ใช่|

ตัวอย่าง: `POST /api/games/12/placement?playerId=7\&cityId=33`

* คืน **body ว่าง** (`ResponseEntity<Void>`) · **Status:** 200 OK
* ผลข้างเคียงเมื่อทุกคนเลือกครบและไม่มีใครเลือกเมืองซ้ำ: เมืองถูกมอบให้เจ้าของ, `soldiers = 0`, `food = 50`, ชื่อเมืองถูกเติมชื่อผู้เล่น, เกมเป็น `IN\_PROGRESS` และ `currentTurnNumber = 1`
* **ถ้ามีผู้เล่นเลือกเมืองเดียวกันซ้ำ → การเลือกของทุกคนถูกรีเซ็ต** และยังคงคืน 200 พร้อม body ว่าง ไม่มี error กลับมา client จึงต้องตรวจจากการที่เกมยังไม่เปลี่ยนสถานะ (และไม่มี endpoint ให้ดูว่าใครเลือกเมืองอะไรไว้ก่อนจบเฟสนี้)

|สถานะ|`message`|
|-|-|
|404|`Game not found with id: {gameId}` · `Player not found with id: {playerId}` · `City not found: {cityId}`|
|409|`Game is not in placement phase` · `Player does not belong to this game` · `City does not belong to this game`|

### 4.5 TurnController — `/api/games/{gameId}` (`TurnController.java:19`)

#### (14) ส่งแอคชันของเทิร์น — `POST /api/games/{gameId}/players/{playerId}/actions`

Request body (`TurnActionRequest`):

|field|ชนิด|บังคับ|หมายเหตุ|
|-|-|-|-|
|`actionType`|`ActionType`|ใช่ (`@NotNull`)|`PRODUCE\_FOOD` / `RECRUIT\_SOLDIERS` / `SEND\_ARMY` / `NONE`|
|`cityId`|Long|ใช่ (`@NotNull`)|เมืองที่ออกแอคชัน ต้องเป็นเมืองของผู้เล่นนี้|
|`targetCityId`|Long|ไม่บังคับ|จำเป็นเมื่อ `SEND\_ARMY`|
|`soldierCount`|Integer|ไม่บังคับ|จำเป็นเมื่อ `SEND\_ARMY`, ต้อง ≥ 1 (`@Min(1)`)|

```json
{ "actionType": "SEND\_ARMY", "cityId": 33, "targetCityId": 41, "soldierCount": 12 }
```

* คืน `TurnActionDto` พร้อม `foodBefore/foodAfter`, `soldiersBefore/soldiersAfter` ให้ client แสดงผลได้ทันที · **Status:** 201 Created
* ประเด็นที่ควรรู้ (`TurnActionService.java:47-51`): ส่งซ้ำที่ **เมืองเดิม เทิร์นเดิม** จะไม่ error แต่คืน 201 พร้อมแอคชันที่บันทึกไว้แล้ว การกัน "หนึ่งแอคชันต่อเทิร์น" จึงคุมระดับเมือง ไม่ใช่ระดับผู้เล่น — คอมเมนต์ที่ `TurnController.java:24` เขียนว่าคุมระดับผู้เล่น ซึ่งไม่ตรงโค้ด ในทางปฏิบัติเกมออกแบบให้ผู้เล่นหนึ่งคนมีหนึ่งเมืองต่อเทิร์น ผลจึงเหมือนกัน แต่ client ไม่ควรพึ่งประโยคนั้น
* พฤติกรรมอื่น: `SEND\_ARMY` มีโอกาส 20% ที่จอมพลบางสาย ability จะกลายเป็น `REBELLION` และถูกบันทึกเป็น `actionType = NONE` (รายละเอียดกติกาอยู่ใน `doc/game\_rules\_specification.md`)

|สถานะ|`message`|
|-|-|
|404|`Game not found with id: {gameId}` · `Player not found with id: {playerId}` · `City not found: {cityId}`|
|409|`Eliminated players cannot take actions` · `Eliminated players cannot command armies` · `Player does not belong to this game` · `You do not own this city` · `Action type is required` · `Target and soldier count are required to send an army` · `Army must contain at least one soldier` · `Not enough soldiers in the city` · `You can only send an army to a directly connected city` · `Target must be in the same game` · `Game is not in progress` · `Not enough food in the connected network. Need {cost} but have {available}` · ข้อความ state machine (ข้อ 2.2)|
|400|`actionType: actionType is required, cityId: cityId is required, soldierCount: soldierCount must be at least 1`|

> `RECRUIT\_SOLDIERS` คิดค่าใช้จ่าย 20 หน่วย × `recruitFoodMultiplier` ของจอมพล (`CityService.java:28,61-64`) และหักจาก food ของ \*\*เครือข่ายเมืองที่ติดกัน\*\* ของผู้เล่นนั้น ไม่ใช่เฉพาะเมืองที่ออกแอคชัน ถ้ารวมกันแล้วไม่พอจะได้ 409 `Not enough food in the connected network. Need {cost} but have {available}` ส่วน `PRODUCE\_FOOD` ไม่มีการตรวจสอบใด ๆ ที่ต้องส่ง error (ได้ 20 หน่วยตามค่า default เมื่อเมืองนั้นยังไม่มีจอมพล, และครึ่งหนึ่งในฤดูหนาว)

#### (15) ดูแอคชันของเทิร์น — `GET /api/games/{gameId}/actions`

|Query|ชนิด|ค่าเริ่มต้น|
|-|-|-|
|`turnNumber`|Integer|ไม่ระบุ → ใช้ `currentTurnNumber` ของเกม (`TurnController.java:35`)|

* คืน `TurnActionDto\[]` · **Status:** 200 OK
* ถ้าไม่ส่ง `turnNumber`: path นี้เรียก `TurnService.getCurrentGame` ซึ่งตรวจ existence → 404 `Game not found with id: {gameId}`
* ถ้าส่ง `turnNumber` มาเอง: ไม่ได้แตะ `gameRepository` เลย เกมที่ไม่มีอยู่จริงจึงคืน 200 พร้อม `\[]`
* endpoint นี้ไม่กรองตามผู้เรียก จึงเห็นแอคชันของผู้เล่นคนอื่นทั้งเทิร์น (รวมถึง `SEND\_ARMY` ที่ยังไม่วางเป้าหมายแบบเปิดเผย) — ใช้เทียบกับการซ่อนข้อมูลของ snapshot ไม่ได้

#### (16) จบเทิร์น (คำนวณเหตุการณ์ + การรบ) — `POST /api/games/{gameId}/resolve-turn`

ไม่รับ body · คืน `GameDto` หลังประมวลผล · **Status:** 200 OK

* เมื่อทุกเมืองของผู้เล่นที่มีชีวิตใช้แอคชันครบ: ประมวลผล `GameEvent`, ยุติการรบ, ถ้าเป็นเทิร์นสิ้นฤดูจะเก็บ upkeep, รีเซ็ตตัวนับแอคชัน แล้ว `currentTurnNumber + 1`
* **ถ้ายังไม่ครบทุกคน: ตอบ 200 พร้อมเกมเดิม ไม่เกิด error** (`TurnService.java:44-46` return game) client จึงต้องเทียบ `currentTurnNumber` กับค่าก่อนเรียก ไม่ใช่ดูแค่ status code
* ถ้าการรบทำให้เกมจบ จะได้ `status = FINISHED` พร้อม `winnerId`/`winnerName` ทันที (ไม่เพิ่มเทิร์น)
* 404 `Game not found with id: {gameId}` · 409 `Game has no active players` หรือข้อความ state machine (ข้อ 2.2)

### 4.6 GameLogController — `/api/games/{gameId}` (`GameLogController.java:16`)

#### (17) ดูบันทึกการรบ — `GET /api/games/{gameId}/battles`

|Query|ชนิด|ค่าเริ่มต้น|
|-|-|-|
|`turnNumber`|Integer|ไม่กรอง (คืนทุกเทิร์นของเกม)|

* คืน `BattleDto\[]` · **Status:** 200 OK (เกมที่ไม่มีการรบคืน `\[]`)
* เช่นเดียวกับ log อีกตัว: path นี้ไม่ตรวจ existence ของเกม เกม id ที่ไม่มีอยู่จึงคืน 200 `\[]` ไม่ใช่ 404

#### (18) ดูบันทึกเหตุการณ์ — `GET /api/games/{gameId}/events`

เหมือน (17) แต่คืน `GameEventDto\[]` — เหตุการณ์อากาศ/เภทภัย/กบฏ/อดอยาก ตามค่า `EventType` ในข้อ 6

> ทั้งสอง endpoint โหลดทุกแถวจาก repository แล้วกรองด้วย stream ใน memory (`GameLogController.java:24-27, 33-36`) ไม่ได้กรองที่ SQL จึงยังไม่เหมาะกับเกมที่มี log ยาวมาก
>
> เช่นเดียวกันกับข้อ (6): ถ้า `gameId` ไม่มีอยู่จริงจะได้ 200 `\[]` เพราะ path นี้ไม่ตรวจสอบว่าเกมมีอยู่จริง

\---

## 5\. โครงสร้างข้อมูลที่ส่งกลับ (DTO)

ทั้งหมดเป็น Java record — ชื่อ field ตามที่ใช้จริงใน JSON

|DTO|field|
|-|-|
|`GameDto`|`id`, `roomCode`, `status`, `currentTurnNumber`, `playerCount`, `alivePlayerCount`, `winnerId`, `winnerName`, `createdAt`, `updatedAt`|
|`PlayerDto`|`id`, `gameId`, `name`, `alive`, `eliminatedAtTurn`, `rerollCount`, `marshal` (object `MarshalDto` หรือ `null`)|
|`MarshalDto`|`id`, `name`, `abilityName`, `abilityDescription`, `disadvantageDescription`, `foodProduction`, `soldierProduction`, `attackKillRatio`, `revealsAttackTarget`, `specialAbilityType`|
|`MarshalCandidateDto`|`id`, `playerId`, `slotNumber`, `isSelected`, `marshal`|
|`TurnActionDto`|`id`, `gameId`, `turnNumber`, `playerId`, `playerName`, `actionType`, `armyId`, `foodBefore`, `foodAfter`, `soldiersBefore`, `soldiersAfter`|
|`BattleDto`|`id`, `gameId`, `turnNumber`, `battleType`, `attackerPlayerId`, `attackerArmyId`, `defenderPlayerId`, `defenderArmyId`, `attackerSoldiers`, `defenderSoldiers`, `attackerCasualties`, `defenderCasualties`, `cityDestroyed`, `result`|
|`GameEventDto`|`id`, `gameId`, `turnNumber`, `eventType`, `affectedPlayerId`, `affectedArmyId`, `locationType`, `foodImpact`, `soldierImpact`, `extraTravelTurns`, `description`|
|`AddPlayerRequest` (body)|`name`|
|`TurnActionRequest` (body)|`actionType`, `cityId`, `targetCityId`, `soldierCount`|

`GameSnapshot` และ record ย่อย 5 ตัว (`GameViewService.java:112-119`) — เป็น object ซ้อนใน body ของ endpoint (8)

|record|field|
|-|-|
|`GameSnapshot`|`gameId`, `status`, `currentTurn`, `season`, `daytime`, `players\[]`, `nodes\[]`, `edges\[]`, `visibleActions\[]`, `visibleArmies\[]`|
|`PlayerSnapshot`|`playerId`, `name`, `alive`, `marshalName`, `isViewer`|
|`NodeSnapshot`|`nodeId`, `name`, `x`, `y`, `ownerId`, `food`, `soldiers`, `actionUsedThisTurn`|
|`EdgeSnapshot`|`edgeId`, `node1Id`, `node2Id`|
|`ActionSnapshot`|`playerId`, `sourceCityId`, `actionType`, `visibleTargetCityId`|
|`ArmySnapshot`|`armyId`, `ownerPlayerId`, `sourceCityId`, `visibleTargetCityId`, `soldiers`, `arrivalTurn`, `status`|

\---

## 6\. ค่า enum ที่ส่งใน JSON

enum ทั้งหมดอยู่ใน `code/src/main/java/com/eternalclash2/domain/enums` และถูก serialize เป็นชื่อค่า (string)

|enum|ค่า|
|-|-|
|`GameStatus`|`WAITING`, `MARSHAL\_SELECTION`, `PLACEMENT`, `IN\_PROGRESS`, `FINISHED`|
|`ActionType`|`PRODUCE\_FOOD`, `RECRUIT\_SOLDIERS`, `SEND\_ARMY`, `NONE`|
|`Season`|`SUMMER`, `RAINY`, `WINTER`|
|`EventType`|`SINKHOLE`, `SUN\_GLARE`, `LIGHTNING`, `AVALANCHE`, `FOOD\_SPOILAGE`, `INSECT\_DAMAGE`, `FROSTBITE`, `EPIDEMIC`, `SLOW`, `FLOOD`, `SUNBURN`, `SNOW\_COVER`, `REBELLION`, `STARVATION`|
|`LocationType`|`IN\_CITY`, `OUTSIDE\_CITY`|
|`BattleType`|`CITY\_SIEGE`, `FIELD\_ENCOUNTER`|
|`BattleResult`|`ATTACKER\_WIN`, `DEFENDER\_WIN`|
|`ArmyStatus`|`TRAVELING`, `ARRIVED`, `DESTROYED`, `CANCELLED`|

\---

## 7\. ลำดับการเรียกใช้งานตามวงจรเกม (สำหรับ client)

```
1  POST /api/games                                   → ได้ gameId + roomCode
2  POST /api/games/{gameId}/players  × 2..7          → ทุกคนเข้าห้อง
3  POST /api/games/{gameId}/start                     → status = MARSHAL\_SELECTION
4  วนจนครบทุกคน (ทีละคน ตามลำดับ id):
     GET  /api/players/{playerId}/marshal-candidates   → slot ปัจจุบัน 1 ตัว
     POST /api/players/{playerId}/marshal-candidates/reroll    → ทำซ้ำจนกว่าจะได้ 'No rerolls remain'
     POST /api/players/{playerId}/marshal-candidates/choose     → คนสุดท้ายทำให้ status = PLACEMENT
5  ทุกคน: POST /api/games/{gameId}/placement?playerId=..\&cityId=..
     → คนสุดท้าย (ถ้าไม่มีใครเลือกเมืองซ้ำ) ทำให้ status = IN\_PROGRESS, currentTurnNumber = 1
6  วนซ้ำจนกว่าจะมีผู้ชนะ:
     GET  /api/games/{gameId}/snapshot?viewerPlayerId=...      → ใช้ตัวนี้วาดกระดาน
     POST /api/games/{gameId}/players/{playerId}/actions        (เมืองละ 1 แอคชัน)
     GET  /api/games/{gameId}/actions
     POST /api/games/{gameId}/resolve-turn                      → เทียบ currentTurnNumber ยืนยันว่าเทิร์นเดินจริง
     GET  /api/games/{gameId}/events  ·  GET /api/games/{gameId}/battles
7  เมื่อ status = FINISHED → อ่าน winnerId / winnerName จาก GameDto
```

\---

## 8\. หลักฐานและขอบเขตของเอกสาร

* ทุก endpoint, path, parameter และ status code อ้างจาก `GameController.java`, `PlayerController.java`, `MarshalDraftController.java`, `PlacementController.java`, `TurnController.java`, `GameLogController.java`
* ข้อความ error ทั้งหมดคัดจาก `service/\*.java`, `command/\*.java`, `state/\*.java` และ `exception/GlobalExceptionHandler.java`
* ตารางเฟสอ้างอิง `state/GameStateContext.java:13-20` — `MARSHAL\_SELECTION` และ `PLACEMENT` ตกเข้า `default` คือ `WaitingPhaseState`
* ความถูกต้องของ contract ส่วนนี้ยืนยันโดย test suite แล้ว (28 test case ใน `Test/Test Suite/TestPlanPrincipleController.xlsx` — 26 pass, 2 fail ซึ่งทั้งสองถูกบันทึกเป็น defect ใน `TestSuiteSummary.xlsx → DefectSummary`)
* **จุดที่เอกสารนี้อิงการอ่านโค้ดล้วน ยังไม่มี test case ยืนยัน:**

  1. ข้อ (6), (17), (18) และการส่ง `turnNumber` ในข้อ (15) — คืน 200 `\[]` เมื่อ `gameId` ไม่มีอยู่จริง
  2. คำขอที่ขาด required query parameter (`viewerPlayerId`, `playerId`, `cityId` ของ placement) — คาดว่าได้ 500 จาก handler `Exception` ตัวสุดท้าย
  3. การกรอง `GET /api/games` แบบ page แรก/หน้าถัดไป
  4. endpoint (8) ทั้งหมด — ไม่มี test class ของ `GameViewService` (รายการช่องว่างการทดสอบเต็มรูปแบบอยู่ใน `TestSuiteSummary.xlsx → CoverageGap`)
* เอกสารนี้ไม่ครอบคลุมกฎเกมเชิงลึก (อาหาร/ทหาร/การรบ/ฤดู) ดู `doc/game\_rules\_specification.md` และไม่ครอบคลุมโครงสร้างตารางฐานข้อมูล ดู `doc/data\_dictionary.md`

