CREATE TABLE project_evaluator_assignments (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    evaluator_id UUID NOT NULL REFERENCES tb_users(id),
    assigned_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_project_evaluator_assignment UNIQUE (project_id, evaluator_id)
);

CREATE INDEX idx_assignment_evaluator_project
    ON project_evaluator_assignments (evaluator_id, project_id);

ALTER TABLE evaluations
    ADD COLUMN assignment_id BIGINT REFERENCES project_evaluator_assignments(id);

CREATE UNIQUE INDEX uk_evaluation_assignment
    ON evaluations (assignment_id)
    WHERE assignment_id IS NOT NULL;
