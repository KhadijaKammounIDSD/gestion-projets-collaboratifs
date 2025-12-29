-- ============================================================================
-- Migration: Add Availability Tracking to Members
-- Date: 2025-12-28
-- Description: Adds weekly_availability and remaining_hours columns
-- ============================================================================

USE project_management;

-- Add weekly_availability column (40 hours per week standard)
ALTER TABLE member 
ADD COLUMN weekly_availability DOUBLE NOT NULL DEFAULT 40.0 AFTER available;

-- Add remaining_hours column (tracks remaining hours this week)
ALTER TABLE member 
ADD COLUMN remaining_hours DOUBLE NOT NULL DEFAULT 40.0 AFTER weekly_availability;

-- Update existing members to have default values
UPDATE member 
SET weekly_availability = 40.0, 
    remaining_hours = 40.0 
WHERE weekly_availability IS NULL OR remaining_hours IS NULL;

-- Add index for performance
CREATE INDEX idx_member_remaining_hours ON member(remaining_hours);

-- Verification query
SELECT 
    id, 
    CONCAT(first_name, ' ', last_name) AS name, 
    current_load, 
    available, 
    weekly_availability, 
    remaining_hours 
FROM member;
