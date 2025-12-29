package dao;

import classes.Alert;
import classes.Member;
import classes.Task;
import classes.Project;
import classes.Team;
import classes.Skill;
import classes.MemberSkill;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des tâches dans la base de données
 */
public class TaskDAO {

    private Connection connection;

    public TaskDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Ajouter une nouvelle tâche
     */
    public boolean addTask(Task task) {
        String sql = "INSERT INTO task (name, description, estimated_duration, planned_start_date, " +
                "planned_end_date, priority, status, assignee_id, project_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, task.getName());
            ps.setString(2, task.getDescription());
            ps.setDouble(3, task.getEstimatedDuration());
            ps.setDate(4, task.getPlannedStartDate() != null ? Date.valueOf(task.getPlannedStartDate()) : null);
            ps.setDate(5, task.getPlannedEndDate() != null ? Date.valueOf(task.getPlannedEndDate()) : null);
            ps.setString(6, task.getPriority());
            ps.setString(7, task.getStatus());

            if (task.getAssigneeId() > 0) {
                ps.setInt(8, task.getAssigneeId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }

            if (task.getProjectId() != null && task.getProjectId() > 0) {
                ps.setInt(9, task.getProjectId());
            } else {
                ps.setNull(9, Types.INTEGER);
            }

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        task.setId(generatedKeys.getInt(1));
                    }
                }

                // Sauvegarder les dépendances
                if (!task.getDependencyIds().isEmpty()) {
                    saveDependencies(task.getId(), task.getDependencyIds());
                }

