-- ============================================================
-- SportSphere Hub - defense/demo data
-- MySQL 8.0+
--
-- Prerequisite:
--   Run 01_create_sportsphere_database.sql first.
--
-- The script uses dates relative to CURDATE(), so active
-- promotions, future reservations and historical statistics
-- remain useful regardless of the defense date.
--
-- Shared password for every seeded account:
--   Admin#123
-- ============================================================

USE sportsphere;

START TRANSACTION;

-- ============================================================
-- SPORTS
-- ============================================================

INSERT INTO sport (id, name) VALUES
(1, 'Football'),
(2, 'Basketball'),
(3, 'Tennis'),
(4, 'Volleyball'),
(5, 'Swimming'),
(6, 'Futsal'),
(7, 'Badminton'),
(8, 'Padel');

-- ============================================================
-- USERS
-- ============================================================

INSERT INTO app_user
(id, username, password_hash, role, status, first_name, last_name,
 phone, email, profile_image_path, created_at)
VALUES
(1, 'admin', SHA2('Admin#123', 256), 'ADMIN', 'ACTIVE',
 'Admin', 'Adminovic', '0600000000', 'admin@sportsphere.rs',
 'default.png', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 300 DAY), '09:00:00')),

(2, 'marko', SHA2('Admin#123', 256), 'ATHLETE', 'ACTIVE',
 'Marko', 'Markovic', '0611111111', 'marko@example.com',
 'default.png', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 280 DAY), '10:00:00')),

(3, 'ana', SHA2('Admin#123', 256), 'ATHLETE', 'ACTIVE',
 'Ana', 'Anic', '0622222222', 'ana@example.com',
 'default.png', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 250 DAY), '11:00:00')),

(4, 'petar', SHA2('Admin#123', 256), 'EMPLOYEE', 'ACTIVE',
 'Petar', 'Petrovic', '0633333333', 'petar@example.com',
 'default.png', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 240 DAY), '12:00:00')),

(5, 'milica', SHA2('Admin#123', 256), 'ATHLETE', 'ACTIVE',
 'Milica', 'Jovanovic', '0644444444', 'milica@example.com',
 'default.png', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 210 DAY), '13:00:00')),

(6, 'nikola', SHA2('Admin#123', 256), 'ATHLETE', 'ACTIVE',
 'Nikola', 'Nikolic', '0655555555', 'nikola@example.com',
 'default.png', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 190 DAY), '14:00:00')),

(7, 'jovan', SHA2('Admin#123', 256), 'ATHLETE', 'ACTIVE',
 'Jovan', 'Ilic', '0666666666', 'jovan@example.com',
 'default.png', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 170 DAY), '15:00:00')),

(8, 'sara', SHA2('Admin#123', 256), 'ATHLETE', 'ACTIVE',
 'Sara', 'Stojanovic', '0677777777', 'sara@example.com',
 'default.png', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 150 DAY), '16:00:00')),

(9, 'jelena', SHA2('Admin#123', 256), 'EMPLOYEE', 'ACTIVE',
 'Jelena', 'Savic', '0688888888', 'jelena@example.com',
 'default.png', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 130 DAY), '10:30:00')),

(10, 'stefan', SHA2('Admin#123', 256), 'EMPLOYEE', 'ACTIVE',
 'Stefan', 'Milic', '0699999999', 'stefan@example.com',
 'default.png', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 120 DAY), '11:30:00')),

(11, 'luka', SHA2('Admin#123', 256), 'ATHLETE', 'PENDING',
 'Luka', 'Lukic', '0601112233', 'luka@example.com',
 'default.png', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '12:30:00')),

(12, 'ivana', SHA2('Admin#123', 256), 'EMPLOYEE', 'PENDING',
 'Ivana', 'Pavlovic', '0602223344', 'ivana@example.com',
 'default.png', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '13:30:00')),

(13, 'dejan', SHA2('Admin#123', 256), 'ATHLETE', 'REJECTED',
 'Dejan', 'Kovacevic', '0603334455', 'dejan@example.com',
 'default.png', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 20 DAY), '14:30:00')),

(14, 'marina', SHA2('Admin#123', 256), 'ATHLETE', 'DELETED',
 'Marina', 'Popovic', '0604445566', 'marina@example.com',
 'default.png', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 80 DAY), '15:30:00'));

