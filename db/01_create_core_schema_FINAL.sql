-- ============================================================
-- SportSphere Hub - FINAL complete database schema
-- MySQL 8.0+
--
-- WARNING:
-- Running this script removes the existing `sportsphere`
-- database and creates it again from scratch.
--
-- Run order:
--   1. 01_create_core_schema_FINAL.sql
--   2. 02_seed_core_data_FINAL.sql
--
-- This script is compatible with MySQL Workbench safe-update
-- mode. It uses only DDL statements and does not disable
-- SQL_SAFE_UPDATES.
--
-- VERIFICATION MARKER:
-- training_session.resource_id is BIGINT NOT NULL, references
-- resource(id), and is indexed for calendar lookups.
-- ============================================================

DROP DATABASE IF EXISTS sportsphere;

CREATE DATABASE sportsphere
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE sportsphere;

-- ============================================================
-- SPORTS AND USERS
-- ============================================================

CREATE TABLE sport (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE app_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,

    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    email VARCHAR(150) NOT NULL UNIQUE,
    profile_image_path VARCHAR(255),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_app_user_role
        CHECK (role IN ('ATHLETE', 'EMPLOYEE', 'ADMIN')),
    CONSTRAINT chk_app_user_status
        CHECK (status IN ('PENDING', 'ACTIVE', 'REJECTED', 'DELETED'))
);

CREATE INDEX idx_app_user_role_status
    ON app_user(role, status);

