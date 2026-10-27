CREATE TABLE evaluations (
    id BIGSERIAL PRIMARY KEY,
    evaluator_id UUID NOT NULL REFERENCES tb_users(id),
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE evaluation_criteria (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    max_score DOUBLE PRECISION NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    display_order INTEGER NOT NULL
);

CREATE TABLE evaluation_scores (
    id BIGSERIAL PRIMARY KEY,
    evaluation_id BIGINT NOT NULL REFERENCES evaluations(id),
    criterion_id BIGINT NOT NULL REFERENCES evaluation_criteria(id),
    score DOUBLE PRECISION NOT NULL,
    comment TEXT
);