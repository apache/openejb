

CREATE TABLE cabin (
    id INT PRIMARY KEY NOT NULL, 
    ship_id INT,
    bed_count INT,
    name CHAR(30) ,
    deck_level INT
)
GO

CREATE TABLE ship (
    id INT PRIMARY KEY NOT NULL,
    name CHAR(30),
    capacity INT,
    tonnage DECIMAL(8,2)
)
GO
