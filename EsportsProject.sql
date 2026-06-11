






-- TABLES -- KEVIN

-- VENUE: stores location info where tournaments are held
CREATE TABLE venue (
    venue_id   NUMBER(5) NOT NULL,
    city       VARCHAR2(30) NOT NULL,
    country    VARCHAR2(30) NOT NULL,
    venue_name VARCHAR2(50)
);

-- TABLE FOR login
CREATE TABLE LOGGEDUSERS(
    username varchar2(40) primary key,
    name varchar2(30),
    password varchar2(30)
)



ALTER TABLE venue ADD CONSTRAINT venue_pk PRIMARY KEY ( venue_id );


-- COACH: each team has exactly one coach (enforced by unique index on team_id)
CREATE TABLE coach (
    coach_id NUMBER(5) NOT NULL,
    name     VARCHAR2(50) NOT NULL,
    salary   NUMBER(8, 2),
    team_id  NUMBER(5) NOT NULL
);
ALTER TABLE coach ADD CONSTRAINT coach_sal_chk CHECK (salary >= 0);
ALTER TABLE coach ADD CONSTRAINT coach_pk PRIMARY KEY ( coach_id );
CREATE UNIQUE INDEX coach__idx ON coach ( team_id ASC );


-- CONTRACT: M:N between player and team
CREATE TABLE contract (
    player_id NUMBER(4) NOT NULL,
    team_id   NUMBER(5) NOT NULL
);
ALTER TABLE contract ADD CONSTRAINT contract_pk PRIMARY KEY ( player_id, team_id );


-- GAME: stores game titles e.g. Valorant, CS2
CREATE TABLE game (
    game_id   NUMBER(4) NOT NULL,
    game_name VARCHAR2(30) NOT NULL,
    genre     VARCHAR2(25)
);
ALTER TABLE game ADD CONSTRAINT game_pk PRIMARY KEY ( game_id );


-- MATCH: each match belongs to a tournament and is played on a specific game
CREATE TABLE match (
    match_id      NUMBER(5) NOT NULL,
    match_date    DATE,
    map_name      VARCHAR2(50),
    tournament_id NUMBER(5) NOT NULL,
    game_id       NUMBER(4) NOT NULL
);
ALTER TABLE match ADD CONSTRAINT match_pk PRIMARY KEY ( match_id );


-- PERFORMS: player stats per match (kills/deaths/assists)
CREATE TABLE performs (
    player_id NUMBER(4) NOT NULL,
    match_id  NUMBER(5) NOT NULL,
    assists   INTEGER,
    deaths    INTEGER,
    kills     INTEGER
);
ALTER TABLE performs ADD CONSTRAINT performs_assists_chk CHECK (assists >= 0);
ALTER TABLE performs ADD CONSTRAINT performs_deaths_chk CHECK (deaths >= 0);
ALTER TABLE performs ADD CONSTRAINT performs_kills_chk CHECK (kills >= 0);
ALTER TABLE performs ADD CONSTRAINT performs_pk PRIMARY KEY ( player_id, match_id );


-- PLAYER: stores player personal and competitive info
CREATE TABLE player (
    player_id   NUMBER(4) NOT NULL,
    first_name  VARCHAR2(25) NOT NULL,
    last_name   VARCHAR2(25),
    salary      NUMBER(8, 2),
    nationality VARCHAR2(25) NOT NULL,
    role        VARCHAR2(25),
    bdate       DATE NOT NULL
);
ALTER TABLE player ADD CONSTRAINT player_pk PRIMARY KEY ( player_id );
ALTER TABLE player ADD CONSTRAINT player_sal_chk CHECK (salary >= 0);


-- PLAYER_LANGUAGES: multi-valued attribute, a player can speak multiple languages
CREATE TABLE player_languages (
    language  VARCHAR2(30) NOT NULL,
    player_id NUMBER(4) NOT NULL
);
ALTER TABLE player_languages ADD CONSTRAINT player_languages_pk PRIMARY KEY ( language, player_id );


-- PLAYS_IN: M:N which teams play in which match
CREATE TABLE plays_in (
    team_id  NUMBER(5) NOT NULL,
    match_id NUMBER(5) NOT NULL
);
ALTER TABLE plays_in ADD CONSTRAINT plays_in_pk PRIMARY KEY ( team_id, match_id );


-- REGISTERS: M:N team registers for a tournament
CREATE TABLE registers (
    team_id       NUMBER(5) NOT NULL,
    tournament_id NUMBER(5) NOT NULL
);
ALTER TABLE registers ADD CONSTRAINT registers_pk PRIMARY KEY ( team_id, tournament_id );


-- ROUND: rounds within a match, winner stores the winning team_id
CREATE TABLE round (
    round_no NUMBER(2) NOT NULL,
    duration NUMBER(3),
    winner   NUMBER(5),
    match_id NUMBER(5) NOT NULL
);
ALTER TABLE round ADD CONSTRAINT round_pk PRIMARY KEY ( round_no, match_id );
ALTER TABLE round ADD CONSTRAINT round_no_chk CHECK (round_no > 0);
ALTER TABLE round ADD CONSTRAINT round_winner_chk CHECK (winner >= 0);


-- SPONSOR: companies that sponsor teams in tournaments
CREATE TABLE sponsor (
    sponsor_id NUMBER(5) NOT NULL,
    name       VARCHAR2(30) NOT NULL
);
ALTER TABLE sponsor ADD CONSTRAINT sponsor_pk PRIMARY KEY ( sponsor_id );


-- SPONSOR_CONTACT: multi-valued, a sponsor can have multiple contact numbers
CREATE TABLE sponsor_contact (
    contact_no VARCHAR2(20) NOT NULL,
    sponsor_id NUMBER(5) NOT NULL
);
ALTER TABLE sponsor_contact ADD CONSTRAINT sponsor_contact_pk PRIMARY KEY ( contact_no, sponsor_id );


-- SPONSORSHIP: ternary relationship, sponsor sponsors a team in a specific tournament
CREATE TABLE sponsorship (
    tournament_id NUMBER(5) NOT NULL,
    team_id       NUMBER(5) NOT NULL,
    sponsor_id    NUMBER(5) NOT NULL
);
ALTER TABLE sponsorship ADD CONSTRAINT sponsorship_pk PRIMARY KEY ( tournament_id, team_id, sponsor_id );



-- TEAM: stores team names
CREATE TABLE team (
    team_id   NUMBER(5) NOT NULL,
    team_name VARCHAR2(25) NOT NULL
);
ALTER TABLE team ADD CONSTRAINT team_pk PRIMARY KEY ( team_id );