                // Sauvegarder les compétences requises
                if (!task.getRequiredSkillIds().isEmpty()) {
                    saveTaskSkills(task.getId(), task.getRequiredSkillIds());
                }

                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Récupérer une tâche par son ID
     */
    public Task getTaskById(int id) {
        String sql = "SELECT * FROM task WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractTaskFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupérer toutes les tâches
     */
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM task";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tasks.add(extractTaskFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    /**
     * Mettre à jour une tâche
     */
    public boolean updateTask(Task task) {
        // Get old task to find previous assignee for workload recalculation
        Task oldTask = getTaskById(task.getId());
        int oldAssigneeId = (oldTask != null && oldTask.getAssigneeId() > 0) ? oldTask.getAssigneeId() : 0;
        int newAssigneeId = (task.getAssigneeId() > 0) ? task.getAssigneeId() : 0;
        
        String sql = "UPDATE task SET name = ?, description = ?, estimated_duration = ?, " +
                "planned_start_date = ?, planned_end_date = ?, priority = ?, status = ?, " +
                "assignee_id = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, task.getName());
            ps.setString(2, task.getDescription());
            ps.setDouble(3, task.getEstimatedDuration());
            ps.setDate(4, task.getPlannedStartDate() != null ? Date.valueOf(task.getPlannedStartDate()) : null);
            ps.setDate(5, task.getPlannedEndDate() != null ? Date.valueOf(task.getPlannedEndDate()) : null);
            ps.setString(6, task.getPriority());
            ps.setString(7, task.getStatus());

            if (task.getAssigneeId() > 0) {
                ps.setInt(8, task.getAssigneeId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }

            ps.setInt(9, task.getId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                // Mettre à jour les dépendances (supprimer anciennes + ajouter nouvelles)
                deleteDependencies(task.getId());
                if (!task.getDependencyIds().isEmpty()) {
                    saveDependencies(task.getId(), task.getDependencyIds());
                }
                
                // Update task skills
                deleteTaskSkills(task.getId());
                if (!task.getRequiredSkillIds().isEmpty()) {
                    saveTaskSkills(task.getId(), task.getRequiredSkillIds());
                }
                
                // Recalculate workload for affected members (single source of truth)
                MemberDAO memberDAO = new MemberDAO(connection);
                if (oldAssigneeId > 0 && oldAssigneeId != newAssigneeId) {
                    // Old assignee lost this task - recalculate their workload
                    memberDAO.recalculateMemberWorkload(oldAssigneeId);
                }
                if (newAssigneeId > 0) {
                    // New assignee got this task - recalculate their workload
                    memberDAO.recalculateMemberWorkload(newAssigneeId);
                }
                
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Supprimer une tâche
     */
    public boolean deleteTask(int id) {
        // Get task before deletion to find assignee for workload recalculation
        Task task = getTaskById(id);
        int assigneeId = (task != null && task.getAssigneeId() > 0) ? task.getAssigneeId() : 0;
        
        String sql = "DELETE FROM task WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            
            if (rows > 0 && assigneeId > 0) {
                // Recalculate workload for the member who had this task (single source of truth)
                MemberDAO memberDAO = new MemberDAO(connection);
                memberDAO.recalculateMemberWorkload(assigneeId);
            }
            
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Assigner une tâche à un membre
     */
    public boolean assignTaskToMember(int taskId, int memberId) {
        String sql = "UPDATE task SET assignee_id = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setInt(2, taskId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Récupérer les tâches non assignées
     */
    public List<Task> getUnassignedTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM task WHERE assignee_id IS NULL";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tasks.add(extractTaskFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    /**
     * Récupérer les tâches assignées à un membre
     */
    public List<Task> getTasksByMember(int memberId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM task WHERE assignee_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, memberId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(extractTaskFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    /**
     * Récupérer les tâches par priorité
     */
    public List<Task> getTasksByPriority(String priority) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM task WHERE priority = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, priority);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(extractTaskFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    /**
     * Récupérer les tâches par statut
     */
    public List<Task> getTasksByStatus(String status) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM task WHERE status = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(extractTaskFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    /**
     * Récupérer les tâches avec deadline proche
     */
    public List<Task> getTasksWithUpcomingDeadline(int daysAhead) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM task WHERE planned_end_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, daysAhead);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(extractTaskFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    /**
     * Extraire une Task depuis un ResultSet
     */
    private Task extractTaskFromResultSet(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setName(rs.getString("name"));
        task.setDescription(rs.getString("description"));
        task.setEstimatedDuration(rs.getDouble("estimated_duration"));

        Date startDate = rs.getDate("planned_start_date");
        Date endDate = rs.getDate("planned_end_date");
        
        LocalDate startLocalDate = startDate != null ? startDate.toLocalDate() : null;
        LocalDate endLocalDate = endDate != null ? endDate.toLocalDate() : null;
        
        // Validate date order - swap if necessary to prevent IllegalArgumentException
        if (startLocalDate != null && endLocalDate != null && endLocalDate.isBefore(startLocalDate)) {
            System.err.println("⚠️  Task ID " + rs.getInt("id") + " has invalid dates: start=" + startLocalDate + ", end=" + endLocalDate + ". Swapping them.");
            LocalDate temp = startLocalDate;
            startLocalDate = endLocalDate;
            endLocalDate = temp;
        }
        
        task.setPlannedStartDate(startLocalDate);
        task.setPlannedEndDate(endLocalDate);

        task.setPriority(rs.getString("priority"));
        task.setStatus(rs.getString("status"));

        int assigneeId = rs.getInt("assignee_id");
        if (!rs.wasNull()) {
            task.setAssigneeId(assigneeId);
        }
        
        // Extract projectId from database
        int projectId = rs.getInt("project_id");
        if (!rs.wasNull()) {
            task.setProjectId(projectId);
        }

        // Charger les dépendances
        task.setDependencyIds(getTaskDependencies(task.getId()));

        // Charger les compétences requises
        task.setRequiredSkillIds(getTaskRequiredSkills(task.getId()));

        return task;
    }

    /**
     * Sauvegarder les dépendances d'une tâche
     */
    private void saveDependencies(int taskId, List<Integer> dependencyIds) {
        String sql = "INSERT INTO task_dependency (task_id, depends_on_task_id) VALUES (?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Integer depId : dependencyIds) {
                ps.setInt(1, taskId);
                ps.setInt(2, depId);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Supprimer les dépendances d'une tâche
     */
    private void deleteDependencies(int taskId) {
        String sql = "DELETE FROM task_dependency WHERE task_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupérer les dépendances d'une tâche
     */
    private List<Integer> getTaskDependencies(int taskId) {
        List<Integer> dependencies = new ArrayList<>();
        String sql = "SELECT depends_on_task_id FROM task_dependency WHERE task_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, taskId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dependencies.add(rs.getInt("depends_on_task_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dependencies;
    }

    /**
     * Sauvegarder les compétences requises pour une tâche
     */
    private void saveTaskSkills(int taskId, List<Integer> skillIds) {
        String sql = "INSERT INTO task_skill (task_id, skill_id, required_level) VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Integer skillId : skillIds) {
                ps.setInt(1, taskId);
                ps.setInt(2, skillId);
                ps.setInt(3, 1); // Default required level = 1
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Supprimer les compétences requises d'une tâche
     */
    private void deleteTaskSkills(int taskId) {
        String sql = "DELETE FROM task_skill WHERE task_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupérer les compétences requises pour une tâche
     */
    private List<Integer> getTaskRequiredSkills(int taskId) {
        List<Integer> skills = new ArrayList<>();
        String sql = "SELECT skill_id FROM task_skill WHERE task_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, taskId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    skills.add(rs.getInt("skill_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return skills;
    }

    /**
     * Récupérer les compétences requises pour une tâche avec leurs noms
     */
    public List<Skill> getTaskRequiredSkillsWithNames(int taskId) {
        List<Skill> skills = new ArrayList<>();
        String sql = "SELECT s.id, s.name, ts.required_level " +
                     "FROM task_skill ts " +
                     "JOIN skill s ON ts.skill_id = s.id " +
                     "WHERE ts.task_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, taskId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Skill skill = new Skill();
                    skill.setId(rs.getInt("id"));
                    skill.setName(rs.getString("name"));
                    skills.add(skill);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return skills;
    }
}
