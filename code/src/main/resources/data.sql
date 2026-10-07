-- data.sql
INSERT INTO marshals (name, ability_name, ability_description, disadvantage_description, food_production, soldier_production, attack_kill_ratio, reveals_attack_target, special_ability_type)
VALUES 
('Zhuge Liang', 'Weather Master', 'Immune to weather penalties', 'High upkeep', 0, 0, 1.0, false, 'WEATHER_IMMUNITY'),
('Lu Bu', 'Unmatched Valor', 'High combat power', 'Low intelligence', 0, 0, 1.5, false, 'NONE')
ON CONFLICT DO NOTHING;