INSERT INTO user_favorite_sport (user_id, sport_id) VALUES
(2, 1), (2, 3), (2, 6),
(3, 2), (3, 4), (3, 5),
(5, 3), (5, 7),
(6, 1), (6, 2), (6, 6),
(7, 4), (7, 5),
(8, 3), (8, 7), (8, 8),
(11, 1);

-- ============================================================
-- ORGANIZATIONS AND EMPLOYEES
-- ============================================================

INSERT INTO organization
(id, name, headquarters_address, registration_number, tax_number)
VALUES
(1, 'Active Sport DOO', 'Vjekoslava Kovaca 11, Beograd',
 '12345678', '123456789'),
(2, 'Vojvodina Recreation DOO', 'Sutjeska 2, Novi Sad',
 '23456789', '234567891'),
(3, 'Southern Sports DOO', '9. brigade 10, Nis',
 '34567890', '345678912');

INSERT INTO organization_employee (organization_id, employee_id) VALUES
(1, 4),
(2, 9),
(3, 10);

-- ============================================================
-- FACILITIES
-- Real addresses are used so the dynamic map resolves markers.
-- Like/dislike counters are calculated later from reviews.
-- ============================================================

INSERT INTO facility
(id, organization_id, name, city, address, description,
 price_per_hour, working_time_from, working_time_to,
 allowed_no_shows, status, like_count, dislike_count, created_at)
VALUES
(1, 1, 'SC Olimp - Zvezdara', 'Beograd',
 'Vjekoslava Kovaca 11',
 'Multi-sport center with outdoor fields, indoor courts and swimming facilities.',
 2600.00, '06:00:00', '23:00:00', 3, 'APPROVED', 0, 0,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 230 DAY), '09:00:00')),

(2, 2, 'SPENS', 'Novi Sad',
 'Sutjeska 2',
 'Large sports and business center with courts, halls and pools.',
 2400.00, '07:00:00', '23:00:00', 3, 'APPROVED', 0, 0,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 220 DAY), '09:00:00')),

(3, 3, 'SC Cair', 'Nis',
 '9. brigade 10',
 'Indoor sports center for team sports, swimming and recreation.',
 2100.00, '07:00:00', '22:00:00', 2, 'APPROVED', 0, 0,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 200 DAY), '09:00:00')),

(4, 1, 'SC Tasmajdan', 'Beograd',
 'Ilije Garasanina 26',
 'Central Belgrade sports center with swimming and futsal facilities.',
 3000.00, '06:00:00', '23:00:00', 3, 'APPROVED', 0, 0,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 180 DAY), '09:00:00')),

(5, 1, 'SC Vozdovac', 'Beograd',
 'Crnotravska 4',
 'Sports center with pools, basketball hall and racket-sport courts.',
 2300.00, '07:00:00', '22:00:00', 3, 'APPROVED', 0, 0,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 160 DAY), '09:00:00')),

(6, 1, 'SRC 11. april', 'Beograd',
 'Autoput za Zagreb 2',
 'Recreation center with tennis, swimming and padel resources.',
 2800.00, '06:00:00', '23:00:00', 3, 'APPROVED', 0, 0,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 140 DAY), '09:00:00')),

(7, 2, 'New Riverside Hall', 'Novi Sad',
 'Futoski put 1',
 'New multi-purpose facility awaiting administrator approval.',
 1900.00, '08:00:00', '22:00:00', 3, 'PENDING', 0, 0,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:00:00')),

(8, 3, 'Rejected Training Dome', 'Nis',
 'Bulevar Nemanjica 10',
 'Example of a facility rejected during administrator review.',
 1700.00, '09:00:00', '21:00:00', 3, 'REJECTED', 0, 0,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 40 DAY), '09:00:00'));

INSERT INTO facility_sport (facility_id, sport_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 5), (1, 6),
(2, 2), (2, 3), (2, 4), (2, 5), (2, 7),
(3, 2), (3, 4), (3, 5), (3, 6),
(4, 5), (4, 6),
(5, 2), (5, 5), (5, 7),
(6, 3), (6, 5), (6, 8),
(7, 1), (7, 2),
(8, 3);

INSERT INTO facility_image (id, facility_id, image_path) VALUES
(1, 1, 'olimp_1.jpg'),
(2, 1, 'olimp_2.jpg'),
(3, 2, 'spens_1.jpg'),
(4, 2, 'spens_2.jpg'),
(5, 3, 'cair_1.jpg'),
(6, 4, 'tasmajdan_1.jpg'),
(7, 5, 'vozdovac_1.jpg'),
(8, 6, '11_april_1.jpg');