-- TOURNAMENT: stores tournament details, linked to a venue
CREATE TABLE tournament (
    tournament_id NUMBER(5) NOT NULL,
    name          VARCHAR2(35) NOT NULL,
    start_date    DATE,
    end_date      DATE,
    prize_pool    NUMBER(10, 2),
    venue_id      NUMBER(5)
);
ALTER TABLE tournament ADD CONSTRAINT tournament_prize_chk CHECK (prize_pool >= 0);
ALTER TABLE tournament ADD CONSTRAINT tournament_date_chk CHECK (start_date <= end_date);
ALTER TABLE tournament ADD CONSTRAINT tournament_pk PRIMARY KEY ( tournament_id );


-- FOREIGN KEYS

ALTER TABLE coach ADD CONSTRAINT coach_team_fk FOREIGN KEY (team_id) REFERENCES team (team_id);

ALTER TABLE contract ADD CONSTRAINT contract_player_fk FOREIGN KEY (player_id) REFERENCES player (player_id) ON DELETE CASCADE;
ALTER TABLE contract ADD CONSTRAINT contract_team_fk FOREIGN KEY (team_id) REFERENCES team (team_id) ON DELETE CASCADE;

ALTER TABLE match ADD CONSTRAINT match_game_fk FOREIGN KEY (game_id) REFERENCES game (game_id);
ALTER TABLE match ADD CONSTRAINT match_tournament_fk FOREIGN KEY (tournament_id) REFERENCES tournament (tournament_id);

ALTER TABLE performs ADD CONSTRAINT performs_match_fk FOREIGN KEY (match_id) REFERENCES match (match_id) ON DELETE CASCADE;
ALTER TABLE performs ADD CONSTRAINT performs_player_fk FOREIGN KEY (player_id) REFERENCES player (player_id) ON DELETE CASCADE;

ALTER TABLE player_languages ADD CONSTRAINT player_languages_player_fk FOREIGN KEY (player_id) REFERENCES player (player_id) ON DELETE CASCADE;

ALTER TABLE plays_in ADD CONSTRAINT plays_in_match_fk FOREIGN KEY (match_id) REFERENCES match (match_id) ON DELETE CASCADE;
ALTER TABLE plays_in ADD CONSTRAINT plays_in_team_fk FOREIGN KEY (team_id) REFERENCES team (team_id) ON DELETE CASCADE;

ALTER TABLE registers ADD CONSTRAINT registers_team_fk FOREIGN KEY (team_id) REFERENCES team (team_id) ON DELETE CASCADE;
ALTER TABLE registers ADD CONSTRAINT registers_tournament_fk FOREIGN KEY (tournament_id) REFERENCES tournament (tournament_id) ON DELETE CASCADE;

ALTER TABLE round ADD CONSTRAINT round_match_fk FOREIGN KEY (match_id) REFERENCES match (match_id) ON DELETE CASCADE;
ALTER TABLE round ADD CONSTRAINT round_winner_team_fk FOREIGN KEY (winner) REFERENCES team (team_id);

ALTER TABLE sponsor_contact ADD CONSTRAINT sponsor_contact_sponsor_fk FOREIGN KEY (sponsor_id) REFERENCES sponsor (sponsor_id) ON DELETE CASCADE;

ALTER TABLE sponsorship ADD CONSTRAINT sponsorship_sponsor_fk FOREIGN KEY (sponsor_id) REFERENCES sponsor (sponsor_id) ON DELETE CASCADE;
ALTER TABLE sponsorship ADD CONSTRAINT sponsorship_team_fk FOREIGN KEY (team_id) REFERENCES team (team_id) ON DELETE CASCADE;
ALTER TABLE sponsorship ADD CONSTRAINT sponsorship_tournament_fk FOREIGN KEY (tournament_id) REFERENCES tournament (tournament_id) ON DELETE CASCADE;

ALTER TABLE tournament ADD CONSTRAINT tournament_venue_fk FOREIGN KEY (venue_id) REFERENCES venue (venue_id);














-- INSERT DATA

-- VENUE
INSERT INTO venue VALUES (1, 'Cologne',    'Germany',   'ESL Arena');
INSERT INTO venue VALUES (2, 'Los Angeles','USA',        'Crypto.com Arena');
INSERT INTO venue VALUES (3, 'Dubai',      'UAE',        'Coca-Cola Arena');
INSERT INTO venue VALUES (4, 'Mumbai',     'India',      'Jio World Convention');
INSERT INTO venue VALUES (5, 'Karachi',    'Pakistan',   'Expo Centre Karachi');

-- GAME
INSERT INTO game VALUES (1, 'Valorant',         'Shooter');
INSERT INTO game VALUES (2, 'Counter-Strike 2', 'Shooter');
INSERT INTO game VALUES (3, 'Apex Legends',     'Battle Royale');

-- TEAM
INSERT INTO team VALUES (1,  'Team Liquid');
INSERT INTO team VALUES (2,  'Natus Vincere');
INSERT INTO team VALUES (3,  'FaZe Clan');
INSERT INTO team VALUES (4,  'Cloud9');
INSERT INTO team VALUES (5,  'Fnatic');
INSERT INTO team VALUES (6,  'Team SouL');
INSERT INTO team VALUES (7,  'GodLike Esports');
INSERT INTO team VALUES (8,  'Entity Gaming');
INSERT INTO team VALUES (9,  'Team Falcons');
INSERT INTO team VALUES (10, 'Velocity Gaming');

-- TOURNAMENT
INSERT INTO tournament VALUES (1, 'ESL Pro League S19',   TO_DATE('2025-01-10','YYYY-MM-DD'), TO_DATE('2025-01-25','YYYY-MM-DD'), 500000,  1);
INSERT INTO tournament VALUES (2, 'VCT Champions 2025',   TO_DATE('2025-03-05','YYYY-MM-DD'), TO_DATE('2025-03-20','YYYY-MM-DD'), 1000000, 2);
INSERT INTO tournament VALUES (3, 'IEM Dubai 2025',       TO_DATE('2025-05-01','YYYY-MM-DD'), TO_DATE('2025-05-15','YYYY-MM-DD'), 750000,  3);
INSERT INTO tournament VALUES (4, 'South Asia Open 2025', TO_DATE('2025-06-10','YYYY-MM-DD'), TO_DATE('2025-06-22','YYYY-MM-DD'), 300000,  4);
INSERT INTO tournament VALUES (5, 'Pakistan Esports Cup', TO_DATE('2025-08-01','YYYY-MM-DD'), TO_DATE('2025-08-12','YYYY-MM-DD'), 200000,  5);

