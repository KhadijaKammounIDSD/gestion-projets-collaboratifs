-- Add project relationship to tasks table
USE project_management;

-- Add project_id column to task table
ALTER TABLE task 
ADD COLUMN project_id BIGINT UNSIGNED NULL AFTER assignee_id;

-- Add foreign key constraint
ALTER TABLE task
ADD CONSTRAINT fk_task_project FOREIGN KEY (project_id) 
    REFERENCES project(id) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE;

-- Add index for better query performance
CREATE INDEX idx_task_project_id ON task(project_id);

SELECT 'Project-Task relationship added successfully!' AS Status;
