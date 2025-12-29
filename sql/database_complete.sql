-- ============================================================================
-- PLATEFORME DE GESTION DE PROJETS COLLABORATIFS
-- Base de données MySQL - Structure uniquement (sans données)
-- Date: 2025-12-28
-- ============================================================================

-- Supprimer la base si elle existe déjà (ATTENTION: efface toutes les données!)
DROP DATABASE IF EXISTS project_management;

-- Créer la base de données
CREATE DATABASE project_management 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Utiliser la base de données
USE project_management;

-- ============================================================================
-- CRÉATION DES TABLES
-- ============================================================================

-- Table: team
-- Description: Équipes de travail
CREATE TABLE team (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    INDEX idx_team_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: member
-- Description: Membres de l'équipe avec charge de travail
CREATE TABLE member (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(320) NOT NULL,
    password VARCHAR(255),
    role VARCHAR(100),
    current_load DOUBLE NOT NULL DEFAULT 0.0,
    available TINYINT(1) NOT NULL DEFAULT 1,
    weekly_availability DOUBLE NOT NULL DEFAULT 40.0,
    remaining_hours DOUBLE NOT NULL DEFAULT 40.0,
    team_id BIGINT UNSIGNED NULL,
    UNIQUE KEY uq_member_email (email),
    INDEX idx_member_team_id (team_id),
    INDEX idx_member_available (available),
    INDEX idx_member_load (current_load),
    CONSTRAINT fk_member_team FOREIGN KEY (team_id) 
        REFERENCES team(id) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: skill
-- Description: Compétences techniques
CREATE TABLE skill (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    UNIQUE KEY uq_skill_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: member_skill
-- Description: Association membre-compétence avec niveau d'expertise (1-5)
CREATE TABLE member_skill (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT UNSIGNED NOT NULL,
    skill_id BIGINT UNSIGNED NOT NULL,
    level TINYINT UNSIGNED NOT NULL,
    UNIQUE KEY uq_member_skill_member_skill (member_id, skill_id),
    INDEX idx_member_skill_member_id (member_id),
    INDEX idx_member_skill_skill_id (skill_id),
    INDEX idx_member_skill_level (level),
    CONSTRAINT fk_ms_member FOREIGN KEY (member_id) 
        REFERENCES member(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT fk_ms_skill FOREIGN KEY (skill_id) 
        REFERENCES skill(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CHECK (level >= 1 AND level <= 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: project
-- Description: Projets avec dates et statut
CREATE TABLE project (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    status VARCHAR(100),
    INDEX idx_project_status (status),
    INDEX idx_project_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: task
-- Description: Tâches avec assignation aux membres
CREATE TABLE task (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    estimated_duration DOUBLE NOT NULL DEFAULT 0.0,
    planned_start_date DATE,
    planned_end_date DATE,
    priority VARCHAR(50),
    status VARCHAR(50),
    assignee_id BIGINT UNSIGNED NULL,
    project_id BIGINT UNSIGNED NULL,
    INDEX idx_task_assignee_id (assignee_id),
    INDEX idx_task_project_id (project_id),
    INDEX idx_task_status (status),
    INDEX idx_task_priority (priority),
    INDEX idx_task_dates (planned_start_date, planned_end_date),
    CONSTRAINT fk_task_assignee FOREIGN KEY (assignee_id) 
        REFERENCES member(id) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) 
        REFERENCES project(id) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: task_dependency
-- Description: Dépendances entre tâches (Predecessor -> Successor)
CREATE TABLE task_dependency (
    task_id BIGINT UNSIGNED NOT NULL,
    depends_on_task_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (task_id, depends_on_task_id),
    CONSTRAINT fk_dependency_task FOREIGN KEY (task_id) 
        REFERENCES task(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT fk_dependency_predecessor FOREIGN KEY (depends_on_task_id) 
        REFERENCES task(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: task_skill
-- Description: Compétences requises pour une tâche (pour l'affectation intelligente)
CREATE TABLE task_skill (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT UNSIGNED NOT NULL,
    skill_id BIGINT UNSIGNED NOT NULL,
    required_level TINYINT UNSIGNED NOT NULL DEFAULT 1,
    UNIQUE KEY uq_task_skill (task_id, skill_id),
    INDEX idx_task_skill_task (task_id),
    INDEX idx_task_skill_skill (skill_id),
    CONSTRAINT fk_task_skill_task FOREIGN KEY (task_id) 
        REFERENCES task(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT fk_task_skill_skill FOREIGN KEY (skill_id) 
        REFERENCES skill(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CHECK (required_level >= 1 AND required_level <= 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: alert
-- Description: Alertes du système (surcharge, retards, etc.)
CREATE TABLE alert (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(100),
    message TEXT,
    issued_date DATE,
    severity_level VARCHAR(50),
    INDEX idx_alert_severity (severity_level),
    INDEX idx_alert_date (issued_date),
    INDEX idx_alert_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- VUES UTILES
-- ============================================================================

-- Vue: Membres avec leurs compétences
CREATE OR REPLACE VIEW v_members_with_skills AS
SELECT 
    m.id AS member_id,
    CONCAT(m.first_name, ' ', m.last_name) AS member_name,
    m.email,
    m.role,
    m.current_load,
    m.available,
    t.name AS team_name,
    GROUP_CONCAT(CONCAT(s.name, ' (', ms.level, ')') SEPARATOR ', ') AS skills
FROM member m
LEFT JOIN team t ON m.team_id = t.id
LEFT JOIN member_skill ms ON m.id = ms.member_id
LEFT JOIN skill s ON ms.skill_id = s.id
GROUP BY m.id, m.first_name, m.last_name, m.email, m.role, m.current_load, m.available, t.name;

-- Vue: Tâches avec assignation
CREATE OR REPLACE VIEW v_tasks_with_assignment AS
SELECT 
    t.id AS task_id,
    t.name AS task_name,
    t.description,
    t.estimated_duration,
    t.planned_start_date,
    t.planned_end_date,
    t.priority,
    t.status,
    CONCAT(m.first_name, ' ', m.last_name) AS assigned_to,
    m.email AS assignee_email
FROM task t
LEFT JOIN member m ON t.assignee_id = m.id;

-- Vue: Statistiques par équipe
CREATE OR REPLACE VIEW v_team_statistics AS
SELECT 
    t.id AS team_id,
    t.name AS team_name,
    COUNT(DISTINCT m.id) AS total_members,
    SUM(m.available) AS available_members,
    AVG(m.current_load) AS avg_load,
    SUM(m.current_load) AS total_load
FROM team t
LEFT JOIN member m ON t.id = m.team_id
GROUP BY t.id, t.name;

-- ============================================================================
-- DONNÉES INITIALES (Compétences)
-- ============================================================================

INSERT INTO skill (name) VALUES 
    ('Java'),
    ('Python'),
    ('JavaScript'),
    ('HTML/CSS'),
    ('SQL'),
    ('React'),
    ('Node.js'),
    ('Spring Boot'),
    ('Machine Learning'),
    ('Docker'),
    ('Kubernetes'),
    ('AWS'),
    ('DevOps'),
    ('Agile/Scrum'),
    ('Project Management'),
    ('Design UI/UX'),
    ('Testing/QA'),
    ('Git'),
    ('Communication'),
    ('Leadership');

-- ============================================================================
-- CONFIRMATION
-- ============================================================================

SELECT '═══════════════════════════════════════════════════' AS '';
SELECT '   BASE DE DONNÉES CRÉÉE AVEC SUCCÈS ✓' AS '';
SELECT '   (Tables avec compétences pré-remplies)' AS '';
SELECT '═══════════════════════════════════════════════════' AS '';
