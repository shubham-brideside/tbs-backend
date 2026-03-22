-- 1) Store guest count / range as text (e.g. "100-300")
-- 2) Add pipeline history, source pipeline id, contacted_to

USE thebrideside;

ALTER TABLE deals MODIFY COLUMN expected_gathering VARCHAR(64) NULL;

ALTER TABLE deals ADD COLUMN pipeline_history JSON NULL;
ALTER TABLE deals ADD COLUMN source_pipeline_id BIGINT NULL;
ALTER TABLE deals ADD COLUMN contacted_to BIGINT NULL;

-- Optional: backfill pipeline / owner fields for existing rows (app sets these on new saves)
-- UPDATE deals SET pipeline_history = 67 WHERE pipeline_history IS NULL;
-- UPDATE deals SET source_pipeline_id = 67 WHERE source_pipeline_id IS NULL;
-- UPDATE deals SET contacted_to = 69 WHERE contacted_to IS NULL;
