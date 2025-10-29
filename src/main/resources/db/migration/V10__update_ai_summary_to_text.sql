-- Update ai_summary column from VARCHAR(2048) to TEXT to support longer content
ALTER TABLE contacts MODIFY COLUMN ai_summary TEXT;