-- ============================================================
-- RESOURCES
-- Several same-facility/same-sport/same-type pairs are included
-- specifically for calendar rotation testing.
-- ============================================================

INSERT INTO resource
(id, facility_id, sport_id, name, type, capacity,
 equipment_description, active)
VALUES
(1, 1, 1, 'Football Field A', 'OUTDOOR_FIELD', 14,
 'Artificial grass, floodlights and full-size goals.', TRUE),
(2, 1, 1, 'Football Field B', 'OUTDOOR_FIELD', 14,
 'Artificial grass, floodlights and full-size goals.', TRUE),
(3, 1, 2, 'Basketball Hall A', 'HALL', 12,
 'Parquet floor, scoreboard and changing rooms.', TRUE),
(4, 1, 3, 'Tennis Court A', 'OUTDOOR_FIELD', 4,
 'Clay surface and night lighting.', TRUE),
(5, 1, 5, 'Pool Lane Group A', 'HALL', 8,
 'Four reserved swimming lanes and timing equipment.', TRUE),
(6, 1, 6, 'Futsal Hall A', 'HALL', 12,
 'Indoor goals, scoreboard and spectator benches.', TRUE),

(7, 2, 2, 'Basketball Court A', 'INDOOR_COURT', 12,
 'Competition floor and electronic scoreboard.', TRUE),
(8, 2, 2, 'Basketball Court B', 'INDOOR_COURT', 12,
 'Training court with adjustable baskets.', TRUE),
(9, 2, 3, 'Tennis Court 1', 'INDOOR_COURT', 4,
 'Hard surface and LED lighting.', TRUE),
(10, 2, 4, 'Volleyball Hall A', 'HALL', 12,
 'Competition net, referee stand and scoreboard.', TRUE),
(11, 2, 5, 'Swimming Pool A', 'HALL', 16,
 'Indoor pool with eight lanes.', TRUE),
(12, 2, 7, 'Badminton Court 1', 'INDOOR_COURT', 4,
 'Professional net and non-slip court surface.', TRUE),

(13, 3, 2, 'Basketball Hall Cair', 'HALL', 12,
 'Main hall with spectator seating.', TRUE),
(14, 3, 4, 'Volleyball Hall Cair', 'HALL', 12,
 'Main court with training equipment.', TRUE),
(15, 3, 5, 'Indoor Pool Cair', 'HALL', 16,
 'Heated pool and changing rooms.', TRUE),
(16, 3, 6, 'Futsal Court Cair', 'INDOOR_COURT', 12,
 'Indoor futsal court and scoreboard.', TRUE),

(17, 4, 5, 'Olympic Pool', 'HALL', 24,
 'Olympic-size pool with marked lanes.', TRUE),
(18, 4, 6, 'Futsal Court A', 'INDOOR_COURT', 12,
 'Indoor court with goals and scoreboard.', TRUE),
(19, 4, 6, 'Futsal Court B', 'INDOOR_COURT', 12,
 'Indoor training court with goals.', TRUE),

(20, 5, 2, 'Basketball Hall Banjica', 'HALL', 12,
 'Full-size basketball hall.', TRUE),
(21, 5, 5, 'Swimming Pool Banjica', 'HALL', 20,
 'Indoor competition pool.', TRUE),
(22, 5, 7, 'Badminton Court Banjica', 'INDOOR_COURT', 4,
 'Badminton court with basic equipment.', TRUE),

(23, 6, 3, 'Tennis Court A', 'OUTDOOR_FIELD', 4,
 'Outdoor hard court and floodlights.', TRUE),
(24, 6, 3, 'Tennis Court B', 'OUTDOOR_FIELD', 4,
 'Outdoor hard court and floodlights.', TRUE),
(25, 6, 5, 'Indoor Pool 11 April', 'HALL', 20,
 'Indoor pool with spectator area.', TRUE),
(26, 6, 8, 'Padel Court A', 'INDOOR_COURT', 4,
 'Glass court with rackets available for rent.', TRUE),
(27, 6, 8, 'Padel Court B', 'INDOOR_COURT', 4,
 'Glass court with rackets available for rent.', TRUE),

(28, 7, 1, 'Riverside Football Field', 'OUTDOOR_FIELD', 14,
 'New field unavailable until facility approval.', FALSE);