-- COACH
INSERT INTO coach VALUES (1,  'Marcus Larsson',     85000, 1);
INSERT INTO coach VALUES (2,  'Andrei Petrescu',    90000, 2);
INSERT INTO coach VALUES (3,  'Jake Williams',      78000, 3);
INSERT INTO coach VALUES (4,  'Tyler Brooks',       82000, 4);
INSERT INTO coach VALUES (5,  'Sam Fletcher',       76000, 5);
INSERT INTO coach VALUES (6,  'Harpreet Singh',     65000, 6);
INSERT INTO coach VALUES (7,  'Rohit Sharma',       67000, 7);
INSERT INTO coach VALUES (8,  'Khalid Al Mansoori', 70000, 8);
INSERT INTO coach VALUES (9,  'Ahmed Al Rashidi',   72000, 9);
INSERT INTO coach VALUES (10, 'Usman Tariq',        63000, 10);

-- PLAYER
INSERT INTO player VALUES (1,  'Alex',      'Hunter',      75000, 'American',   'Duelist',     TO_DATE('2000-03-15','YYYY-MM-DD'));
INSERT INTO player VALUES (2,  'Jonas',     'Kraft',       80000, 'German',     'Sentinel',    TO_DATE('1999-07-22','YYYY-MM-DD'));
INSERT INTO player VALUES (3,  'Lena',      'Park',        72000, 'Korean',     'Controller',  TO_DATE('2001-01-10','YYYY-MM-DD'));
INSERT INTO player VALUES (4,  'Ivan',      'Petrov',      85000, 'Russian',    'Initiator',   TO_DATE('1998-11-05','YYYY-MM-DD'));
INSERT INTO player VALUES (5,  'Sasha',     'Morozov',     78000, 'Ukrainian',  'Duelist',     TO_DATE('2000-06-30','YYYY-MM-DD'));
INSERT INTO player VALUES (6,  'Carlos',    'Reyes',       70000, 'Brazilian',  'AWPer',       TO_DATE('2001-09-18','YYYY-MM-DD'));
INSERT INTO player VALUES (7,  'Mikkel',    'Hansen',      82000, 'Danish',     'Rifler',      TO_DATE('1999-04-12','YYYY-MM-DD'));
INSERT INTO player VALUES (8,  'Nikola',    'Kovac',       95000, 'Bosnian',    'Rifler',      TO_DATE('1997-02-16','YYYY-MM-DD'));
INSERT INTO player VALUES (9,  'Timothy',   'Chase',       68000, 'American',   'IGL',         TO_DATE('2002-08-25','YYYY-MM-DD'));
INSERT INTO player VALUES (10, 'Olof',      'Kajbjer',     88000, 'Swedish',    'Rifler',      TO_DATE('1996-12-03','YYYY-MM-DD'));
INSERT INTO player VALUES (11, 'Robin',     'Ronnquist',   73000, 'Swedish',    'Support',     TO_DATE('2000-05-14','YYYY-MM-DD'));
INSERT INTO player VALUES (12, 'William',   'Hobbs',       71000, 'British',    'Rifler',      TO_DATE('2001-11-29','YYYY-MM-DD'));
INSERT INTO player VALUES (13, 'Yana',      'Kobzar',      69000, 'Ukrainian',  'Duelist',     TO_DATE('2002-03-07','YYYY-MM-DD'));
INSERT INTO player VALUES (14, 'Russ',      'Daley',       74000, 'American',   'IGL',         TO_DATE('1999-10-11','YYYY-MM-DD'));
INSERT INTO player VALUES (15, 'Martin',    'Lund',        77000, 'Danish',     'AWPer',       TO_DATE('2000-01-28','YYYY-MM-DD'));
INSERT INTO player VALUES (16, 'Naman',     'Mathur',      60000, 'Indian',     'IGL',         TO_DATE('1997-05-22','YYYY-MM-DD'));
INSERT INTO player VALUES (17, 'Tanvir',    'Ahmed',       58000, 'Indian',     'Rifler',      TO_DATE('1999-11-14','YYYY-MM-DD'));
INSERT INTO player VALUES (18, 'Jonathan',  'Jogi',        62000, 'Indian',     'Duelist',     TO_DATE('2001-07-19','YYYY-MM-DD'));
INSERT INTO player VALUES (19, 'Vivek',     'Horo',        57000, 'Indian',     'AWPer',       TO_DATE('2000-09-03','YYYY-MM-DD'));
INSERT INTO player VALUES (20, 'Hector',    'Rodrigues',   55000, 'Indian',     'Sentinel',    TO_DATE('2001-04-27','YYYY-MM-DD'));
INSERT INTO player VALUES (21, 'Akshat',    'Yadav',       56000, 'Indian',     'Controller',  TO_DATE('2002-02-15','YYYY-MM-DD'));
INSERT INTO player VALUES (22, 'Atharv',    'Pimpalkar',   54000, 'Indian',     'Initiator',   TO_DATE('2001-06-30','YYYY-MM-DD'));
INSERT INTO player VALUES (23, 'Sabyasachi','Bose',        59000, 'Indian',     'Rifler',      TO_DATE('2000-12-08','YYYY-MM-DD'));
INSERT INTO player VALUES (24, 'Arjun',     'Nair',        53000, 'Indian',     'Support',     TO_DATE('2002-01-20','YYYY-MM-DD'));
INSERT INTO player VALUES (25, 'Hassan',    'Butt',        50000, 'Pakistani',  'Rifler',      TO_DATE('2000-03-11','YYYY-MM-DD'));
INSERT INTO player VALUES (26, 'Shahzaib',  'Khan',        52000, 'Pakistani',  'AWPer',       TO_DATE('1999-08-25','YYYY-MM-DD'));
INSERT INTO player VALUES (27, 'Talha',     'Mirza',       48000, 'Pakistani',  'IGL',         TO_DATE('2001-05-17','YYYY-MM-DD'));
INSERT INTO player VALUES (28, 'Zain',      'Raza',        49000, 'Pakistani',  'Duelist',     TO_DATE('2002-10-04','YYYY-MM-DD'));
INSERT INTO player VALUES (29, 'Bilal',     'Ahmed',       47000, 'Pakistani',  'Controller',  TO_DATE('2001-12-22','YYYY-MM-DD'));
INSERT INTO player VALUES (30, 'Omar',      'Al Farsi',    65000, 'Emirati',    'IGL',         TO_DATE('1999-06-15','YYYY-MM-DD'));
INSERT INTO player VALUES (31, 'Khalid',    'Al Suwaidi',  63000, 'Emirati',    'AWPer',       TO_DATE('2000-02-28','YYYY-MM-DD'));
INSERT INTO player VALUES (32, 'Rayan',     'Hassan',      60000, 'Emirati',    'Duelist',     TO_DATE('2001-09-12','YYYY-MM-DD'));
INSERT INTO player VALUES (33, 'Faris',     'Al Mazrouei', 61000, 'Emirati',    'Rifler',      TO_DATE('2000-11-07','YYYY-MM-DD'));
INSERT INTO player VALUES (34, 'Abdullah',  'Al Qahtani',  68000, 'Saudi',      'IGL',         TO_DATE('1998-04-19','YYYY-MM-DD'));
INSERT INTO player VALUES (35, 'Fahad',     'Al Otaibi',   64000, 'Saudi',      'Rifler',      TO_DATE('1999-07-31','YYYY-MM-DD'));
INSERT INTO player VALUES (36, 'Saud',      'Al Shehri',   62000, 'Saudi',      'AWPer',       TO_DATE('2000-03-22','YYYY-MM-DD'));

