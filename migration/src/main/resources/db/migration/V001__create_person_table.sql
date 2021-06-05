CREATE TABLE person (
    id CHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    username VARBINARY(256) NOT NULL,
    first_name VARBINARY(128) NOT NULL,
    last_name VARBINARY(128) NOT NULL,
    email VARBINARY(256) NOT NULL,

    PRIMARY KEY (id)
);