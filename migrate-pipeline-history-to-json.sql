-- Use if pipeline_history was created as BIGINT (or any non-JSON type) and inserts fail.
-- MySQL 8+

USE thebrideside;

-- If the column is numeric, convert stored values to a JSON array of one id, then change type.
-- Step 1: add temp column
ALTER TABLE deals ADD COLUMN pipeline_history_json JSON NULL;

UPDATE deals
SET pipeline_history_json = JSON_ARRAY(pipeline_history)
WHERE pipeline_history IS NOT NULL;

ALTER TABLE deals DROP COLUMN pipeline_history;
ALTER TABLE deals CHANGE COLUMN pipeline_history_json pipeline_history JSON NULL;
