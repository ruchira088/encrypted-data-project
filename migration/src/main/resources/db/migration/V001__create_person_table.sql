CREATE TABLE person (
    id CHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    username BYTEA NOT NULL,
    first_name BYTEA NOT NULL,
    last_name BYTEA NOT NULL,
    email BYTEA NOT NULL,

    PRIMARY KEY (id)
);