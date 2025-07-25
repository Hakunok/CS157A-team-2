CREATE DATABASE IF NOT EXISTS airchive;
USE airchive;

-- Unique record for every individual in the system, whether they have an account or not
CREATE TABLE person (
    person_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(40) NOT NULL,
    last_name VARCHAR(40) NOT NULL,
    contact_email VARCHAR(75) NOT NULL UNIQUE
);

-- Stores the authentication and permission details for platform users
CREATE TABLE account (
    account_id INT PRIMARY KEY AUTO_INCREMENT,
    person_id INT NOT NULL UNIQUE,
    email VARCHAR(75) NOT NULL UNIQUE,
    username VARCHAR(20) NOT NULL UNIQUE,
    password_hash VARCHAR(255) DEFAULT NULL,
    role ENUM('READER', 'AUTHOR') NOT NULL DEFAULT 'READER',
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (person_id) REFERENCES person(person_id) ON DELETE CASCADE
);

-- Tracks requests for 'READER' accounts to gain 'AUTHOR' privileges
CREATE TABLE author_request (
    account_id INT PRIMARY KEY,
    status ENUM('PENDING', 'APPROVED') DEFAULT 'PENDING',
    requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE
);

-- Stores the classification topics which are assigned to publications
CREATE TABLE topic (
    topic_id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(10) NOT NULL UNIQUE,
    full_name VARCHAR(50) NOT NULL UNIQUE
);

-- Stores all published works on the platform
CREATE TABLE publication (
    pub_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    content TEXT,
    doi VARCHAR(100) UNIQUE,
    url VARCHAR(2083),
    kind ENUM('PAPER', 'BLOG', 'ARTICLE') NOT NULL,
    submitter_id INT,
    corresponding_author_id INT,
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at DATETIME,
    status ENUM('PUBLISHED', 'DRAFT') NOT NULL DEFAULT 'DRAFT',
    FOREIGN KEY (submitter_id) REFERENCES account(account_id) ON DELETE SET NULL,
    FOREIGN KEY (corresponding_author_id) REFERENCES person(person_id) ON DELETE SET NULL
);

-- Relates publications to their authors (person)
CREATE TABLE publication_author (
    pub_id INT NOT NULL,
    person_id INT NOT NULL,
    author_order INT NOT NULL,
    PRIMARY KEY (pub_id, person_id),
    FOREIGN KEY (pub_id) REFERENCES publication(pub_id) ON DELETE CASCADE,
    FOREIGN KEY (person_id) REFERENCES person(person_id) ON DELETE RESTRICT
);

-- Relates one ore more topics to a publication
CREATE TABLE publication_topic (
    pub_id INT NOT NULL,
    topic_id INT NOT NULL,
    PRIMARY KEY (pub_id, topic_id),
    FOREIGN KEY (pub_id) REFERENCES publication(pub_id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES topic(topic_id) ON DELETE CASCADE
);

-- Stores an account's personal collections of publications
CREATE TABLE collection (
    collection_id INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE
);

-- Relates publications to a collection
CREATE TABLE collection_item (
    collection_id INT NOT NULL,
    pub_id INT NOT NULL,
    added_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (collection_id, pub_id),
    FOREIGN KEY (collection_id) REFERENCES collection(collection_id) ON DELETE CASCADE,
    FOREIGN KEY (pub_id) REFERENCES publication(pub_id) ON DELETE CASCADE
);

-- Stores an account's interactions with a publication
CREATE TABLE publication_interaction (
    interaction_id INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL,
    pub_id INT NOT NULL,
    kind ENUM('VIEW', 'LIKE', 'SAVE') NOT NULL,
    interacted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (account_id, pub_id, kind),
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE,
    FOREIGN KEY (pub_id) REFERENCES publication(pub_id) ON DELETE CASCADE
);

-- Stores an account's affinity towards a topic
-- Used for account-based recommendations
CREATE TABLE topic_affinity (
    account_id INT NOT NULL,
    topic_id INT NOT NULL,
    score INT NOT NULL DEFAULT 0,
    last_updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id, topic_id),
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES topic(topic_id) ON DELETE CASCADE
);