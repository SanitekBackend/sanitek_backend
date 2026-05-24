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

-- Municipalities (CDMX - CONEVAL 2020 social backlog index)
INSERT IGNORE INTO municipality (id, municipality_name, social_vulnerability, social_index, created_at, updated_at) VALUES
(1, 'Alvaro Obregon', -0.37, 'LOW', NOW(), NOW()),
(2, 'Azcapotzalco', -0.58, 'LOW', NOW(), NOW()),
(3, 'Benito Juarez', -1.89, 'LOW', NOW(), NOW()),
(4, 'Coyoacan', -0.55, 'LOW', NOW(), NOW()),
(5, 'Cuajimalpa', -0.48, 'LOW', NOW(), NOW()),
(6, 'Cuauhtemoc', -0.62, 'LOW', NOW(), NOW()),
(7, 'Gustavo A. Madero', 0.31, 'MEDIUM', NOW(), NOW()),
(8, 'Iztacalco', -0.28, 'LOW', NOW(), NOW()),
(9, 'Iztapalapa', 0.92, 'HIGH', NOW(), NOW()),
(10, 'La Magdalena Contreras', 0.19, 'MEDIUM', NOW(), NOW()),
(11, 'Miguel Hidalgo', -1.31, 'LOW', NOW(), NOW()),
(12, 'Milpa Alta', 1.24, 'HIGH', NOW(), NOW()),
(13, 'Tlahuac', 0.62, 'MEDIUM', NOW(), NOW()),
(14, 'Tlalpan', 0.47, 'MEDIUM', NOW(), NOW()),
(15, 'Venustiano Carranza', -0.21, 'LOW', NOW(), NOW()),
(16, 'Xochimilco', 0.69, 'MEDIUM', NOW(), NOW());

-- Monitoring stations (SINAICA / desired_stations.csv)
INSERT IGNORE INTO station (id, station_short_name, station_name, municipality_id, created_at, updated_at) VALUES
(1, 'AJU', 'Ajusco', 14, NOW(), NOW()),
(2, 'AJM', 'Ajusco Medio', 14, NOW(), NOW()),
(3, 'BJU', 'Benito Juarez', 3, NOW(), NOW()),
(4, 'CAM', 'Camarones', 2, NOW(), NOW()),
(5, 'CCA', 'Centro de Ciencias Atmosfera', 4, NOW(), NOW()),
(6, 'COY', 'Coyoacan', 4, NOW(), NOW()),
(7, 'CUA', 'Cuajimalpa', 5, NOW(), NOW()),
(8, 'GAM', 'Gustavo A. Madero', 7, NOW(), NOW()),
(9, 'HGM', 'Hospital General de Mexico', 6, NOW(), NOW()),
(10, 'IZT', 'Iztacalco', 8, NOW(), NOW()),
(11, 'MER', 'Merced', 15, NOW(), NOW()),
(12, 'MGH', 'Miguel Hidalgo', 11, NOW(), NOW()),
(13, 'MPA', 'Milpa Alta', 12, NOW(), NOW()),
(14, 'PED', 'Pedregal', 1, NOW(), NOW()),
(15, 'SAC', 'Santiago Acahualtepec', 9, NOW(), NOW()),
(16, 'SFE', 'Santa Fe', 5, NOW(), NOW()),
(17, 'SJA', 'San Juan Aragon', 7, NOW(), NOW()),
(18, 'TAH', 'Tlahuac', 13, NOW(), NOW()),
(19, 'UAX', 'UAM Xochimilco', 16, NOW(), NOW()),
(20, 'UIZ', 'UAM Iztapalapa', 9, NOW(), NOW()),
(21, 'LMC', 'La Magdalena Contreras', 10, NOW(), NOW());
