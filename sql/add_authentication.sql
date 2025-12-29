-- ============================================================================
-- ADD AUTHENTICATION SUPPORT TO PROJECT MANAGEMENT DATABASE
-- ============================================================================

USE project_management;

-- Add password field to member table
ALTER TABLE member 
ADD COLUMN password VARCHAR(255) NULL AFTER email;

-- Add index for login lookups
CREATE INDEX idx_member_email_password ON member(email, password);

-- Update existing members to have a default password (optional)
-- You can remove this if you want all users to signup fresh
-- UPDATE member SET password = 'password123' WHERE password IS NULL;

SELECT 'Authentication columns added successfully!' AS Status;