-- PLAYER_LANGUAGES
INSERT INTO player_languages VALUES ('English',    1);
INSERT INTO player_languages VALUES ('German',     2);
INSERT INTO player_languages VALUES ('English',    2);
INSERT INTO player_languages VALUES ('Korean',     3);
INSERT INTO player_languages VALUES ('English',    3);
INSERT INTO player_languages VALUES ('Russian',    4);
INSERT INTO player_languages VALUES ('English',    4);
INSERT INTO player_languages VALUES ('Ukrainian',  5);
INSERT INTO player_languages VALUES ('Russian',    5);
INSERT INTO player_languages VALUES ('Portuguese', 6);
INSERT INTO player_languages VALUES ('Spanish',    6);
INSERT INTO player_languages VALUES ('Danish',     7);
INSERT INTO player_languages VALUES ('English',    7);
INSERT INTO player_languages VALUES ('Bosnian',    8);
INSERT INTO player_languages VALUES ('English',    8);
INSERT INTO player_languages VALUES ('English',    9);
INSERT INTO player_languages VALUES ('Swedish',    10);
INSERT INTO player_languages VALUES ('English',    10);
INSERT INTO player_languages VALUES ('Swedish',    11);
INSERT INTO player_languages VALUES ('English',    11);
INSERT INTO player_languages VALUES ('English',    12);
INSERT INTO player_languages VALUES ('Ukrainian',  13);
INSERT INTO player_languages VALUES ('English',    13);
INSERT INTO player_languages VALUES ('English',    14);
INSERT INTO player_languages VALUES ('Danish',     15);
INSERT INTO player_languages VALUES ('English',    15);
INSERT INTO player_languages VALUES ('Hindi',      16);
INSERT INTO player_languages VALUES ('English',    16);
INSERT INTO player_languages VALUES ('Hindi',      17);
INSERT INTO player_languages VALUES ('English',    17);
INSERT INTO player_languages VALUES ('Hindi',      18);
INSERT INTO player_languages VALUES ('Marathi',    18);
INSERT INTO player_languages VALUES ('Hindi',      19);
INSERT INTO player_languages VALUES ('English',    19);
INSERT INTO player_languages VALUES ('Hindi',      20);
INSERT INTO player_languages VALUES ('Telugu',     20);
INSERT INTO player_languages VALUES ('Hindi',      21);
INSERT INTO player_languages VALUES ('English',    21);
INSERT INTO player_languages VALUES ('Hindi',      22);
INSERT INTO player_languages VALUES ('Marathi',    22);
INSERT INTO player_languages VALUES ('Hindi',      23);
INSERT INTO player_languages VALUES ('Bengali',    23);
INSERT INTO player_languages VALUES ('Hindi',      24);
INSERT INTO player_languages VALUES ('Tamil',      24);
INSERT INTO player_languages VALUES ('Urdu',       25);
INSERT INTO player_languages VALUES ('English',    25);
INSERT INTO player_languages VALUES ('Urdu',       26);
INSERT INTO player_languages VALUES ('Punjabi',    26);
INSERT INTO player_languages VALUES ('Urdu',       27);
INSERT INTO player_languages VALUES ('English',    27);
INSERT INTO player_languages VALUES ('Urdu',       28);
INSERT INTO player_languages VALUES ('Sindhi',     28);
INSERT INTO player_languages VALUES ('Urdu',       29);
INSERT INTO player_languages VALUES ('English',    29);
INSERT INTO player_languages VALUES ('Arabic',     30);
INSERT INTO player_languages VALUES ('English',    30);
INSERT INTO player_languages VALUES ('Arabic',     31);
INSERT INTO player_languages VALUES ('English',    31);
INSERT INTO player_languages VALUES ('Arabic',     32);
INSERT INTO player_languages VALUES ('English',    32);
INSERT INTO player_languages VALUES ('Arabic',     33);
INSERT INTO player_languages VALUES ('Hindi',      33);
INSERT INTO player_languages VALUES ('Arabic',     34);
INSERT INTO player_languages VALUES ('English',    34);
INSERT INTO player_languages VALUES ('Arabic',     35);
INSERT INTO player_languages VALUES ('English',    35);
INSERT INTO player_languages VALUES ('Arabic',     36);
INSERT INTO player_languages VALUES ('English',    36);

-- CONTRACT
INSERT INTO contract VALUES (1,  1);
INSERT INTO contract VALUES (2,  1);
INSERT INTO contract VALUES (3,  1);
INSERT INTO contract VALUES (4,  2);
INSERT INTO contract VALUES (5,  2);
INSERT INTO contract VALUES (13, 2);
INSERT INTO contract VALUES (6,  3);
INSERT INTO contract VALUES (7,  3);
INSERT INTO contract VALUES (8,  3);
INSERT INTO contract VALUES (9,  4);
INSERT INTO contract VALUES (10, 4);
INSERT INTO contract VALUES (14, 4);
INSERT INTO contract VALUES (11, 5);
INSERT INTO contract VALUES (12, 5);
INSERT INTO contract VALUES (15, 5);
INSERT INTO contract VALUES (16, 6);
INSERT INTO contract VALUES (17, 6);
INSERT INTO contract VALUES (18, 6);
INSERT INTO contract VALUES (19, 7);
INSERT INTO contract VALUES (20, 7);
INSERT INTO contract VALUES (21, 7);
INSERT INTO contract VALUES (22, 8);
INSERT INTO contract VALUES (23, 8);
INSERT INTO contract VALUES (24, 8);
INSERT INTO contract VALUES (30, 8);
INSERT INTO contract VALUES (34, 9);
INSERT INTO contract VALUES (35, 9);
INSERT INTO contract VALUES (36, 9);
INSERT INTO contract VALUES (25, 10);
INSERT INTO contract VALUES (26, 10);
INSERT INTO contract VALUES (27, 10);
INSERT INTO contract VALUES (28, 10);
INSERT INTO contract VALUES (29, 10);

-- SPONSOR
INSERT INTO sponsor VALUES (1, 'Intel');
INSERT INTO sponsor VALUES (2, 'Red Bull');
INSERT INTO sponsor VALUES (3, 'HyperX');
INSERT INTO sponsor VALUES (4, 'Logitech');
INSERT INTO sponsor VALUES (5, 'ASUS ROG');
INSERT INTO sponsor VALUES (6, 'Mountain Dew');
INSERT INTO sponsor VALUES (7, 'Jio');
INSERT INTO sponsor VALUES (8, 'PTCL');

