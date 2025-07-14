USE airchive;

INSERT INTO User (username, first_name, last_name, email, password_hash) VALUES
('bobeatsbees', 'Bob', 'Lee', 'bob.eatsbees@gmail.com', 'hashed_password_placeholder'),
('meliodassds', 'Meliodas', 'Haha', 'meliodashaha@gmail.com', 'hashed_password_placeholder'),
('drake_boylover', 'Drake', 'Boylover', 'drakeboylover@gmail.com', 'hashed_password_placeholder'),
('bleachfan2001', 'John', 'Banff', 'bleachfan2001@gmail.com', 'hashed_password_placeholder'),
('getsugotenshou2', 'Hahaha', 'Ha', 'bleachfan2002@gmail.com', 'hashed_password_placeholder'),
('spongebob', 'Sponge', 'Bob', 'spongebob@gmail.com', 'hashed_password_placeholder'),
('patrick', 'Patrick', 'Bob', 'patricksquarepants@gmail.com', 'hashed_password_placeholder'),
('dabourbonman', 'Aekn', 'Admal', 'pikachu@gmail.com', 'hashed_password_placeholder'),
('sqllover2005', 'Sql', 'Lover', 'sqllover@gmail.com', 'hashed_password_placeholder'),
('ilikeuha', 'Gon', 'Freecss', 'gonfreecss@gmail.com', 'hashed_password_placeholder');

INSERT INTO Administrator (username, email, password_hash) VALUES
('admin_luke', 'luke@airchive.admin', 'hash_pass'),
('admin_bob', 'bob@airchive.admin', 'hash_pass'),
('admin_darth', 'darth@airchive.admin', 'hash_pass'),
('admin_monkey', 'monkey@airchive.admin', 'hash_pass'),
('admin_dog', 'dog@airchive.admin', 'hash_pass'),
('admin_c3po', 'c3po@airchive.admin', 'hash_pass'),
('dabourbonman', 'dabourbonman@airchive.admin', 'hash_pass'),
('adminman', 'adminman@airchive.admin', 'hash_pass'),
('supervisor1', 'supervisor1@airchive.admin', 'hash_pass'),
('supervisor2', 'supervisor2@airchive.admin', 'hash_pass');

INSERT INTO Topic (topic_code, name) VALUES
('ai.ET', 'AI Ethics'),
('ai.QC', 'Quantum Computing'),
('ai.CV', 'Computer Vision'),
('ai.NLP', 'Natural Language Processing'),
('ai.NS', 'Neuroscience'),
('ai.CBS', 'Cybersecurity'),
('ai.BCT', 'Blockchain Technology'),
('ai.AP', 'Astrophysics'),
('ai.BT', 'Biotech'),
('ai.GAI', 'Generative AI');

INSERT INTO Author (first_name, last_name, bio, is_verified, user_id) VALUES
('Gon', 'Freecss', 'Son of Ging, rookie AI researcher', TRUE, 11),
('Ibrahim', 'Rafik', 'Stupid man of science', TRUE, NULL),
('Drake', 'Boylover', 'Music guy of science', TRUE, 4),
('Alan', 'Turing', 'Famous guy', TRUE, NULL),
('Isaac', 'Newton', 'Smart guy', TRUE, NULL),
('Robert', 'Sedgewick', 'smart professor', TRUE, NULL),
('Ada', 'Lovelace', 'smart mathematician', TRUE, NULL),
('This', 'Dude', 'random dude', FALSE, NULL),
('Another', 'Dude', 'random dude2', FALSE, NULL),
('And', 'anotherDude', 'random dude 3', FALSE, NULL),
('System', 'User', 'This is the aiRchive system publisher', TRUE, 1);

