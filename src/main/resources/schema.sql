CREATE TABLE IF NOT EXISTS
    USERS (
        uid INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT NOT NULL UNIQUE,
        password TEXT NOT NULL,
        email TEXT UNIQUE,
        role TEXT NOT NULL DEFAULT 'user',
        active INTEGER NOT NULL DEFAULT 0,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS
    MOVIES (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        title TEXT NOT NULL,
        description TEXT,
        director TEXT,
        movie_cast TEXT,
        duration INTEGER NOT NULL CHECK (duration > 0),
        movie_type TEXT NOT NULL,
        language TEXT,
        rating INTEGER CHECK (rating BETWEEN 1 AND 5),
        trailer_url TEXT,
        poster_url TEXT,
        release_date TIMESTAMP NOT NULL,
        status TEXT NOT NULL DEFAULT 'COMING_SOON'
    );

CREATE TABLE IF NOT EXISTS
    SCREENINGS (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        movie_id INTEGER NOT NULL,
        start_time TIMESTAMP NOT NULL,
        room_number TEXT NOT NULL,
        price INTEGER NOT NULL CHECK (price >= 0),
        available_seats INTEGER NOT NULL CHECK (available_seats >= 0),
        FOREIGN KEY (movie_id) REFERENCES MOVIES (id)
    );

CREATE TABLE IF NOT EXISTS
    TICKETS (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        uid INTEGER NOT NULL,
        screening_id INTEGER NOT NULL,
        seat_number TEXT NOT NULL,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (uid) REFERENCES USERS (uid),
        FOREIGN KEY (screening_id) REFERENCES SCREENINGS (id),
        UNIQUE (screening_id, seat_number)
    );

INSERT INTO USERS (username, password, email, role, active)
SELECT 'admin', 'admin123', '', 'admin', 1
WHERE NOT EXISTS (SELECT 1 FROM USERS WHERE role = 'admin');