-- ============================================================
-- RESERVATIONS
-- Future records exercise athlete and employee calendars.
-- Historical records exercise statistics and monthly reports.
-- ============================================================

INSERT INTO reservation
(id, athlete_id, facility_id, resource_id, sport_id,
 start_time, end_time, status, created_at)
VALUES
(1, 2, 1, 1, 1,
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '18:00:00'),
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '20:00:00'),
 'REQUESTED', CURRENT_TIMESTAMP),

(2, 3, 1, 3, 2,
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '17:00:00'),
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '18:00:00'),
 'REQUESTED', CURRENT_TIMESTAMP),

(3, 5, 2, 9, 3,
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '10:00:00'),
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '12:00:00'),
 'CONFIRMED', CURRENT_TIMESTAMP),

(4, 6, 3, 16, 6,
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '20:00:00'),
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '21:00:00'),
 'REQUESTED', CURRENT_TIMESTAMP),

(5, 7, 5, 21, 5,
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '09:00:00'),
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '10:00:00'),
 'REQUESTED', CURRENT_TIMESTAMP),

(6, 8, 6, 26, 8,
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 7 DAY), '19:00:00'),
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 7 DAY), '20:00:00'),
 'CONFIRMED', CURRENT_TIMESTAMP),

(7, 2, 1, 2, 1,
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '20:00:00'),
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '22:00:00'),
 'CONFIRMED', CURRENT_TIMESTAMP),

(8, 3, 2, 8, 2,
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '18:00:00'),
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '19:00:00'),
 'REQUESTED', CURRENT_TIMESTAMP),

(9, 2, 1, 1, 1,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '18:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '20:00:00'),
 'CONFIRMED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 12 DAY), '11:00:00')),

(10, 2, 2, 9, 3,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 20 DAY), '10:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 20 DAY), '11:00:00'),
 'CONFIRMED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 27 DAY), '11:00:00')),

(11, 2, 4, 18, 6,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 35 DAY), '19:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 35 DAY), '20:00:00'),
 'CONFIRMED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 42 DAY), '11:00:00')),

(12, 2, 1, 3, 2,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 65 DAY), '17:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 65 DAY), '18:00:00'),
 'NO_SHOW',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 70 DAY), '11:00:00')),

(13, 2, 6, 23, 3,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 100 DAY), '09:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 100 DAY), '11:00:00'),
 'CONFIRMED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 108 DAY), '11:00:00')),

(14, 3, 1, 3, 2,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 7 DAY), '16:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 7 DAY), '18:00:00'),
 'CONFIRMED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 14 DAY), '12:00:00')),

(15, 3, 2, 10, 4,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 30 DAY), '18:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 30 DAY), '20:00:00'),
 'CONFIRMED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 37 DAY), '12:00:00')),

(16, 3, 3, 15, 5,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 60 DAY), '08:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 60 DAY), '09:00:00'),
 'NO_SHOW',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 67 DAY), '12:00:00')),

(17, 3, 5, 20, 2,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 90 DAY), '20:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 90 DAY), '21:00:00'),
 'CANCELLED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 98 DAY), '12:00:00')),

(18, 5, 2, 9, 3,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 8 DAY), '12:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 8 DAY), '13:00:00'),
 'CONFIRMED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 16 DAY), '13:00:00')),

(19, 5, 6, 24, 3,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 45 DAY), '10:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 45 DAY), '12:00:00'),
 'CONFIRMED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 52 DAY), '13:00:00')),

(20, 5, 2, 12, 7,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 75 DAY), '18:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 75 DAY), '19:00:00'),
 'CANCELLED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 82 DAY), '13:00:00')),

(21, 6, 1, 2, 1,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 10 DAY), '20:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 10 DAY), '22:00:00'),
 'CONFIRMED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 18 DAY), '14:00:00')),

(22, 6, 3, 16, 6,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 40 DAY), '19:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 40 DAY), '20:00:00'),
 'NO_SHOW',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 48 DAY), '14:00:00')),

(23, 6, 4, 19, 6,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 70 DAY), '17:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 70 DAY), '19:00:00'),
 'CONFIRMED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 77 DAY), '14:00:00')),

(24, 7, 3, 15, 5,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 12 DAY), '09:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 12 DAY), '10:00:00'),
 'CONFIRMED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 19 DAY), '15:00:00')),

