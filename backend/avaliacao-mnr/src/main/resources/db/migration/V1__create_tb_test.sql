CREATE TABLE IF NOT EXISTS tb_test (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO tb_test (description) VALUES ('Database connection and Flyway migration test successful!');
