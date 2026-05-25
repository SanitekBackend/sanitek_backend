-- Roles
INSERT IGNORE INTO role (id, role_name, created_at, updated_at) VALUES
(1, 'ADMIN', NOW(), NOW()),
(2, 'USER', NOW(), NOW()),
(3, 'SUPER_ADMIN', NOW(), NOW());

-- Pollutant units
INSERT IGNORE INTO pollutant_unit (id, notation, created_at, updated_at) VALUES
(1, 'ug/m3', NOW(), NOW()),
(2, 'C', NOW(), NOW());

-- Pollutants
INSERT IGNORE INTO pollutant (id, nomenclature, name, pollutant_unit_id, created_at, updated_at) VALUES
(1, 'NO2', 'Nitrogen Dioxide', 1, NOW(), NOW()),
(2, 'O3', 'Ozone', 1, NOW(), NOW()),
(3, 'PM2.5', 'Particulate Matter 2.5', 1, NOW(), NOW());

-- Municipalities (CDMX - CONEVAL 2020 social vulnerability values)
INSERT IGNORE INTO municipality (id, municipality_name, social_vulnerability, created_at, updated_at) VALUES
(1, 'Alvaro Obregon', -0.37, NOW(), NOW()),
(2, 'Azcapotzalco', -0.58, NOW(), NOW()),
(3, 'Benito Juarez', -1.89, NOW(), NOW()),
(4, 'Coyoacan', -0.55, NOW(), NOW()),
(5, 'Cuajimalpa', -0.48, NOW(), NOW()),
(6, 'Cuauhtemoc', -0.62, NOW(), NOW()),
(7, 'Gustavo A. Madero', 0.31, NOW(), NOW()),
(8, 'Iztacalco', -0.28, NOW(), NOW()),
(9, 'Iztapalapa', 0.92, NOW(), NOW()),
(10, 'La Magdalena Contreras', 0.19, NOW(), NOW()),
(11, 'Miguel Hidalgo', -1.31, NOW(), NOW()),
(12, 'Milpa Alta', 1.24, NOW(), NOW()),
(13, 'Tlahuac', 0.62, NOW(), NOW()),
(14, 'Tlalpan', 0.47, NOW(), NOW()),
(15, 'Venustiano Carranza', -0.21, NOW(), NOW()),
(16, 'Xochimilco', 0.69, NOW(), NOW());

-- Monitoring stations (SINAICA / desired_stations.csv)
INSERT IGNORE INTO station (id, station_short_name, station_name, created_at, updated_at) VALUES
(1, 'AJU', 'Ajusco', NOW(), NOW()),
(2, 'AJM', 'Ajusco Medio', NOW(), NOW()),
(3, 'BJU', 'Benito Juarez', NOW(), NOW()),
(4, 'CAM', 'Camarones', NOW(), NOW()),
(5, 'CCA', 'Centro de Ciencias Atmosfera', NOW(), NOW()),
(6, 'COY', 'Coyoacan', NOW(), NOW()),
(7, 'CUA', 'Cuajimalpa', NOW(), NOW()),
(8, 'GAM', 'Gustavo A. Madero', NOW(), NOW()),
(9, 'HGM', 'Hospital General de Mexico', NOW(), NOW()),
(10, 'IZT', 'Iztacalco', NOW(), NOW()),
(11, 'MER', 'Merced', NOW(), NOW()),
(12, 'MGH', 'Miguel Hidalgo', NOW(), NOW()),
(13, 'MPA', 'Milpa Alta', NOW(), NOW()),
(14, 'PED', 'Pedregal', NOW(), NOW()),
(15, 'SAC', 'Santiago Acahualtepec', NOW(), NOW()),
(16, 'SFE', 'Santa Fe', NOW(), NOW()),
(17, 'SJA', 'San Juan Aragon', NOW(), NOW()),
(18, 'TAH', 'Tlahuac', NOW(), NOW()),
(19, 'UAX', 'UAM Xochimilco', NOW(), NOW()),
(20, 'UIZ', 'UAM Iztapalapa', NOW(), NOW()),
(21, 'LMC', 'La Magdalena Contreras', NOW(), NOW());

INSERT IGNORE INTO municipality_station (municipality_id, station_id) VALUES
(14, 1),
(14, 2),
(3, 3),
(2, 4),
(4, 5),
(4, 6),
(5, 7),
(7, 8),
(6, 9),
(8, 10),
(15, 11),
(11, 12),
(12, 13),
(1, 14),
(9, 15),
(5, 16),
(7, 17),
(13, 18),
(16, 19),
(9, 20),
(10, 21);
