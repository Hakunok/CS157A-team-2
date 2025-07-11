/*
 This doesn't drop the existing schema, you must do that beforehand if you choose to do so.
 Or just use a different database name.
 */
CREATE DATABASE IF NOT EXISTS airchive;
USE airchive;

/*
 Represents a user account on the platform
 This table contains users of all roles: reader, author, admin
 */
CREATE TABLE IF NOT EXISTS user (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(40) NOT NULL,
    last_name VARCHAR(40) NOT NULL,
    email VARCHAR(75) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('reader', 'author', 'admin') NOT NULL DEFAULT 'reader',
    status ENUM('active', 'suspended', 'deleted') NOT NULL DEFAULT 'active',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

/*
 Represents an author profile on the platform
 An author entity can have an optional relationship (link) with a user entity whose role is "author"
 */
CREATE TABLE IF NOT EXISTS author (
    author_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT UNIQUE,
    first_name VARCHAR(40) NOT NULL,
    last_name VARCHAR(40) NOT NULL,
    bio TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_verified BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE SET NULL
);

/*
 Represents a publication on the platform (paper, blog, article)
 */
CREATE TABLE IF NOT EXISTS publication (
    pub_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    abstract TEXT,
    content TEXT,
    doi VARCHAR(100),
    url VARCHAR(2083),
    type ENUM('paper', 'blog', 'article'),
    submitter_id INT,
    published_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    status ENUM('published', 'unpublished', 'flagged', 'removed_by_admin') DEFAULT 'published',
    FOREIGN KEY (submitter_id) REFERENCES user(user_id) ON DELETE SET NULL
);

/*
 Represents a topic on the platform (i.e., GenAI, Computer Vision, etc.)
 */
CREATE TABLE IF NOT EXISTS topic (
    topic_id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL UNIQUE
);

/*
 Represents a user's (reader or author) reading list on the platform
 */
CREATE TABLE IF NOT EXISTS reading_list (
    list_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(160),
    is_public BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);

/*
 Represents a user's (reader) request to become an author
 */
CREATE TABLE IF NOT EXISTS author_request (
    request_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
    requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at DATETIME,
    rejected_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);

/*
 Represents a many-to-many relationship between publication and author.
 A publication entity can have 1 or more authors.
 An author entity can have 1 or more publications.
 */
CREATE TABLE IF NOT EXISTS publication_author (
    pub_id INT,
    author_id INT,
    author_order INT DEFAULT NULL,
    PRIMARY KEY (pub_id, author_id),
    FOREIGN KEY (pub_id) REFERENCES publication(pub_id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES author(author_id) ON DELETE CASCADE
);

/*
 Represents a many-to-many relationship between a publication and topic.
 A publication entity can have 1 or more topics (i.e., Computer Vision & Biotech)
 A topic entity can be related to 1 more publications.
 */
CREATE TABLE IF NOT EXISTS publication_topic (
    pub_id INT,
    topic_id INT,
    PRIMARY KEY (pub_id, topic_id),
    FOREIGN KEY (pub_id) REFERENCES publication(pub_id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES topic(topic_id) ON DELETE CASCADE
);

/*
 Represents a many-to-many relationship between a user and a topic.
 A user can interact with 1 or more topics.
 A topic can be interacted with by 1 or more users.
 */
CREATE TABLE IF NOT EXISTS topic_interaction (
    interaction_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    topic_id INT,
    interaction ENUM('view', 'like', 'save', 'interest'),
    interacted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES topic(topic_id) ON DELETE CASCADE
);

/*
 Represents a many-to-many relationship between a reading list and a publication.
 A reading list entity can contain 1 or more publications.
 A publication can be included within 1 or more reading lists.
 */
CREATE TABLE IF NOT EXISTS reading_list_item (
    list_id INT,
    pub_id INT,
    item_order INT DEFAULT NULL,
    added_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (list_id, pub_id),
    FOREIGN KEY (list_id) REFERENCES reading_list(list_id) ON DELETE CASCADE,
    FOREIGN KEY (pub_id) REFERENCES publication(pub_id) ON DELETE CASCADE
);

/*
 Represents a many-to-many relationship between a user and a publication.
 A user entity can like 1 or more publications.
 A publication entity can be liked by 1 or more users.
 */
CREATE TABLE IF NOT EXISTS likes (
    user_id INT,
    pub_id INT,
    liked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, pub_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (pub_id) REFERENCES publication(pub_id) ON DELETE CASCADE
);

DELIMITER $$

CREATE TRIGGER IF NOT EXISTS before_publication_update
    BEFORE UPDATE ON publication
    FOR EACH ROW
BEGIN
    IF NOT (
        OLD.title <=> NEW.title AND
        OLD.abstract <=> NEW.abstract AND
        OLD.content <=> NEW.content AND
        OLD.doi <=> NEW.doi AND
        OLD.url <=> NEW.url
    ) THEN
        SET NEW.updated_at = CURRENT_TIMESTAMP;
    ELSE
        SET NEW.updated_at = OLD.updated_at;
END IF;
END$$

DELIMITER ;