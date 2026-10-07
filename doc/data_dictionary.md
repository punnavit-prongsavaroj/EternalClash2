# Data Dictionary

## 1. Table: games
- id (PK) - รหัสเกม (Long)
- 
oom_code - รหัสห้อง 6 หลักสำหรับเข้าเล่น (String)
- status - สถานะเกม (Enum: WAITING, MARSHAL_SELECTION, IN_PROGRESS, FINISHED)
- current_turn_number - เทิร์นปัจจุบันของเกม (Integer)
- winner_player_id (FK) - อ้างอิงรหัสผู้ชนะ (One-to-One)

## 2. Table: players
- id (PK) - รหัสผู้เล่น
- game_id (FK) - รหัสเกมที่เข้าร่วม (Many-to-One)
- 
ame - ชื่อผู้เล่นในห้อง
- is_alive - สถานะยังมีชีวิตอยู่
- marshal_id (FK) - ขุนพลที่ผู้เล่นเลือกตอนเปิดเกม

## 3. Table: cities
- id (PK) - รหัสเมือง
- player_id (FK) - รหัสเจ้าของเมือง (One-to-One)
- 
ame - ชื่อเมือง
- ood - จำนวนเสบียง
- soldiers - จำนวนทหารป้องกันเมือง

## 4. Table: armies
- id (PK) - รหัสกองทัพ
- owner_id (FK) - เจ้าของกองทัพ (Many-to-One)
- 	arget_id (FK) - เป้าหมายผู้เล่นที่กำลังเดินทัพไปโจมตี
- soldiers - จำนวนทหารในทัพ
- rrival_turn - เทิร์นที่จะถึงเป้าหมาย (เดินทางเสร็จสิ้น)

## 5. Table: marshals
- id (PK) - รหัสขุนพล
- 
ame - ชื่อขุนพล (เช่น ขงเบ้ง, ลิโป้)
- bility_description - คำอธิบายสกิลและจุดเด่น

## 6. Table: game_events
- id (PK) - รหัสเหตุการณ์สุ่ม
- game_id (FK) - รหัสเกมที่เกิดเหตุการณ์
- event_type - ประเภทอีเวนต์ (Enum เช่น SUN_GLARE, FLOOD)
- description - รายละเอียดที่ถูกสร้าง
