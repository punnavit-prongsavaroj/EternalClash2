# Data Dictionary

## 1. Table: games
| Column Name | Data Type | Constraint | Description |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY | รหัสเกม (Auto Increment) |
| room_code | VARCHAR(10) | UNIQUE | รหัสห้อง 6 หลักสำหรับชวนเพื่อนเข้าเล่น |
| status | VARCHAR(50) | NOT NULL | สถานะเกม (WAITING, MARSHAL_SELECTION, PLACEMENT, IN_PROGRESS, FINISHED) |
| current_turn_number | INTEGER | NOT NULL | เทิร์นปัจจุบันของเกม (เริ่มต้นที่ 1) |
| winner_player_id | BIGINT | FOREIGN KEY | อ้างอิงผู้ชนะของเกมนี้ (เชื่อมกับตาราง players) |

## 2. Table: players
| Column Name | Data Type | Constraint | Description |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY | รหัสผู้เล่น |
| game_id | BIGINT | FOREIGN KEY | รหัสเกมที่ผู้เล่นเข้าร่วม |
| name | VARCHAR(255) | NOT NULL | ชื่อผู้เล่น |
| session_id | VARCHAR(255) | | รหัส Session ของผู้เล่น |
| is_alive | BOOLEAN | NOT NULL | สถานะยังมีชีวิตอยู่ (ยังไม่ถูกตีเมืองแตก) |
| marshal_id | BIGINT | FOREIGN KEY | ขุนพลที่ผู้เล่นเลือกตอนเปิดเกม |

## 3. Table: player_stats
| Column Name | Data Type | Constraint | Description |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY | รหัสสถิติ |
| player_id | BIGINT | FOREIGN KEY | เจ้าของสถิติ (One-to-One กับ players) |
| total_food_produced | INTEGER | NOT NULL | จำนวนเสบียงทั้งหมดที่ผลิตได้ |
| total_soldiers_recruited | INTEGER | NOT NULL | จำนวนทหารทั้งหมดที่เกณฑ์ได้ |
| battles_won | INTEGER | NOT NULL | จำนวนครั้งที่ชนะการต่อสู้ |
| cities_captured | INTEGER | NOT NULL | จำนวนเมืองที่ยึดมาได้ |
| soldiers_lost | INTEGER | NOT NULL | จำนวนทหารที่สูญเสียไปทั้งหมด |

## 4. Table: cities
| Column Name | Data Type | Constraint | Description |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY | รหัสเมือง |
| game_id | BIGINT | FOREIGN KEY | รหัสเกมของเมืองนี้ |
| player_id | BIGINT | FOREIGN KEY | ผู้เล่นที่เป็นเจ้าของเมือง (ถ้ามี) |
| name | VARCHAR(255) | NOT NULL | ชื่อเมือง |
| food | INTEGER | NOT NULL | จำนวนเสบียงในเมือง |
| soldiers | INTEGER | NOT NULL | จำนวนทหารในเมือง |
| action_used_this_turn | BOOLEAN | NOT NULL | ตรวจสอบว่าในเทิร์นนี้มีการใช้คำสั่งเมืองไปแล้วหรือยัง |

## 5. Table: map_edges
| Column Name | Data Type | Constraint | Description |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY | รหัสเส้นทางเชื่อมต่อ |
| game_id | BIGINT | FOREIGN KEY | รหัสเกม |
| city1_id | BIGINT | FOREIGN KEY | เมืองต้นทาง |
| city2_id | BIGINT | FOREIGN KEY | เมืองปลายทาง |
| travel_distance | INTEGER | NOT NULL | ระยะเวลาเดินทัพ (จำนวนเทิร์น) |

## 6. Table: armies
| Column Name | Data Type | Constraint | Description |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY | รหัสกองทัพ |
| owner_id | BIGINT | FOREIGN KEY | ผู้เล่นที่เป็นเจ้าของกองทัพ |
| target_city_id | BIGINT | FOREIGN KEY | เมืองเป้าหมายที่กำลังเดินทางไปโจมตี/สนับสนุน |
| soldiers | INTEGER | NOT NULL | จำนวนทหารในกองทัพ |
| departure_turn | INTEGER | NOT NULL | เทิร์นที่เริ่มออกเดินทาง |
| arrival_turn | INTEGER | NOT NULL | เทิร์นที่จะถึงเมืองเป้าหมาย |
| status | VARCHAR(50) | NOT NULL | สถานะกองทัพ (TRAVELING, ARRIVED, DESTROYED, CANCELLED) |

## 7. Table: marshals
| Column Name | Data Type | Constraint | Description |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY | รหัสขุนพล |
| name | VARCHAR(255) | NOT NULL | ชื่อขุนพล (เช่น ขงเบ้ง, ลิโป้) |
| food_production | INTEGER | NOT NULL | ค่าโบนัสการผลิตเสบียง |
| soldier_production| INTEGER | NOT NULL | ค่าโบนัสการเกณฑ์ทหาร |
| special_ability_type| VARCHAR(255)| NOT NULL | รหัสสกิลพิเศษประจำตัว (เช่น KONGMING_STRATEGY) |
| image_url | VARCHAR(255) | | รูปภาพประกอบขุนพล |
| combat_video_url | VARCHAR(255) | | วิดีโอสั้นเมื่อขุนพลชนะการต่อสู้ |

## 8. Table: game_events
| Column Name | Data Type | Constraint | Description |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY | รหัสเหตุการณ์สุ่ม |
| game_id | BIGINT | FOREIGN KEY | รหัสเกมที่เกิดเหตุการณ์ |
| turn_number | INTEGER | NOT NULL | เทิร์นที่เกิดเหตุการณ์นี้ขึ้น |
| event_type | VARCHAR(50) | NOT NULL | ชนิดของเหตุการณ์ (เช่น FLOOD, STARVATION) |
| affected_player_id| BIGINT | FOREIGN KEY | ผู้เล่นที่ได้รับผลกระทบ |
| affected_army_id | BIGINT | FOREIGN KEY | กองทัพที่ได้รับผลกระทบ |
| location_type | VARCHAR(50) | NOT NULL | ประเภทสถานที่ (IN_CITY, OUTSIDE_CITY) |
| food_impact | INTEGER | NOT NULL | ค่าเสบียงที่เปลี่ยนแปลง |
| soldier_impact | INTEGER | NOT NULL | ค่าทหารที่เปลี่ยนแปลง |
| extra_travel_turns| INTEGER | NOT NULL | จำนวนเทิร์นการเดินทางที่เพิ่มขึ้น (ถ้ามี) |
| description | VARCHAR(255) | NOT NULL | ข้อความอธิบายเหตุการณ์ |

## 9. Table: turn_actions
| Column Name | Data Type | Constraint | Description |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY | รหัสแอ็กชัน |
| game_id | BIGINT | FOREIGN KEY | รหัสเกม |
| turn_number | INTEGER | NOT NULL | เทิร์นที่สั่งแอ็กชัน |
| player_id | BIGINT | FOREIGN KEY | ผู้เล่นที่ออกคำสั่ง |
| action_type | VARCHAR(50) | NOT NULL | ประเภทคำสั่ง (PRODUCE_FOOD, RECRUIT_SOLDIERS, SEND_ARMY, NONE) |
| target_city_id | BIGINT | FOREIGN KEY | เมืองเป้าหมายที่อ้างอิง |
| soldiers_count | INTEGER | | จำนวนทหาร (สำหรับส่งทัพ) |