(25, 7, 5, 21, 5,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 50 DAY), '08:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 50 DAY), '09:00:00'),
 'CONFIRMED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 57 DAY), '15:00:00')),

(26, 8, 6, 26, 8,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '19:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '20:00:00'),
 'CONFIRMED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 13 DAY), '16:00:00')),

(27, 8, 2, 12, 7,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 25 DAY), '17:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 25 DAY), '18:00:00'),
 'CONFIRMED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 32 DAY), '16:00:00')),

(28, 8, 6, 27, 8,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 55 DAY), '18:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 55 DAY), '19:00:00'),
 'NO_SHOW',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 62 DAY), '16:00:00')),

(29, 2, 1, 1, 1,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 160 DAY), '18:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 160 DAY), '20:00:00'),
 'CONFIRMED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 167 DAY), '11:00:00')),

(30, 3, 2, 7, 2,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 150 DAY), '17:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 150 DAY), '19:00:00'),
 'CONFIRMED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 157 DAY), '12:00:00'));

-- ============================================================
-- TRAINERS AND TRAINING SESSIONS
-- ============================================================

INSERT INTO trainer
(id, facility_id, sport_id, first_name, last_name,
 specialization, average_rating, price_per_hour, active)
VALUES
(1, 1, 1, 'Milan', 'Jovanovic',
 'Football conditioning and technique', 4.70, 3000.00, TRUE),
(2, 1, 2, 'Ivana', 'Nikolic',
 'Basketball shooting and footwork', 4.85, 3500.00, TRUE),
(3, 2, 3, 'Stefan', 'Ilic',
 'Tennis for beginner and intermediate players', 4.50, 2800.00, TRUE),
(4, 2, 7, 'Tamara', 'Savic',
 'Badminton technique and match preparation', 4.60, 2600.00, TRUE),
(5, 3, 5, 'Aleksandar', 'Mitic',
 'Swimming technique and endurance', 4.90, 3200.00, TRUE),
(6, 4, 6, 'Nemanja', 'Lazic',
 'Futsal tactics and ball control', 4.40, 2900.00, TRUE),
(7, 6, 8, 'Marija', 'Tomic',
 'Padel fundamentals and doubles tactics', 4.75, 3400.00, TRUE),
(8, 5, 2, 'Bojan', 'Radic',
 'Basketball conditioning', 4.20, 2700.00, FALSE);

INSERT INTO training_session
(id, athlete_id, trainer_id, facility_id, sport_id,
 start_time, end_time, price_per_hour, status, created_at)
VALUES
(1, 2, 1, 1, 1,
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '12:00:00'),
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '13:00:00'),
 3000.00, 'SCHEDULED', CURRENT_TIMESTAMP),

(2, 3, 2, 1, 2,
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '14:00:00'),
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '15:00:00'),
 3500.00, 'SCHEDULED', CURRENT_TIMESTAMP),

(3, 5, 3, 2, 3,
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '09:00:00'),
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '10:00:00'),
 2800.00, 'SCHEDULED', CURRENT_TIMESTAMP),

(4, 8, 7, 6, 8,
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 8 DAY), '18:00:00'),
 TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 8 DAY), '19:00:00'),
 3400.00, 'SCHEDULED', CURRENT_TIMESTAMP),

(5, 2, 1, 1, 1,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 15 DAY), '12:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 15 DAY), '13:00:00'),
 3000.00, 'HELD',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 22 DAY), '10:00:00')),

(6, 3, 2, 1, 2,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 25 DAY), '14:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 25 DAY), '15:00:00'),
 3500.00, 'HELD',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 32 DAY), '10:00:00')),

(7, 5, 4, 2, 7,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 35 DAY), '16:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 35 DAY), '17:00:00'),
 2600.00, 'CANCELLED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 42 DAY), '10:00:00')),

(8, 6, 6, 4, 6,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 45 DAY), '18:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 45 DAY), '19:00:00'),
 2900.00, 'NO_SHOW',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 52 DAY), '10:00:00')),

(9, 7, 5, 3, 5,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 55 DAY), '11:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 55 DAY), '12:00:00'),
 3200.00, 'HELD',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 62 DAY), '10:00:00')),

(10, 8, 7, 6, 8,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 65 DAY), '17:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 65 DAY), '18:00:00'),
 3400.00, 'HELD',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 72 DAY), '10:00:00'));

-- ============================================================
-- TEAMMATE ADS AND REQUESTS
-- ============================================================

