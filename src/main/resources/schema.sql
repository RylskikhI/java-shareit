CREATE TABLE IF NOT EXISTS users (
     id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
     name VARCHAR(255) NOT NULL,
     email VARCHAR(255) NOT NULL,
     UNIQUE(email)
);

CREATE TABLE IF NOT EXISTS requests (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    description VARCHAR(1000) NOT NULL,
    requestor_id BIGINT,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    FOREIGN KEY (requestor_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS items (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    is_available BOOLEAN NOT NULL,
    owner_id BIGINT,
    request_id BIGINT,
    FOREIGN KEY(owner_id) REFERENCES users(id),
    FOREIGN KEY(request_id) REFERENCES requests(id)
);

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    item_id BIGINT,
    booker_id BIGINT,
    status VARCHAR(8) NOT NULL,
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (booker_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    text VARCHAR(300) NOT NULL,
    created TIMESTAMP WITH TIME ZONE NOT NULL,
    item_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    FOREIGN KEY(item_id) REFERENCES items(id),
    FOREIGN KEY(author_id) REFERENCES users(id)
);

