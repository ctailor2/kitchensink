CREATE TABLE member (
    id IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    CONSTRAINT email_uniq UNIQUE (email)
)