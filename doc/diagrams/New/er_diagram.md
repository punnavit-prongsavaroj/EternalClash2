# Entity-Relationship (ER) Diagram

> **คืออะไร (What is this?)**
> แผนภาพแสดงโครงสร้างและการเชื่อมโยงข้อมูลในฐานข้อมูล (Database Schema)
> 
> **ใช้เทคนิคอะไร (Techniques Used)**
> ออกแบบฐานข้อมูลเชิงสัมพันธ์ (Relational Database) โดยใช้ความสัมพันธ์แบบ One-to-One, One-to-Many ตามหลักการ Normalization
> 
> **ใช้ทำไมและเพื่ออะไร (Why & Purpose)**
> เพื่อใช้เป็นแกนหลักในการสร้าง Entity Class สำหรับ Spring Data JPA (Hibernate) ทำให้รู้ว่าแต่ละตารางต้องมี Foreign Key โยงหากันอย่างไร ช่วยให้เก็บข้อมูลสถานะของเกมและผู้เล่นได้อย่างถูกต้องและลดความซ้ำซ้อนของข้อมูล

```mermaid
erDiagram
    GAMES {
        BIGINT id PK
        VARCHAR room_code
        VARCHAR status
        INTEGER current_turn_number
        BIGINT winner_player_id FK
    }
    
    PLAYERS {
        BIGINT id PK
        BIGINT game_id FK
        VARCHAR name
        VARCHAR session_id
        BOOLEAN is_alive
        BIGINT marshal_id FK
    }
    
    PLAYER_STATS {
        BIGINT id PK
        BIGINT player_id FK
        INTEGER total_food_produced
        INTEGER total_soldiers_recruited
        INTEGER battles_won
        INTEGER cities_captured
        INTEGER soldiers_lost
    }
    
    CITIES {
        BIGINT id PK
        BIGINT game_id FK
        BIGINT player_id FK
        VARCHAR name
        INTEGER food
        INTEGER soldiers
        BOOLEAN action_used_this_turn
    }
    
    MAP_EDGES {
        BIGINT id PK
        BIGINT game_id FK
        BIGINT city1_id FK
        BIGINT city2_id FK
        INTEGER travel_distance
    }
    
    ARMIES {
        BIGINT id PK
        BIGINT owner_id FK
        BIGINT target_city_id FK
        INTEGER soldiers
        INTEGER departure_turn
        INTEGER arrival_turn
        VARCHAR status
    }
    
    MARSHALS {
        BIGINT id PK
        VARCHAR name
        INTEGER food_production
        INTEGER soldier_production
        VARCHAR special_ability_type
        VARCHAR image_url
        VARCHAR combat_video_url
    }
    
    GAME_EVENTS {
        BIGINT id PK
        BIGINT game_id FK
        INTEGER turn_number
        VARCHAR event_type
        BIGINT affected_player_id FK
        BIGINT affected_army_id FK
        VARCHAR location_type
        INTEGER food_impact
        INTEGER soldier_impact
        INTEGER extra_travel_turns
        VARCHAR description
    }
    
    TURN_ACTIONS {
        BIGINT id PK
        BIGINT game_id FK
        INTEGER turn_number
        BIGINT player_id FK
        VARCHAR action_type
        BIGINT target_city_id FK
        INTEGER soldiers_count
    }

    %% Relationships
    GAMES ||--o{ PLAYERS : "has"
    GAMES ||--o{ CITIES : "contains"
    GAMES ||--o{ MAP_EDGES : "contains"
    GAMES ||--o{ GAME_EVENTS : "records"
    GAMES ||--o{ TURN_ACTIONS : "receives"
    
    PLAYERS ||--o| PLAYER_STATS : "owns"
    PLAYERS ||--o{ CITIES : "controls"
    PLAYERS ||--o{ ARMIES : "commands"
    PLAYERS }o--|| MARSHALS : "selects"
    
    CITIES ||--o{ MAP_EDGES : "connects"
    CITIES ||--o{ ARMIES : "targeted_by"
```
