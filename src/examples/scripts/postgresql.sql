DROP USER username;
DROP TABLE cabin;
DROP SEQUENCE cabin_seq;
DROP TABLE ship;
DROP SEQUENCE ship_seq;

CREATE USER username;
ALTER USER username PASSWORD 'password';

CREATE TABLE cabin (
    id INTEGER PRIMARY KEY NOT NULL, 
    ship_id INTEGER,
    bed_count INTEGER,
    name VARCHAR(30),
    deck_level INTEGER
);

ALTER TABLE cabin OWNER TO username;

CREATE SEQUENCE cabin_seq;

CREATE TABLE ship (
    id INTEGER PRIMARY KEY NOT NULL,
    name VARCHAR(30),
    capacity INTEGER,
    tonnage DECIMAL(8,2)
);

CREATE SEQUENCE ship_seq;

ALTER TABLE ship OWNER TO username;