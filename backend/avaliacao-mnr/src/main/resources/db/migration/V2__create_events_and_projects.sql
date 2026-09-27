CREATE TABLE IF NOT EXISTS events (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    pdf_url TEXT,
    level VARCHAR(50),
    video_url TEXT,
    participant_name VARCHAR(255),
    participant_cpf VARCHAR(20),
    participant_email VARCHAR(255),
    institution_name VARCHAR(255),
    marked_for_review BOOLEAN NOT NULL DEFAULT FALSE,
    validated BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_projects_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_projects_event_id ON projects (event_id);
CREATE INDEX IF NOT EXISTS idx_projects_name ON projects (name);