INSERT INTO Publication (title, abstract, content, doi, url, type, submitter_id, userStatus) VALUES
('The Alignment Problem in Advanced AI', 'An exploration of the challenges in ensuring AI systems pursue goals aligned with human values.', 'Full text content here...', '10.1000/182', 'https://example.com/pub/ai_alignment', 'research', 1, 'published'),
('A Primer on Shor\'s Algorithm', 'Simplifying the complex principles behind quantum factorization.', 'Full text content here...', '10.1001/qcomp.2024', 'https://example.com/pub/shors_algo', 'article', 4, 'published'),
('The Hunter\'s Method for AI Optimization', 'A novel approach to training generative models inspired by hunter techniques.', 'Full text content discussing reinforcement learning and search algorithms...', '10.2025/ai.hm.01', NULL, 'research', 11, 'published'),
('Top Lines and Bottom Lines: NLP in Modern Music', 'How Natural Language Processing is changing the way we write and produce hit records.', 'Blog content with examples from recent music history...', NULL, 'https://example.com/blog/nlp_music', 'blog', 4, 'unpublished'),
('On Computable Patterns: A Theory of Machine Vision', 'This paper lays the theoretical groundwork for machines that can perceive and interpret visual information.', 'Full text content detailing the mathematical basis for pattern recognition...', '10.1950/turing.cv.01', NULL, 'research', 1, 'published'),
('Principia Astro-mechanica', 'A modern interpretation of the laws governing celestial bodies and their motions.', 'Full text content exploring gravitational physics...', '10.1687/newton.ap.01', NULL, 'article', 1, 'published'),
('Analyzing Sedgewick’s Approach to Cryptographic Hashing', 'A review of the algorithmic efficiencies in modern cybersecurity protocols.', 'Article content breaking down complex hashing functions...', NULL, 'https://example.com/articles/sedgewick_crypto', 'article', 11, 'published'),
('CRISPR-Cas9: A New Frontier in Gene Editing', 'The transformative potential and ethical considerations of CRISPR technology.', 'Full text content here...', '10.1006/biotech.crispr', 'https://example.com/pub/crispr', 'research', 1, 'published'), -- The missing 'type' value is added here
('Detecting Exoplanet Atmospheres via Spectroscopy', 'Techniques for analyzing light from distant stars to determine the composition of exoplanet atmospheres.', 'Full text content here...', '10.1004/astro.spectro', 'https://example.com/pub/exoplanets', 'research', 11, 'published'),
('Optogenetics in Memory Engram Research', 'Using light to manipulate neurons and uncover the physical basis of memory.', 'Full text content here...', '10.1003/neuro.mem', 'https://example.com/pub/optogenetics', 'research', 4, 'published');

INSERT INTO ReadingList (user_id, list_name, description, is_public) VALUES
(1, 'AI Ethics & Safety', 'Essential reading on the challenges of creating safe AI.', TRUE),
(2, 'Intro to Quantum Computing', 'My collection of foundational papers on QC.', FALSE),
(3, 'NLP in Music Production', 'Articles on generative music and lyrical analysis.', FALSE),
(4, 'Cybersecurity Weekly', 'The latest articles and whitepapers on cybersecurity threats.', TRUE),
(5, 'Neuroscience & AI', 'Papers exploring the intersection of brain science and artificial intelligence.', FALSE),
(6, 'Blockchain Deep Dive', 'From fundamentals to advanced DeFi concepts.', FALSE),
(7, 'Astrophysics Discoveries', 'Recent breakthroughs in our understanding of the cosmos.', FALSE),
(8, 'Biotech Innovations', 'The future of medicine and genetic engineering.', FALSE),
(9, 'Advanced SQL Techniques', 'Personal reference for complex queries and database architecture.', FALSE),
(11, 'Generative AI Showcase', 'Coolest examples of generative art and text models.', TRUE);

INSERT INTO AuthorRequest (user_id, userStatus) VALUES
(11, 'approved'),
(4, 'approved'),
(2, 'pending'),
(3, 'pending'),
(4, 'rejected'),
(6, 'pending'),
(7, 'pending'),
(1, 'approved'),
(5, 'pending'),
(4, 'rejected');

INSERT INTO PublicationAuthor (pub_id, author_id, author_order) VALUES
(2, 6, 1),
(3, 1, 1),
(4, 3, 1),
(5, 4, 1),
(6, 5, 1),
(10, 2, 1),
(1, 4, 1),
(1, 7, 2),
(8, 2, 1),
(8, 11, 2);

INSERT INTO PublicationTopic (pub_id, topic_id) VALUES
(1, 1),
(1, 10),
(2, 2),
(3, 10),
(4, 4),
(5, 3),
(6, 8),
(7, 6),
(8, 9),
(9, 8);

INSERT INTO TopicInteraction (user_id, topic_id, interaction_type, interaction_weight) VALUES
(10, 10, 'declared_interest', 10),
(3, 4, 'liked', 5),
(9, 6, 'saved', 8),
(1, 1, 'viewed', 1),
(5, 5, 'declared_interest', 10),
(6, 9, 'liked', 5),
(7, 8, 'saved', 8),
(8, 7, 'declared_interest', 10),
(4, 3, 'viewed', 1),
(10, 1, 'liked', 5);

INSERT INTO ReadingListItem(list_id, pub_id, item_order) VALUES
(1, 1, 1),
(1, 3, 2),
(1, 5, 3),
(2, 2, 1),
(3, 4, 1),
(7, 7, 1),
(5, 10, 1),
(5, 1, 2),
(7, 6, 1),
(7, 9, 2);

INSERT INTO Likes (user_id, pub_id) VALUES
(2, 1),
(10, 1),
(5, 1),
(2, 2),
(3, 4),
(4, 5),
(6, 6),
(7, 9),
(8, 8),
(9, 7);