-- SPONSOR_CONTACT
INSERT INTO sponsor_contact VALUES ('+1-800-555-0101',    1);
INSERT INTO sponsor_contact VALUES ('+1-800-555-0102',    1);
INSERT INTO sponsor_contact VALUES ('+43-662-6582-001',   2);
INSERT INTO sponsor_contact VALUES ('+43-662-6582-002',   2);
INSERT INTO sponsor_contact VALUES ('+1-800-555-0201',    3);
INSERT INTO sponsor_contact VALUES ('+1-800-555-0202',    3);
INSERT INTO sponsor_contact VALUES ('+41-21-863-5100',    4);
INSERT INTO sponsor_contact VALUES ('+1-800-276-5568',    5);
INSERT INTO sponsor_contact VALUES ('+1-800-276-5569',    5);
INSERT INTO sponsor_contact VALUES ('+1-914-767-6000',    6);
INSERT INTO sponsor_contact VALUES ('+91-22-6660-0000',   7);
INSERT INTO sponsor_contact VALUES ('+91-22-6660-0001',   7);
INSERT INTO sponsor_contact VALUES ('+92-51-111-900-900', 8);

-- REGISTERS
INSERT INTO registers VALUES (1,  1);
INSERT INTO registers VALUES (2,  1);
INSERT INTO registers VALUES (3,  1);
INSERT INTO registers VALUES (4,  1);
INSERT INTO registers VALUES (1,  2);
INSERT INTO registers VALUES (3,  2);
INSERT INTO registers VALUES (5,  2);
INSERT INTO registers VALUES (8,  2);
INSERT INTO registers VALUES (9,  2);
INSERT INTO registers VALUES (2,  3);
INSERT INTO registers VALUES (4,  3);
INSERT INTO registers VALUES (5,  3);
INSERT INTO registers VALUES (8,  3);
INSERT INTO registers VALUES (9,  3);
INSERT INTO registers VALUES (6,  4);
INSERT INTO registers VALUES (7,  4);
INSERT INTO registers VALUES (8,  4);
INSERT INTO registers VALUES (10, 4);
INSERT INTO registers VALUES (6,  5);
INSERT INTO registers VALUES (7,  5);
INSERT INTO registers VALUES (9,  5);
INSERT INTO registers VALUES (10, 5);

-- SPONSORSHIP
INSERT INTO sponsorship VALUES (1, 1, 1);
INSERT INTO sponsorship VALUES (1, 2, 2);
INSERT INTO sponsorship VALUES (1, 3, 3);
INSERT INTO sponsorship VALUES (1, 4, 4);
INSERT INTO sponsorship VALUES (2, 1, 2);
INSERT INTO sponsorship VALUES (2, 3, 1);
INSERT INTO sponsorship VALUES (2, 5, 4);
INSERT INTO sponsorship VALUES (2, 8, 5);
INSERT INTO sponsorship VALUES (2, 9, 3);
INSERT INTO sponsorship VALUES (3, 2, 1);
INSERT INTO sponsorship VALUES (3, 4, 3);
INSERT INTO sponsorship VALUES (3, 5, 2);
INSERT INTO sponsorship VALUES (3, 8, 4);
INSERT INTO sponsorship VALUES (3, 9, 5);
INSERT INTO sponsorship VALUES (4, 6, 7);
INSERT INTO sponsorship VALUES (4, 7, 6);
INSERT INTO sponsorship VALUES (4, 8, 5);
INSERT INTO sponsorship VALUES (4, 10, 3);
INSERT INTO sponsorship VALUES (5, 6, 7);
INSERT INTO sponsorship VALUES (5, 7, 6);
INSERT INTO sponsorship VALUES (5, 9, 5);
INSERT INTO sponsorship VALUES (5, 10, 8);

-- MATCH
INSERT INTO match VALUES (1,  TO_DATE('2025-01-12','YYYY-MM-DD'), 'Dust2',    1, 2);
INSERT INTO match VALUES (2,  TO_DATE('2025-01-13','YYYY-MM-DD'), 'Mirage',   1, 2);
INSERT INTO match VALUES (3,  TO_DATE('2025-01-24','YYYY-MM-DD'), 'Inferno',  1, 2);
INSERT INTO match VALUES (4,  TO_DATE('2025-03-07','YYYY-MM-DD'), 'Ascent',   2, 1);
INSERT INTO match VALUES (5,  TO_DATE('2025-03-10','YYYY-MM-DD'), 'Bind',     2, 1);
INSERT INTO match VALUES (6,  TO_DATE('2025-05-03','YYYY-MM-DD'), 'Nuke',     3, 2);
INSERT INTO match VALUES (7,  TO_DATE('2025-05-08','YYYY-MM-DD'), 'Overpass', 3, 2);
INSERT INTO match VALUES (8,  TO_DATE('2025-06-11','YYYY-MM-DD'), 'Haven',    4, 1);
INSERT INTO match VALUES (9,  TO_DATE('2025-06-13','YYYY-MM-DD'), 'Split',    4, 1);
INSERT INTO match VALUES (10, TO_DATE('2025-06-21','YYYY-MM-DD'), 'Lotus',    4, 1);
INSERT INTO match VALUES (11, TO_DATE('2025-08-02','YYYY-MM-DD'), 'Ancient',  5, 2);
INSERT INTO match VALUES (12, TO_DATE('2025-08-05','YYYY-MM-DD'), 'Vertigo',  5, 2);
INSERT INTO match VALUES (13, TO_DATE('2025-08-11','YYYY-MM-DD'), 'Dust2',    5, 2);

-- PLAYS_IN
INSERT INTO plays_in VALUES (1,  1);
INSERT INTO plays_in VALUES (2,  1);
INSERT INTO plays_in VALUES (3,  2);
INSERT INTO plays_in VALUES (4,  2);
INSERT INTO plays_in VALUES (1,  3);
INSERT INTO plays_in VALUES (3,  3);
INSERT INTO plays_in VALUES (1,  4);
INSERT INTO plays_in VALUES (8,  4);
INSERT INTO plays_in VALUES (3,  5);
INSERT INTO plays_in VALUES (9,  5);
INSERT INTO plays_in VALUES (2,  6);
INSERT INTO plays_in VALUES (4,  6);
INSERT INTO plays_in VALUES (5,  7);
INSERT INTO plays_in VALUES (9,  7);
INSERT INTO plays_in VALUES (6,  8);
INSERT INTO plays_in VALUES (7,  8);
INSERT INTO plays_in VALUES (8,  9);
INSERT INTO plays_in VALUES (10, 9);
INSERT INTO plays_in VALUES (6,  10);
INSERT INTO plays_in VALUES (8,  10);
INSERT INTO plays_in VALUES (6,  11);
INSERT INTO plays_in VALUES (10, 11);
INSERT INTO plays_in VALUES (7,  12);
INSERT INTO plays_in VALUES (9,  12);
INSERT INTO plays_in VALUES (6,  13);
INSERT INTO plays_in VALUES (7,  13);