INSERT INTO teammate_ad
(id, athlete_id, sport_id, city, play_date, start_time, end_time,
 needed_players, accepted_players, status, created_at)
VALUES
(1, 2, 1, 'Beograd', DATE_ADD(CURDATE(), INTERVAL 5 DAY),
 '19:00:00', '21:00:00', 3, 1, 'ACTIVE', CURRENT_TIMESTAMP),
(2, 3, 3, 'Novi Sad', DATE_ADD(CURDATE(), INTERVAL 6 DAY),
 '10:00:00', '12:00:00', 2, 1, 'ACTIVE', CURRENT_TIMESTAMP),
(3, 6, 2, 'Beograd', DATE_ADD(CURDATE(), INTERVAL 8 DAY),
 '18:00:00', '20:00:00', 4, 0, 'ACTIVE', CURRENT_TIMESTAMP),
(4, 5, 7, 'Novi Sad', DATE_SUB(CURDATE(), INTERVAL 10 DAY),
 '17:00:00', '19:00:00', 1, 1, 'COMPLETED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 25 DAY), '10:00:00')),
(5, 8, 8, 'Beograd', DATE_SUB(CURDATE(), INTERVAL 20 DAY),
 '18:00:00', '20:00:00', 2, 0, 'CLOSED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 35 DAY), '10:00:00'));

INSERT INTO teammate_request
(id, ad_id, athlete_id, status, created_at)
VALUES
(1, 1, 3, 'ACCEPTED', CURRENT_TIMESTAMP),
(2, 1, 7, 'PENDING', CURRENT_TIMESTAMP),
(3, 1, 8, 'REJECTED', CURRENT_TIMESTAMP),
(4, 2, 5, 'ACCEPTED', CURRENT_TIMESTAMP),
(5, 2, 8, 'PENDING', CURRENT_TIMESTAMP),
(6, 3, 2, 'PENDING', CURRENT_TIMESTAMP),
(7, 4, 8, 'ACCEPTED',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 20 DAY), '12:00:00'));

-- ============================================================
-- EQUIPMENT
-- ============================================================

INSERT INTO equipment
(id, facility_id, sport_id, name, image_path, price, stock, active)
VALUES
(1, 1, 1, 'Football Ball Pro', 'football_ball.jpg', 4200.00, 15, TRUE),
(2, 1, 1, 'Training Bib Set', 'training_bibs.jpg', 1800.00, 12, TRUE),
(3, 1, 2, 'Basketball Match Ball', 'basketball_ball.jpg', 5200.00, 9, TRUE),
(4, 2, 4, 'Volleyball Knee Pads', 'knee_pads.jpg', 2600.00, 14, TRUE),
(5, 2, 3, 'Tennis Racket Starter', 'tennis_racket.jpg', 11000.00, 6, TRUE),
(6, 2, 3, 'Tennis Balls Pack', 'tennis_balls.jpg', 1400.00, 24, TRUE),
(7, 2, 7, 'Badminton Racket', 'badminton_racket.jpg', 4800.00, 10, TRUE),
(8, 3, 5, 'Swimming Goggles', 'swimming_goggles.jpg', 3000.00, 18, TRUE),
(9, 3, 5, 'Swimming Cap', 'swimming_cap.jpg', 900.00, 30, TRUE),
(10, 3, 6, 'Futsal Ball', 'futsal_ball.jpg', 3900.00, 11, TRUE),
(11, 6, 8, 'Padel Racket', 'padel_racket.jpg', 14500.00, 5, TRUE),
(12, 6, 8, 'Padel Balls Pack', 'padel_balls.jpg', 1600.00, 16, TRUE),
(13, 5, 2, 'Basketball Shoes', 'basketball_shoes.jpg', 9800.00, 7, TRUE),
(14, 3, 4, 'Volleyball Match Ball', 'volleyball_ball.jpg', 4300.00, 8, TRUE),
(15, 4, 5, 'Sports Towel', 'sports_towel.jpg', 1200.00, 25, TRUE);

