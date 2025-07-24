
CREATE DATABASE IF NOT EXISTS airchive_v3;
USE airchive_v3;


CREATE TABLE IF NOT EXISTS user (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(20) UNIQUE,
    first_name VARCHAR(40) NOT NULL,
    last_name VARCHAR(40) NOT NULL,
    email VARCHAR(75) NOT NULL UNIQUE,
    password_hash VARCHAR(255) DEFAULT NULL,
    permission ENUM('READER', 'AUTHOR') NOT NULL DEFAULT 'READER',
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    is_placeholder BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS publication (
    pub_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    abstract TEXT,
    content TEXT,
    doi VARCHAR(100),
    url VARCHAR(2083),
    kind ENUM('PAPER', 'BLOG', 'ARTICLE'),
    submitter_id INT,
    corresponding_author_id INT,
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PUBLISHED', 'DRAFT') DEFAULT 'DRAFT',
    FOREIGN KEY (submitter_id) REFERENCES user(user_id) ON DELETE SET NULL,
    FOREIGN KEY (corresponding_author_id) REFERENCES user(user_id) ON DELETE SET NULL
);


CREATE TABLE IF NOT EXISTS topic (
    topic_id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(10) NOT NULL UNIQUE,
    full_name VARCHAR(50) NOT NULL UNIQUE,
);


CREATE TABLE IF NOT EXISTS reading_list (
    list_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS author_request (
    user_id INT PRIMARY KEY,
    status ENUM('PENDING', 'APPROVED') DEFAULT 'PENDING',
    requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    decided_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS publication_author (
    pub_id INT NOT NULL,
    user_id INT NOT NULL,
    author_order INT NOT NULL,
    PRIMARY KEY (pub_id, user_id),
    FOREIGN KEY (pub_id) REFERENCES publication(pub_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS publication_topic (
    pub_id INT NOT NULL,
    topic_id INT NOT NULL,
    PRIMARY KEY (pub_id, topic_id),
    FOREIGN KEY (pub_id) REFERENCES publication(pub_id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES topic(topic_id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS topic_interaction (
    interaction_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    topic_id INT NOT NULL,
    kind ENUM('VIEW', 'LIKE', 'SAVE') NOT NULL,
    interacted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES topic(topic_id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS reading_list_item (
    list_id INT NOT NULL,
    pub_id INT NOT NULL,
    added_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (list_id, pub_id),
    FOREIGN KEY (list_id) REFERENCES reading_list(list_id) ON DELETE CASCADE,
    FOREIGN KEY (pub_id) REFERENCES publication(pub_id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS publication_interaction (
    interaction_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    pub_id INT NOT NULL,
    kind ENUM('VIEW', 'LIKE', 'SAVE') NOT NULL,
    interacted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, pub_id, kind),
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (pub_id) REFERENCES publication(pub_id) ON DELETE CASCADE
);

CREATE INDEX idx_user_username ON user(username);

CREATE INDEX idx_pub_status ON publication(status);
CREATE INDEX idx_pub_published ON publication(published_at DESC);

CREATE INDEX idx_reading_list_user ON reading_list(user_id);
CREATE INDEX idx_reading_list_added ON reading_list_item(list_id, added_at);

CREATE INDEX idx_pub_interaction ON publication_interaction(user_id, kind);
CREATE INDEX idx_topic_interaction ON topic_interaction(user_id, kind);

CREATE INDEX idx_topic_code ON topic(code);