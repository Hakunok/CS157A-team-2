CREATE DATABASE IF NOT EXISTS airchive;
USE airchive;

CREATE TABLE User (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(40) NOT NULL,
    last_name VARCHAR(40) NOT NULL,
    email VARCHAR(75) NOT NULL UNIQUE,
    password_hash VARCHAR(60) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Author (
    author_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(40) NOT NULL,
    last_name VARCHAR(40) NOT NULL,
    bio VARCHAR(160),
    is_verified BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id INT UNIQUE,
    FOREIGN KEY (user_id) REFERENCES User(user_id)
);

CREATE TABLE Administrator (
    admin_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(75) NOT NULL UNIQUE,
    password_hash VARCHAR(60) NOT NULL
);

CREATE TABLE Publication (
    pub_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    abstract TEXT,
    content TEXT,
    doi VARCHAR(100),
    url VARCHAR(2083),
    type ENUM('research', 'blog', 'article'),
    submitter_id INT NOT NULL,
    published_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    status ENUM('published', 'unpublished', 'flagged') DEFAULT 'published',
    FOREIGN KEY (submitter_id) REFERENCES User(user_id)
);

CREATE TABLE Topic (
    topic_id INT PRIMARY KEY AUTO_INCREMENT,
    topic_code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE ReadingList (
    list_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    list_name VARCHAR(100) NOT NULL,
    description VARCHAR(160),
    is_public BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(user_id)
);

CREATE TABLE AuthorRequest (
    request_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
    requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(user_id)
);

CREATE TABLE PublicationAuthor (
    pub_id INT,
    author_id INT,
    author_order INT DEFAULT NULL,
    PRIMARY KEY (pub_id, author_id),
    FOREIGN KEY (pub_id) REFERENCES Publication(pub_id),
    FOREIGN KEY (author_id) REFERENCES Author(author_id)
);

CREATE TABLE PublicationTopic (
    pub_id INT,
    topic_id INT,
    PRIMARY KEY (pub_id, topic_id),
    FOREIGN KEY (pub_id) REFERENCES Publication(pub_id),
    FOREIGN KEY (topic_id) REFERENCES Topic(topic_id)
);

CREATE TABLE TopicInteraction (
    interaction_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    topic_id INT,
    interaction_type ENUM('viewed', 'liked', 'saved', 'declared_interest'),
    interaction_weight INT,
    interacted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(user_id),
    FOREIGN KEY (topic_id) REFERENCES Topic(topic_id)
);

CREATE TABLE ReadingListItem (
    list_id INT,
    pub_id INT,
    item_order INT DEFAULT NULL,
    added_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (list_id, pub_id),
    FOREIGN KEY (list_id) REFERENCES ReadingList(list_id),
    FOREIGN KEY (pub_id) REFERENCES Publication(pub_id)
);

CREATE TABLE Likes (
    user_id INT,
    pub_id INT,
    liked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, pub_id),
    FOREIGN KEY (user_id) REFERENCES User(user_id),
    FOREIGN KEY (pub_id) REFERENCES Publication(pub_id)
);