CREATE TABLE user_favorite_sport (
    user_id BIGINT NOT NULL,
    sport_id BIGINT NOT NULL,

    PRIMARY KEY (user_id, sport_id),

    CONSTRAINT fk_favorite_user
        FOREIGN KEY (user_id) REFERENCES app_user(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_favorite_sport
        FOREIGN KEY (sport_id) REFERENCES sport(id)
        ON DELETE CASCADE
);

-- ============================================================
-- ORGANIZATIONS AND FACILITIES
-- ============================================================

CREATE TABLE organization (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    headquarters_address VARCHAR(255) NOT NULL,
    registration_number CHAR(8) NOT NULL UNIQUE,
    tax_number CHAR(9) NOT NULL UNIQUE,

    CONSTRAINT chk_organization_registration_number
        CHECK (registration_number REGEXP '^[0-9]{8}$'),
    CONSTRAINT chk_organization_tax_number
        CHECK (tax_number REGEXP '^[1-9][0-9]{8}$')
);

CREATE TABLE organization_employee (
    organization_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,

    PRIMARY KEY (organization_id, employee_id),

    CONSTRAINT fk_organization_employee_organization
        FOREIGN KEY (organization_id) REFERENCES organization(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_organization_employee_user
        FOREIGN KEY (employee_id) REFERENCES app_user(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_organization_employee_employee
    ON organization_employee(employee_id);

CREATE TABLE facility (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,

    name VARCHAR(150) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    description TEXT,

    price_per_hour DECIMAL(10, 2) NOT NULL,
    working_time_from TIME NOT NULL,
    working_time_to TIME NOT NULL,

    allowed_no_shows INT NOT NULL DEFAULT 3,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    like_count INT NOT NULL DEFAULT 0,
    dislike_count INT NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_facility_organization
        FOREIGN KEY (organization_id) REFERENCES organization(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_facility_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT chk_facility_price
        CHECK (price_per_hour >= 0),
    CONSTRAINT chk_facility_no_shows
        CHECK (allowed_no_shows >= 0),
    CONSTRAINT chk_facility_like_count
        CHECK (like_count >= 0),
    CONSTRAINT chk_facility_dislike_count
        CHECK (dislike_count >= 0),
    CONSTRAINT chk_facility_working_time
        CHECK (working_time_to > working_time_from)
);

CREATE INDEX idx_facility_status_like_count
    ON facility(status, like_count);

CREATE INDEX idx_facility_organization
    ON facility(organization_id);

CREATE INDEX idx_facility_city_status
    ON facility(city, status);

CREATE TABLE facility_sport (
    facility_id BIGINT NOT NULL,
    sport_id BIGINT NOT NULL,

    PRIMARY KEY (facility_id, sport_id),

    CONSTRAINT fk_facility_sport_facility
        FOREIGN KEY (facility_id) REFERENCES facility(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_facility_sport_sport
        FOREIGN KEY (sport_id) REFERENCES sport(id)
        ON DELETE CASCADE
);

CREATE TABLE facility_image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id BIGINT NOT NULL,
    image_path VARCHAR(255) NOT NULL,

    CONSTRAINT fk_facility_image_facility
        FOREIGN KEY (facility_id) REFERENCES facility(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_facility_image_facility
    ON facility_image(facility_id);

CREATE TABLE resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id BIGINT NOT NULL,
    sport_id BIGINT NOT NULL,

    name VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL,
    capacity INT NOT NULL,
    equipment_description VARCHAR(300),
    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_resource_facility
        FOREIGN KEY (facility_id) REFERENCES facility(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_resource_sport
        FOREIGN KEY (sport_id) REFERENCES sport(id),

    CONSTRAINT uq_resource_facility_name
        UNIQUE (facility_id, name),
    CONSTRAINT chk_resource_type
        CHECK (type IN ('OUTDOOR_FIELD', 'INDOOR_COURT', 'HALL')),
    CONSTRAINT chk_resource_capacity
        CHECK (capacity >= 1)
);

CREATE INDEX idx_resource_facility_active
    ON resource(facility_id, active);

CREATE INDEX idx_resource_rotation
    ON resource(facility_id, sport_id, type, active);

-- ============================================================
-- RESERVATIONS
-- ============================================================

CREATE TABLE reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    athlete_id BIGINT NOT NULL,
    facility_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    sport_id BIGINT NOT NULL,

    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reservation_athlete
        FOREIGN KEY (athlete_id) REFERENCES app_user(id),
    CONSTRAINT fk_reservation_facility
        FOREIGN KEY (facility_id) REFERENCES facility(id),
    CONSTRAINT fk_reservation_resource
        FOREIGN KEY (resource_id) REFERENCES resource(id),
    CONSTRAINT fk_reservation_sport
        FOREIGN KEY (sport_id) REFERENCES sport(id),

    CONSTRAINT chk_reservation_status
        CHECK (status IN ('REQUESTED', 'CONFIRMED', 'CANCELLED', 'NO_SHOW')),
    CONSTRAINT chk_reservation_interval
        CHECK (end_time > start_time)
);

CREATE INDEX idx_reservation_athlete
    ON reservation(athlete_id);

CREATE INDEX idx_reservation_facility
    ON reservation(facility_id);

CREATE INDEX idx_reservation_resource_time
    ON reservation(resource_id, start_time, end_time);

CREATE INDEX idx_reservation_status
    ON reservation(status);

-- ============================================================
-- TRAINERS AND INDIVIDUAL TRAINING
-- ============================================================

CREATE TABLE trainer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    facility_id BIGINT NOT NULL,
    sport_id BIGINT NOT NULL,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    specialization VARCHAR(150) NOT NULL,
    average_rating DECIMAL(3, 2) NOT NULL DEFAULT 0.00,
    price_per_hour DECIMAL(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_trainer_facility
        FOREIGN KEY (facility_id) REFERENCES facility(id),
    CONSTRAINT fk_trainer_sport
        FOREIGN KEY (sport_id) REFERENCES sport(id),

    CONSTRAINT chk_trainer_rating
        CHECK (average_rating >= 0 AND average_rating <= 5),
    CONSTRAINT chk_trainer_price
        CHECK (price_per_hour >= 0)
);

CREATE INDEX idx_trainer_facility_sport
    ON trainer(facility_id, sport_id);

CREATE INDEX idx_trainer_active
    ON trainer(active);

CREATE TABLE training_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    athlete_id BIGINT NOT NULL,
    trainer_id BIGINT NOT NULL,
    facility_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    sport_id BIGINT NOT NULL,

    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    price_per_hour DECIMAL(10, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_training_session_athlete
        FOREIGN KEY (athlete_id) REFERENCES app_user(id),
    CONSTRAINT fk_training_session_trainer
        FOREIGN KEY (trainer_id) REFERENCES trainer(id),
    CONSTRAINT fk_training_session_facility
        FOREIGN KEY (facility_id) REFERENCES facility(id),
    CONSTRAINT fk_training_session_resource
        FOREIGN KEY (resource_id) REFERENCES resource(id),
    CONSTRAINT fk_training_session_sport
        FOREIGN KEY (sport_id) REFERENCES sport(id),

    CONSTRAINT chk_training_session_status
        CHECK (status IN ('SCHEDULED', 'HELD', 'CANCELLED', 'NO_SHOW')),
    CONSTRAINT chk_training_session_interval
        CHECK (end_time > start_time),
    CONSTRAINT chk_training_session_price
        CHECK (price_per_hour >= 0)
);

CREATE INDEX idx_training_athlete
    ON training_session(athlete_id);

CREATE INDEX idx_training_trainer_time
    ON training_session(trainer_id, start_time, end_time);

CREATE INDEX idx_training_resource_time
    ON training_session(resource_id, start_time, end_time);

CREATE INDEX idx_training_facility
    ON training_session(facility_id);

CREATE INDEX idx_training_status
    ON training_session(status);

-- ============================================================
-- TEAMMATE ADS
-- ============================================================

CREATE TABLE teammate_ad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    athlete_id BIGINT NOT NULL,
    sport_id BIGINT NOT NULL,

    city VARCHAR(100) NOT NULL,
    play_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    needed_players INT NOT NULL,
    accepted_players INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_teammate_ad_athlete
        FOREIGN KEY (athlete_id) REFERENCES app_user(id),
    CONSTRAINT fk_teammate_ad_sport
        FOREIGN KEY (sport_id) REFERENCES sport(id),

    CONSTRAINT chk_teammate_ad_needed_players
        CHECK (needed_players > 0),
    CONSTRAINT chk_teammate_ad_accepted_players
        CHECK (accepted_players >= 0 AND accepted_players <= needed_players),
    CONSTRAINT chk_teammate_ad_interval
        CHECK (end_time > start_time),
    CONSTRAINT chk_teammate_ad_status
        CHECK (status IN ('ACTIVE', 'COMPLETED', 'CLOSED'))
);

CREATE INDEX idx_teammate_ad_active
    ON teammate_ad(status, city, sport_id, play_date);

CREATE INDEX idx_teammate_ad_owner
    ON teammate_ad(athlete_id);

CREATE TABLE teammate_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    ad_id BIGINT NOT NULL,
    athlete_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_teammate_request_ad
        FOREIGN KEY (ad_id) REFERENCES teammate_ad(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_teammate_request_athlete
        FOREIGN KEY (athlete_id) REFERENCES app_user(id),

    CONSTRAINT uq_teammate_request_ad_athlete
        UNIQUE (ad_id, athlete_id),
    CONSTRAINT chk_teammate_request_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED'))
);

CREATE INDEX idx_teammate_request_ad
    ON teammate_request(ad_id);

CREATE INDEX idx_teammate_request_athlete
    ON teammate_request(athlete_id);

CREATE INDEX idx_teammate_request_status
    ON teammate_request(status);

-- ============================================================
-- EQUIPMENT SHOP
-- ============================================================

CREATE TABLE equipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    facility_id BIGINT NOT NULL,
    sport_id BIGINT NOT NULL,

    name VARCHAR(150) NOT NULL,
    image_path VARCHAR(255),
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_equipment_facility
        FOREIGN KEY (facility_id) REFERENCES facility(id),
    CONSTRAINT fk_equipment_sport
        FOREIGN KEY (sport_id) REFERENCES sport(id),

    CONSTRAINT chk_equipment_price
        CHECK (price >= 0),
    CONSTRAINT chk_equipment_stock
        CHECK (stock >= 0)
);

CREATE INDEX idx_equipment_facility
    ON equipment(facility_id);

CREATE INDEX idx_equipment_sport_active
    ON equipment(sport_id, active);

CREATE TABLE equipment_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    athlete_id BIGINT NOT NULL,
    facility_id BIGINT NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'ORDERED',
    total_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_equipment_order_athlete
        FOREIGN KEY (athlete_id) REFERENCES app_user(id),
    CONSTRAINT fk_equipment_order_facility
        FOREIGN KEY (facility_id) REFERENCES facility(id),

    CONSTRAINT chk_equipment_order_status
        CHECK (status IN ('ORDERED', 'ACCEPTED', 'PICKED_UP', 'CANCELLED')),
    CONSTRAINT chk_equipment_order_total
        CHECK (total_price >= 0)
);

CREATE INDEX idx_equipment_order_athlete
    ON equipment_order(athlete_id);

CREATE INDEX idx_equipment_order_facility
    ON equipment_order(facility_id);

CREATE INDEX idx_equipment_order_status
    ON equipment_order(status);

CREATE TABLE equipment_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    order_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,

    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id) REFERENCES equipment_order(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_order_item_equipment
        FOREIGN KEY (equipment_id) REFERENCES equipment(id),

    CONSTRAINT chk_order_item_quantity
        CHECK (quantity > 0),
    CONSTRAINT chk_order_item_price
        CHECK (unit_price >= 0)
);

CREATE INDEX idx_order_item_order
    ON equipment_order_item(order_id);

CREATE INDEX idx_order_item_equipment
    ON equipment_order_item(equipment_id);

-- ============================================================
-- PROMOTIONS
-- ============================================================

CREATE TABLE promotion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    facility_id BIGINT NOT NULL,
    sport_id BIGINT NULL,

    name VARCHAR(150) NOT NULL,
    valid_from DATE NOT NULL,
    valid_to DATE NOT NULL,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_promotion_facility
        FOREIGN KEY (facility_id) REFERENCES facility(id),
    CONSTRAINT fk_promotion_sport
        FOREIGN KEY (sport_id) REFERENCES sport(id),

    CONSTRAINT chk_promotion_period
        CHECK (valid_to >= valid_from),
    CONSTRAINT chk_promotion_type
        CHECK (discount_type IN ('PERCENT', 'FIXED')),
    CONSTRAINT chk_promotion_value
        CHECK (discount_value >= 0)
);

CREATE INDEX idx_promotion_active_period
    ON promotion(active, valid_from, valid_to);

CREATE INDEX idx_promotion_facility
    ON promotion(facility_id);

CREATE INDEX idx_promotion_sport
    ON promotion(sport_id);

-- ============================================================
-- FACILITY REVIEWS
-- ============================================================

CREATE TABLE facility_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    facility_id BIGINT NOT NULL,
    athlete_id BIGINT NOT NULL,

    reaction VARCHAR(10) NOT NULL,
    comment_text VARCHAR(1000) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_facility_review_facility
        FOREIGN KEY (facility_id) REFERENCES facility(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_facility_review_athlete
        FOREIGN KEY (athlete_id) REFERENCES app_user(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_facility_review_reaction
        CHECK (reaction IN ('LIKE', 'DISLIKE')),
    CONSTRAINT chk_facility_review_comment_not_blank
        CHECK (CHAR_LENGTH(TRIM(comment_text)) > 0)
);

CREATE INDEX idx_review_facility_created
    ON facility_review(facility_id, created_at);

CREATE INDEX idx_review_athlete_facility
    ON facility_review(athlete_id, facility_id);

CREATE INDEX idx_review_facility_reaction
    ON facility_review(facility_id, reaction);
