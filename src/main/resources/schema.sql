CREATE TABLE IF NOT EXISTS person (
    person_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    birth_date DATE,
    role VARCHAR(20) NOT NULL DEFAULT 'USER'
);

CREATE TABLE IF NOT EXISTS bootcamp_person (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    person_id BIGINT NOT NULL,
    bootcamp_id BIGINT NOT NULL,
    enrolled_at DATETIME NOT NULL,
    bootcamp_start_date DATE NOT NULL,
    bootcamp_duration_days INT NOT NULL,
    UNIQUE(person_id, bootcamp_id)
);
