-- schema.sql
CREATE TABLE IF NOT EXISTS marshals (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    ability_name VARCHAR(255),
    ability_description TEXT,
    disadvantage_description TEXT,
    food_production INT,
    soldier_production INT,
    attack_kill_ratio DOUBLE PRECISION,
    reveals_attack_target BOOLEAN,
    special_ability_type VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS games (
    id BIGSERIAL PRIMARY KEY,
    room_code VARCHAR(10) UNIQUE,
    status VARCHAR(20) NOT NULL,
    current_turn_number INT NOT NULL,
    winner_player_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS players (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    is_alive BOOLEAN DEFAULT TRUE,
    reroll_count INT DEFAULT 0,
    marshal_id BIGINT REFERENCES marshals(id) ON DELETE SET NULL
);