INSERT INTO equipment_order
(id, athlete_id, facility_id, status, total_price, created_at, updated_at)
VALUES
(1, 2, 1, 'ORDERED', 6000.00,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '12:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '12:00:00')),
(2, 3, 2, 'ACCEPTED', 13800.00,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '13:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:00:00')),
(3, 5, 2, 'PICKED_UP', 4800.00,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 8 DAY), '14:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '10:00:00')),
(4, 6, 3, 'PICKED_UP', 8200.00,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 10 DAY), '15:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 7 DAY), '10:00:00')),
(5, 7, 3, 'CANCELLED', 3000.00,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 12 DAY), '16:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 11 DAY), '10:00:00')),
(6, 8, 6, 'ORDERED', 17700.00,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '17:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '17:00:00')),
(7, 2, 1, 'PICKED_UP', 4200.00,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 40 DAY), '12:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 37 DAY), '10:00:00')),
(8, 3, 5, 'ACCEPTED', 9800.00,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '13:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '10:00:00')),
(9, 7, 4, 'PICKED_UP', 2400.00,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 18 DAY), '14:00:00'),
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 15 DAY), '10:00:00'));

INSERT INTO equipment_order_item
(id, order_id, equipment_id, quantity, unit_price)
VALUES
(1, 1, 1, 1, 4200.00),
(2, 1, 2, 1, 1800.00),
(3, 2, 5, 1, 11000.00),
(4, 2, 6, 2, 1400.00),
(5, 3, 7, 1, 4800.00),
(6, 4, 10, 1, 3900.00),
(7, 4, 14, 1, 4300.00),
(8, 5, 8, 1, 3000.00),
(9, 6, 11, 1, 14500.00),
(10, 6, 12, 2, 1600.00),
(11, 7, 1, 1, 4200.00),
(12, 8, 13, 1, 9800.00),
(13, 9, 15, 2, 1200.00);

-- ============================================================
-- PROMOTIONS
-- ============================================================

INSERT INTO promotion
(id, facility_id, sport_id, name, valid_from, valid_to,
 discount_type, discount_value, active, created_at)
VALUES
(1, 1, 1, 'Football Evening Discount',
 DATE_SUB(CURDATE(), INTERVAL 2 DAY),
 DATE_ADD(CURDATE(), INTERVAL 20 DAY),
 'PERCENT', 15.00, TRUE, CURRENT_TIMESTAMP),

(2, 1, NULL, 'Olimp Summer Promotion',
 DATE_SUB(CURDATE(), INTERVAL 1 DAY),
 DATE_ADD(CURDATE(), INTERVAL 30 DAY),
 'FIXED', 500.00, TRUE, CURRENT_TIMESTAMP),

(3, 2, 3, 'Tennis Weekend Promotion',
 DATE_SUB(CURDATE(), INTERVAL 3 DAY),
 DATE_ADD(CURDATE(), INTERVAL 15 DAY),
 'PERCENT', 10.00, TRUE, CURRENT_TIMESTAMP),

(4, 3, 5, 'Morning Swimming Discount',
 CURDATE(),
 DATE_ADD(CURDATE(), INTERVAL 10 DAY),
 'FIXED', 300.00, TRUE, CURRENT_TIMESTAMP),

(5, 6, 8, 'Padel Launch Week',
 DATE_ADD(CURDATE(), INTERVAL 5 DAY),
 DATE_ADD(CURDATE(), INTERVAL 25 DAY),
 'PERCENT', 20.00, TRUE, CURRENT_TIMESTAMP),

(6, 5, 2, 'Basketball School Offer',
 DATE_ADD(CURDATE(), INTERVAL 3 DAY),
 DATE_ADD(CURDATE(), INTERVAL 14 DAY),
 'FIXED', 400.00, TRUE, CURRENT_TIMESTAMP),

(7, 4, NULL, 'Expired Winter Promotion',
 DATE_SUB(CURDATE(), INTERVAL 30 DAY),
 DATE_SUB(CURDATE(), INTERVAL 5 DAY),
 'PERCENT', 12.00, FALSE,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 40 DAY), '10:00:00')),

(8, 2, 7, 'Paused Badminton Promotion',
 DATE_SUB(CURDATE(), INTERVAL 10 DAY),
 DATE_ADD(CURDATE(), INTERVAL 5 DAY),
 'PERCENT', 8.00, FALSE,
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 15 DAY), '10:00:00'));

-- ============================================================
-- FACILITY REVIEWS
-- Every seeded review is backed by one CONFIRMED reservation.
-- Several athlete/facility pairs intentionally keep unused
-- quota so new reviews can be demonstrated after seeding.
-- ============================================================