-- ROUND
INSERT INTO round VALUES (1, 120, 1,  1);
INSERT INTO round VALUES (2, 95,  2,  1);
INSERT INTO round VALUES (3, 110, 1,  1);
INSERT INTO round VALUES (1, 105, 3,  2);
INSERT INTO round VALUES (2, 88,  4,  2);
INSERT INTO round VALUES (3, 115, 3,  2);
INSERT INTO round VALUES (1, 130, 1,  3);
INSERT INTO round VALUES (2, 100, 3,  3);
INSERT INTO round VALUES (3, 125, 1,  3);
INSERT INTO round VALUES (1, 140, 1,  4);
INSERT INTO round VALUES (2, 118, 8,  4);
INSERT INTO round VALUES (3, 135, 1,  4);
INSERT INTO round VALUES (1, 122, 3,  5);
INSERT INTO round VALUES (2, 98,  9,  5);
INSERT INTO round VALUES (3, 108, 3,  5);
INSERT INTO round VALUES (1, 112, 2,  6);
INSERT INTO round VALUES (2, 103, 4,  6);
INSERT INTO round VALUES (3, 119, 2,  6);
INSERT INTO round VALUES (1, 127, 5,  7);
INSERT INTO round VALUES (2, 94,  9,  7);
INSERT INTO round VALUES (3, 116, 9,  7);
INSERT INTO round VALUES (1, 133, 6,  8);
INSERT INTO round VALUES (2, 108, 7,  8);
INSERT INTO round VALUES (3, 121, 6,  8);
INSERT INTO round VALUES (1, 145, 8,  9);
INSERT INTO round VALUES (2, 112, 10, 9);
INSERT INTO round VALUES (3, 138, 8,  9);
INSERT INTO round VALUES (1, 119, 6,  10);
INSERT INTO round VALUES (2, 102, 8,  10);
INSERT INTO round VALUES (3, 127, 6,  10);
INSERT INTO round VALUES (1, 135, 6,  11);
INSERT INTO round VALUES (2, 109, 10, 11);
INSERT INTO round VALUES (3, 122, 6,  11);
INSERT INTO round VALUES (1, 118, 7,  12);
INSERT INTO round VALUES (2, 97,  9,  12);
INSERT INTO round VALUES (3, 130, 7,  12);
INSERT INTO round VALUES (1, 142, 6,  13);
INSERT INTO round VALUES (2, 115, 7,  13);
INSERT INTO round VALUES (3, 128, 6,  13);

-- PERFORMS
-- match 1: liquid vs navi
INSERT INTO performs VALUES (1,  1, 5,  12, 18);
INSERT INTO performs VALUES (2,  1, 8,  14, 15);
INSERT INTO performs VALUES (3,  1, 10, 16, 12);
INSERT INTO performs VALUES (4,  1, 4,  10, 20);
INSERT INTO performs VALUES (5,  1, 6,  13, 17);
INSERT INTO performs VALUES (13, 1, 7,  15, 14);
-- match 2: faze vs cloud9
INSERT INTO performs VALUES (6,  2, 6,  11, 16);
INSERT INTO performs VALUES (7,  2, 3,  9,  19);
INSERT INTO performs VALUES (8,  2, 5,  8,  22);
INSERT INTO performs VALUES (9,  2, 9,  17, 13);
INSERT INTO performs VALUES (10, 2, 4,  10, 21);
INSERT INTO performs VALUES (14, 2, 7,  14, 15);
-- match 3: liquid vs faze (esl final)
INSERT INTO performs VALUES (1,  3, 6,  11, 19);
INSERT INTO performs VALUES (2,  3, 9,  13, 16);
INSERT INTO performs VALUES (3,  3, 12, 17, 11);
INSERT INTO performs VALUES (6,  3, 5,  15, 14);
INSERT INTO performs VALUES (7,  3, 4,  12, 17);
INSERT INTO performs VALUES (8,  3, 6,  9,  20);
-- match 4: liquid vs entity
INSERT INTO performs VALUES (1,  4, 8,  14, 15);
INSERT INTO performs VALUES (2,  4, 7,  11, 18);
INSERT INTO performs VALUES (3,  4, 11, 18, 10);
INSERT INTO performs VALUES (22, 4, 6,  13, 16);
INSERT INTO performs VALUES (23, 4, 3,  10, 20);
INSERT INTO performs VALUES (24, 4, 9,  15, 14);
-- match 5: faze vs falcons
INSERT INTO performs VALUES (6,  5, 7,  12, 17);
INSERT INTO performs VALUES (7,  5, 10, 15, 14);
INSERT INTO performs VALUES (8,  5, 8,  16, 13);
INSERT INTO performs VALUES (34, 5, 5,  13, 16);
INSERT INTO performs VALUES (35, 5, 4,  10, 19);
INSERT INTO performs VALUES (36, 5, 6,  14, 15);
-- match 6: navi vs cloud9
INSERT INTO performs VALUES (4,  6, 5,  9,  21);
INSERT INTO performs VALUES (5,  6, 6,  12, 18);
INSERT INTO performs VALUES (13, 6, 8,  14, 15);
INSERT INTO performs VALUES (9,  6, 10, 17, 12);
INSERT INTO performs VALUES (10, 6, 4,  10, 20);
INSERT INTO performs VALUES (14, 6, 7,  13, 16);
-- match 7: fnatic vs falcons
INSERT INTO performs VALUES (11, 7, 7,  14, 15);
INSERT INTO performs VALUES (12, 7, 5,  11, 18);
INSERT INTO performs VALUES (15, 7, 8,  13, 16);
INSERT INTO performs VALUES (34, 7, 6,  12, 17);
INSERT INTO performs VALUES (35, 7, 9,  16, 13);
INSERT INTO performs VALUES (36, 7, 4,  10, 19);
-- match 8: soul vs godlike
INSERT INTO performs VALUES (16, 8, 9,  13, 17);
INSERT INTO performs VALUES (17, 8, 7,  11, 19);
INSERT INTO performs VALUES (18, 8, 11, 15, 14);
INSERT INTO performs VALUES (19, 8, 5,  14, 16);
INSERT INTO performs VALUES (20, 8, 8,  17, 13);
INSERT INTO performs VALUES (21, 8, 6,  12, 15);
-- match 9: entity vs velocity
INSERT INTO performs VALUES (22, 9, 8,  13, 16);
INSERT INTO performs VALUES (23, 9, 6,  11, 18);
INSERT INTO performs VALUES (24, 9, 10, 16, 13);
INSERT INTO performs VALUES (25, 9, 7,  14, 15);
INSERT INTO performs VALUES (26, 9, 5,  10, 20);
INSERT INTO performs VALUES (27, 9, 9,  17, 12);
-- match 10: soul vs entity
INSERT INTO performs VALUES (16, 10, 10, 14, 16);
INSERT INTO performs VALUES (17, 10, 8,  12, 18);
INSERT INTO performs VALUES (18, 10, 12, 17, 13);
INSERT INTO performs VALUES (22, 10, 7,  15, 15);
INSERT INTO performs VALUES (23, 10, 5,  10, 19);
INSERT INTO performs VALUES (24, 10, 9,  16, 14);
-- match 11: soul vs velocity
INSERT INTO performs VALUES (16, 11, 8,  11, 19);
INSERT INTO performs VALUES (17, 11, 11, 15, 15);
INSERT INTO performs VALUES (18, 11, 7,  13, 17);
INSERT INTO performs VALUES (25, 11, 6,  14, 16);
INSERT INTO performs VALUES (26, 11, 9,  17, 13);
INSERT INTO performs VALUES (27, 11, 5,  10, 20);
-- match 12: godlike vs falcons
INSERT INTO performs VALUES (19, 12, 7,  13, 16);
INSERT INTO performs VALUES (20, 12, 9,  15, 14);
INSERT INTO performs VALUES (21, 12, 6,  11, 18);
INSERT INTO performs VALUES (34, 12, 8,  14, 15);
INSERT INTO performs VALUES (35, 12, 5,  10, 20);
INSERT INTO performs VALUES (36, 12, 10, 16, 13);
-- match 13: soul vs godlike (pakistan cup final)
INSERT INTO performs VALUES (16, 13, 9,  12, 18);
INSERT INTO performs VALUES (17, 13, 7,  10, 20);
INSERT INTO performs VALUES (18, 13, 11, 16, 14);
INSERT INTO performs VALUES (19, 13, 6,  13, 17);
INSERT INTO performs VALUES (20, 13, 8,  15, 15);
INSERT INTO performs VALUES (21, 13, 5,  11, 19);

