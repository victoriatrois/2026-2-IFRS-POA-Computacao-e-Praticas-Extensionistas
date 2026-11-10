ALTER TABLE evaluations
    ADD COLUMN article_present BOOLEAN,
    ADD COLUMN video_present BOOLEAN,
    ADD COLUMN honorable_mention BOOLEAN,
    ADD COLUMN organization_notes TEXT,
    ADD COLUMN participant_feedback TEXT;