INSERT INTO facility_review
(id, facility_id, athlete_id, reaction, comment_text, created_at)
VALUES
(1, 1, 2, 'LIKE',
 'The football fields are well maintained and the evening lighting is excellent.',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 150 DAY), '20:30:00')),
(2, 1, 2, 'LIKE',
 'My second visit was just as good, and the staff prepared the field on time.',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '20:30:00')),
(3, 1, 3, 'LIKE',
 'Clean changing rooms and a very useful basketball scoreboard.',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '18:30:00')),
(4, 1, 6, 'LIKE',
 'Booking was simple and the staff confirmed the reservation quickly.',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 9 DAY), '22:15:00')),

(5, 2, 3, 'DISLIKE',
 'The basketball court was good, but the changing rooms were crowded.',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 148 DAY), '20:00:00')),
(6, 2, 2, 'LIKE',
 'The indoor tennis court is comfortable even during bad weather.',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 18 DAY), '12:00:00')),
(7, 2, 3, 'LIKE',
 'The volleyball hall was clean and the equipment was ready when we arrived.',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 28 DAY), '20:30:00')),
(8, 2, 5, 'LIKE',
 'Good court surface and enough space around the baseline.',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 7 DAY), '14:00:00')),
(9, 2, 8, 'LIKE',
 'Badminton equipment was ready before our scheduled time.',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 23 DAY), '19:00:00')),

(10, 3, 7, 'LIKE',
 'The pool was clean and the lane organization worked well.',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 11 DAY), '11:00:00')),

(11, 4, 2, 'LIKE',
 'Central location and a good futsal hall for a small group.',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 34 DAY), '21:00:00')),
(12, 4, 6, 'DISLIKE',
 'The court rotation is useful, but the changing rooms need better maintenance.',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 68 DAY), '19:30:00')),

(13, 5, 7, 'LIKE',
 'Swimming facilities are clean and the morning term was quiet.',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 48 DAY), '10:00:00')),

(14, 6, 5, 'LIKE',
 'Both tennis courts have consistent surfaces and good lighting.',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 43 DAY), '13:00:00')),
(15, 6, 8, 'LIKE',
 'The new padel courts are excellent for doubles matches.',
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '21:00:00'));

-- Synchronize the aggregate counters used by the Facility
-- entity and the TOP 3 query.
UPDATE facility f
SET
    like_count = (
        SELECT COUNT(*)
        FROM facility_review fr
        WHERE fr.facility_id = f.id
          AND fr.reaction = 'LIKE'
    ),
    dislike_count = (
        SELECT COUNT(*)
        FROM facility_review fr
        WHERE fr.facility_id = f.id
          AND fr.reaction = 'DISLIKE'
    )
WHERE f.id > 0;

COMMIT;

-- ============================================================
-- QUICK VERIFICATION
-- Expected important totals:
--   app_user        14
--   facility         8 (6 approved)
--   resource        28
--   reservation     30
--   facility_review 15
-- ============================================================

SELECT 'app_user' AS table_name, COUNT(*) AS row_count FROM app_user
UNION ALL
SELECT 'sport', COUNT(*) FROM sport
UNION ALL
SELECT 'organization', COUNT(*) FROM organization
UNION ALL
SELECT 'facility', COUNT(*) FROM facility
UNION ALL
SELECT 'resource', COUNT(*) FROM resource
UNION ALL
SELECT 'reservation', COUNT(*) FROM reservation
UNION ALL
SELECT 'trainer', COUNT(*) FROM trainer
UNION ALL
SELECT 'training_session', COUNT(*) FROM training_session
UNION ALL
SELECT 'teammate_ad', COUNT(*) FROM teammate_ad
UNION ALL
SELECT 'teammate_request', COUNT(*) FROM teammate_request
UNION ALL
SELECT 'equipment', COUNT(*) FROM equipment
UNION ALL
SELECT 'equipment_order', COUNT(*) FROM equipment_order
UNION ALL
SELECT 'promotion', COUNT(*) FROM promotion
UNION ALL
SELECT 'facility_review', COUNT(*) FROM facility_review;

SELECT
    f.id,
    f.name,
    f.status,
    f.like_count,
    f.dislike_count,
    COUNT(fr.id) AS review_count
FROM facility f
LEFT JOIN facility_review fr
    ON fr.facility_id = f.id
GROUP BY
    f.id,
    f.name,
    f.status,
    f.like_count,
    f.dislike_count
ORDER BY
    f.like_count DESC,
    f.name ASC;
