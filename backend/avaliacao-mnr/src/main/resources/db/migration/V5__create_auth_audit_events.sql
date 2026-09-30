CREATE TABLE auth_audit_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    email VARCHAR(255),
    event_type VARCHAR(40) NOT NULL,
    successful BOOLEAN NOT NULL,
    ip_address VARCHAR(64),
    user_agent VARCHAR(512),
    details VARCHAR(512),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_auth_audit_events_user_id ON auth_audit_events(user_id);
CREATE INDEX idx_auth_audit_events_created_at ON auth_audit_events(created_at);
CREATE INDEX idx_auth_audit_events_type ON auth_audit_events(event_type);