COMMIT;













-- DML STATEMENTS  -- SHAIMA

-- VENUE
-- adding new venue for upcoming middle east event
INSERT INTO venue VALUES (7, 'Riyadh', 'Saudi Arabia', 'Kingdom Arena');

-- coca cola arena got renamed after new deal
UPDATE venue SET venue_name = 'Dubai Esports Arena' WHERE venue_id = 3;

-- clearing matches and tournament tied to venue 5 first before deleting
DELETE FROM match WHERE tournament_id = 5;
DELETE FROM tournament WHERE tournament_id = 5;
DELETE FROM venue WHERE venue_id = 5;


-- TOURNAMENT
-- new tournament at riyadh
INSERT INTO tournament VALUES (6, 'BLAST Premier Riyadh', TO_DATE('2025-10-01','YYYY-MM-DD'), TO_DATE('2025-10-14','YYYY-MM-DD'), 850000, 7);

-- prize pool went up after new sponsor deal
UPDATE tournament SET prize_pool = 450000 WHERE tournament_id = 4;

-- adding and removing a test entry to show delete on tournament table
INSERT INTO tournament VALUES (7, 'Test Invitational', TO_DATE('2025-12-01','YYYY-MM-DD'), TO_DATE('2025-12-05','YYYY-MM-DD'), 50000, 7);
DELETE FROM tournament WHERE tournament_id = 7;


-- GAME
-- adding pubg mobile since apex wasnt being used
INSERT INTO game VALUES (4, 'PUBG Mobile', 'Battle Royale');

-- apex reclassified
UPDATE game SET genre = 'Shooter/Battle Royale' WHERE game_id = 3;

-- removing apex, no teams playing it
DELETE FROM game WHERE game_id = 3;


-- TEAM
-- new team added
INSERT INTO team VALUES (11, 'Nigma Galaxy');

-- velocity rebranded
UPDATE team SET team_name = 'Team Spirit' WHERE team_id = 10;

-- nigma disbanded before playing any matches
DELETE FROM team WHERE team_id = 11;


-- COACH
-- team 9 already had a coach, remove first then add new one
DELETE FROM coach WHERE team_id = 9;
INSERT INTO coach VALUES (11, 'Faisal Al Dosari', 74000, 9);

-- liquid coach got raise after winning esl
UPDATE coach SET salary = 100000 WHERE coach_id = 1;

-- navi replaced coach after early exit
DELETE FROM coach WHERE coach_id = 2;


-- PLAYER
-- new emirati young player signed
INSERT INTO player VALUES (37, 'Saif', 'Al Ketbi', 45000, 'Emirati', 'Duelist', TO_DATE('2004-06-15','YYYY-MM-DD'));

-- alex hunter got mvp raise
UPDATE player SET salary = 95000 WHERE player_id = 1;

-- yana retired
DELETE FROM player WHERE player_id = 13;


-- PLAYER_LANGUAGES
-- saif speaks arabic and english
INSERT INTO player_languages VALUES ('Arabic',  37);
INSERT INTO player_languages VALUES ('English', 37);

-- sabyasachi picked up english during bootcamp abroad
UPDATE player_languages SET language = 'English' WHERE player_id = 23 AND language = 'Bengali';

-- data cleanup for robin
DELETE FROM player_languages WHERE player_id = 11 AND language = 'Swedish';


-- SPONSOR
-- new uae telecom sponsor
INSERT INTO sponsor VALUES (9, 'Etisalat');

-- mountain dew rebranded
UPDATE sponsor SET name = 'Starry' WHERE sponsor_id = 6;

-- ptcl pulled out after pakistan cup got cancelled
DELETE FROM sponsor WHERE sponsor_id = 8;


-- SPONSOR_CONTACT
-- etisalat contact numbers
INSERT INTO sponsor_contact VALUES ('+971-2-628-3333', 9);
INSERT INTO sponsor_contact VALUES ('+971-2-628-3334', 9);

-- intel updated contact
UPDATE sponsor_contact SET contact_no = '+1-800-555-0199' WHERE contact_no = '+1-800-555-0101' AND sponsor_id = 1;

-- old red bull contact removed after rep change
DELETE FROM sponsor_contact WHERE contact_no = '+43-662-6582-002' AND sponsor_id = 2;


-- MATCH
-- first match added for riyadh tournament
INSERT INTO match VALUES (14, TO_DATE('2025-10-02','YYYY-MM-DD'), 'Dust2', 6, 2);

