CREATE DATABASE IF NOT EXISTS airchive;
USE airchive;

-- =============================================================================
-- TABLE: person
-- Stores unique information about an individual on the platform. This table
-- decouples the concept of a person from a user account, allowing authors to
-- be credited without having an account on the system.
-- The identity_email attribute is a unique email used to identify the person,
-- this is set on account creation or when an author credits another non-platform
-- author.
-- =============================================================================
CREATE TABLE person (
    person_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(40) NOT NULL,
    last_name VARCHAR(40) NOT NULL,
    identity_email VARCHAR(75) NOT NULL UNIQUE
);

-- =============================================================================
-- TABLE: account
-- Manages user accounts for the platform. Each account corresponds to a person
-- and stores the account's authentication, role, and permission metadata.
-- =============================================================================
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

-- =============================================================================
-- TABLE: author_request
-- Tracks requests from users with the 'READER' role to be upgraded to the
-- 'AUTHOR' role.
-- =============================================================================
CREATE TABLE author_request (
    account_id INT PRIMARY KEY,
    status ENUM('PENDING', 'APPROVED') DEFAULT 'PENDING',
    requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE
);

-- =============================================================================
-- TABLE: topic
-- A lookup table for categorizing publications based on a topic. This allows
-- for filtering and personalized content recommendation functionalities.
-- =============================================================================
CREATE TABLE topic (
    topic_id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(10) NOT NULL UNIQUE,
    full_name VARCHAR(50) NOT NULL UNIQUE
);

-- =============================================================================
-- TABLE: publication
-- The core table holding metadata for all publications, such as papers,
-- articles, and blog posts.
-- =============================================================================
CREATE TABLE publication (
    pub_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    content TEXT,
    doi VARCHAR(100) UNIQUE,
    url VARCHAR(2083),
    kind ENUM('PAPER', 'BLOG', 'ARTICLE') NOT NULL,
    submitter_id INT,
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    published_at DATETIME,
    status ENUM('PUBLISHED', 'DRAFT') NOT NULL DEFAULT 'DRAFT',
    FOREIGN KEY (submitter_id) REFERENCES account(account_id) ON DELETE SET NULL,
);

-- =============================================================================
-- TABLE: publication_author
-- A many-to-many relationship between publications and their authors (persons)
-- and also stores their order of authorship.
-- =============================================================================
CREATE TABLE publication_author (
    pub_id INT NOT NULL,
    person_id INT NOT NULL,
    author_order INT NOT NULL,
    PRIMARY KEY (pub_id, person_id),
    FOREIGN KEY (pub_id) REFERENCES publication(pub_id) ON DELETE RESTRICT,
    FOREIGN KEY (person_id) REFERENCES person(person_id) ON DELETE RESTRICT
);

-- =============================================================================
-- TABLE: publication_topic
-- A many-to-many relationship between publications and topics. This table is
-- used for filtering publications based on topics and providing personalized
-- content recommendations.
-- =============================================================================
CREATE TABLE publication_topic (
    pub_id INT NOT NULL,
    topic_id INT NOT NULL,
    PRIMARY KEY (pub_id, topic_id),
    FOREIGN KEY (pub_id) REFERENCES publication(pub_id) ON DELETE RESTRICT,
    FOREIGN KEY (topic_id) REFERENCES topic(topic_id) ON DELETE CASCADE
);

-- =============================================================================
-- TABLE: collection
-- Stores an account's collections (reading lists) and their metadata. Each
-- account has 1 default collection.
-- =============================================================================
CREATE TABLE collection (
    collection_id INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE
);

-- =============================================================================
-- TABLE: collection_item
-- A many-to-many relationship between collections and publications. This table
-- stores the publications saved within an account's collection.
-- =============================================================================
CREATE TABLE collection_item (
    collection_id INT NOT NULL,
    pub_id INT NOT NULL,
    added_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (collection_id, pub_id),
    FOREIGN KEY (collection_id) REFERENCES collection(collection_id) ON DELETE CASCADE,
    FOREIGN KEY (pub_id) REFERENCES publication(pub_id) ON DELETE RESTRICT
);

-- =============================================================================
-- TABLE: publication_view
-- Tracks all instances of an account viewing a publication. This table allows
-- for personalized content recommendations.
-- =============================================================================
CREATE TABLE publication_view (
    account_id INT NOT NULL,
    pub_id INT NOT NULL,
    viewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id, pub_id, viewed_at),
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE,
    FOREIGN KEY (pub_id) REFERENCES publication(pub_id) ON DELETE RESTRICT
);

-- =============================================================================
-- TABLE: publication_like
-- Tracks an account's "like" interaction on a publication. This table allows
-- for personalized content recommendations.
-- =============================================================================
CREATE TABLE publication_like (
    account_id INT NOT NULL,
    pub_id INT NOT NULL,
    liked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id, pub_id),
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE,
    FOREIGN KEY (pub_id) REFERENCES publication(pub_id) ON DELETE RESTRICT
);

-- =============================================================================
-- TABLE: topic_affinity
-- Stores a calculated score representing an account's affinity for a specific
-- topic. This score is calculated from an account's interactions (views,
-- likes, saves). These scores are used to provide personalized content
-- recommendations.
-- =============================================================================
CREATE TABLE topic_affinity (
    account_id INT NOT NULL,
    topic_id INT NOT NULL,
    score DOUBLE NOT NULL DEFAULT 0,
    last_updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id, topic_id),
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES topic(topic_id) ON DELETE CASCADE
);

-- =============================================================================
-- TABLE: author_affinity
-- Stores a calculated score representing an account's affinity for a specific
-- author. This score is calculated from an account's interactions (views,
-- likes, saves). These scores are used to provide personalized content
-- recommendations.
-- =============================================================================
CREATE TABLE author_affinity (
    account_id INT NOT NULL,
    author_id INT NOT NULL,
    score DOUBLE NOT NULL DEFAULT 0,
    last_updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id, author_id),
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES person(person_id) ON DELETE CASCADE
);

-- FULLTEXT indices for our MATCH ... AGAINST ... searches
CREATE FULLTEXT INDEX ft_pub_title ON publication(title);
CREATE FULLTEXT INDEX ft_topic_fullname ON topic(full_name);

CREATE INDEX idx_publication_published ON publication (published_at);
CREATE INDEX idx_publication_status_kind ON publication (status, kind);

CREATE INDEX idx_view_pub ON publication_view (pub_id);
CREATE INDEX idx_like_pub ON publication_like (pub_id);

CREATE INDEX idx_collection_pub ON collection_item (pub_id);

CREATE INDEX idx_pub_author ON publication_author (person_id);
CREATE INDEX idx_pub_topic ON publication_topic (topic_id);