-- map was entered wrong for match 8
UPDATE match SET map_name = 'Pearl' WHERE match_id = 8;

-- match postponed before teams were assigned
DELETE FROM match WHERE match_id = 14;


-- ROUND
-- match 1 went to overtime
INSERT INTO round VALUES (4, 95, 1, 1);

-- duration was recorded wrong
UPDATE round SET duration = 115 WHERE round_no = 2 AND match_id = 3;

-- overtime result overturned after review
DELETE FROM round WHERE round_no = 4 AND match_id = 1;


-- PERFORMS
-- saif played a warmup match
INSERT INTO performs VALUES (37, 8, 3, 8, 10);

-- niko stats were entered wrong in match 2
UPDATE performs SET kills = 25, deaths = 7 WHERE player_id = 8 AND match_id = 2;

-- warmup match wasnt official so removed
DELETE FROM performs WHERE player_id = 37 AND match_id = 8;


-- CONTRACT
-- saif signed to entity gaming
INSERT INTO contract VALUES (37, 8);

-- khalid transferred to falcons
INSERT INTO contract VALUES (31, 9);

-- omar released from entity
DELETE FROM contract WHERE player_id = 30 AND team_id = 8;


-- REGISTERS
-- falcons and liquid register for riyadh
INSERT INTO registers VALUES (9, 6);
INSERT INTO registers VALUES (1, 6);

-- entity took velocity slot in south asia open
UPDATE registers SET team_id = 9 WHERE team_id = 10 AND tournament_id = 4;

-- godlike withdrew due to visa issues
DELETE FROM registers WHERE team_id = 7 AND tournament_id = 4;


-- PLAYS_IN
INSERT INTO match VALUES (15, TO_DATE('2025-10-03','YYYY-MM-DD'), 'Mirage', 6, 2);
INSERT INTO plays_in VALUES (1,  15);
INSERT INTO plays_in VALUES (9,  15);

-- wrong team was entered for match 9
UPDATE plays_in SET team_id = 6 WHERE team_id = 8 AND match_id = 9;

-- team withdrew before match started
DELETE FROM plays_in WHERE team_id = 9 AND match_id = 15;


-- SPONSORSHIP
-- etisalat sponsors falcons in riyadh
INSERT INTO sponsorship VALUES (6, 9, 9);

-- asus rog switched from entity to liquid for iem dubai
UPDATE sponsorship SET team_id = 1 WHERE tournament_id = 3 AND team_id = 8 AND sponsor_id = 4;

-- jio pulled out from soul in south asia open
DELETE FROM sponsorship WHERE tournament_id = 4 AND team_id = 6 AND sponsor_id = 7;

COMMIT;













-- SELECT QUERIES 

-- 1. equi join
-- showing all players with their team and role
SELECT p.first_name, p.last_name, p.nationality, p.role, t.team_name
FROM player p, contract c, team t
WHERE p.player_id = c.player_id
AND c.team_id = t.team_id
ORDER BY t.team_name;


-- 2. natural join
-- teams and the matches they played in
SELECT team_name, match_id, match_date, map_name
FROM team
NATURAL JOIN plays_in
NATURAL JOIN match
ORDER BY match_date;


-- 3. left outer join
-- all players and their stats, players with no matches show as null
SELECT p.first_name, p.last_name, p.role, pf.match_id, pf.kills, pf.deaths, pf.assists
FROM player p
LEFT OUTER JOIN performs pf ON p.player_id = pf.player_id
ORDER BY p.player_id;


-- 4. right outer join
-- all matches and which players performed, matches with no stats show as null
SELECT p.first_name, p.last_name, pf.match_id, pf.kills, pf.deaths, pf.assists
FROM performs pf
RIGHT OUTER JOIN player p ON pf.player_id = p.player_id
ORDER BY pf.match_id;


-- 5. self join
-- players who share the same nationality (same country, different players)
SELECT p1.first_name || ' ' || p1.last_name AS player_1,
       p2.first_name || ' ' || p2.last_name AS player_2,
       p1.nationality
FROM player p1, player p2
WHERE p1.nationality = p2.nationality
AND p1.player_id < p2.player_id
ORDER BY p1.nationality;


-- 6. aggregate + subquery #1
-- teams whose total kills are above the average total kills across all teams
SELECT t.team_name, SUM(pf.kills) AS total_kills
FROM team t
JOIN contract c ON t.team_id = c.team_id
JOIN performs pf ON c.player_id = pf.player_id
GROUP BY t.team_name
HAVING SUM(pf.kills) > (
    SELECT AVG(team_kills)
    FROM (
        SELECT SUM(pf2.kills) AS team_kills
        FROM contract c2
        JOIN performs pf2 ON c2.player_id = pf2.player_id
        GROUP BY c2.team_id
    )
)
ORDER BY total_kills DESC;


-- 7. aggregate + subquery #2
-- tournaments where prize pool is higher than the average prize pool
SELECT name, prize_pool, start_date, end_date
FROM tournament
WHERE prize_pool > (SELECT AVG(prize_pool) FROM tournament)
ORDER BY prize_pool DESC;


-- 8. multi-row subquery #1
-- players who played in matches that took place at the dubai venue
SELECT first_name, last_name, nationality, role
FROM player
WHERE player_id IN (
    SELECT player_id FROM performs
    WHERE match_id IN (
        SELECT match_id FROM match
        WHERE tournament_id IN (
            SELECT tournament_id FROM tournament
            WHERE venue_id IN (
                SELECT venue_id FROM venue WHERE city = 'Dubai'
            )
        )
    )
);


-- 9. multi-row subquery #2
-- teams that have at least one sponsor in common with team liquid (not including liquid)
SELECT DISTINCT t.team_name
FROM team t
WHERE t.team_id IN (
    SELECT team_id FROM sponsorship
    WHERE sponsor_id IN (
        SELECT sponsor_id FROM sponsorship
        WHERE team_id IN (
            SELECT team_id FROM team WHERE team_name = 'Team Liquid'
        )
    )
)
AND t.team_name != 'Team Liquid';


-- 10. 3 levels of nesting
-- players who scored more kills than the average kills of players
-- who belong to teams that have won at least one round
SELECT first_name, last_name, nationality, role
FROM player
WHERE player_id IN (
    SELECT player_id FROM performs
    WHERE kills > (
        SELECT AVG(kills) FROM performs
        WHERE player_id IN (
            SELECT player_id FROM contract
            WHERE team_id IN (
                SELECT DISTINCT winner FROM round
                WHERE winner IS NOT NULL
            )
        )
    )
)
ORDER BY last_name;


ALTER TABLE sponsorship
ADD CONSTRAINT sponsorship_unique_tournament
UNIQUE (tournament_id, sponsor_id);













