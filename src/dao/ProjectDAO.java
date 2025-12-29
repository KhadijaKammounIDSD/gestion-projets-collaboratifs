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
 * DAO pour la gestion des projets dans la base de données
 */
public class ProjectDAO {

    private Connection connection;

    public ProjectDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Ajouter un nouveau projet
     */
    public boolean addProject(Project project) {
        String sql = "INSERT INTO project (name, description, start_date, end_date, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, project.getName());
            ps.setString(2, project.getDescription());
            ps.setDate(3, project.getStartDate() != null ? Date.valueOf(project.getStartDate()) : null);
            ps.setDate(4, project.getEndDate() != null ? Date.valueOf(project.getEndDate()) : null);
            ps.setString(5, project.getStatus());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        project.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Récupérer un projet par son ID
     */
    public Project getProjectById(int id) {
        String sql = "SELECT * FROM project WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractProjectFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupérer tous les projets
     */
    public List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM project";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                projects.add(extractProjectFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }

    /**
     * Mettre à jour un projet
     */
    public boolean updateProject(Project project) {
        String sql = "UPDATE project SET name = ?, description = ?, start_date = ?, " +
                "end_date = ?, status = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, project.getName());
            ps.setString(2, project.getDescription());
            ps.setDate(3, project.getStartDate() != null ? Date.valueOf(project.getStartDate()) : null);
            ps.setDate(4, project.getEndDate() != null ? Date.valueOf(project.getEndDate()) : null);
            ps.setString(5, project.getStatus());
            ps.setInt(6, project.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Supprimer un projet
     */
    public boolean deleteProject(int id) {
        String sql = "DELETE FROM project WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Récupérer les projets par statut
     */
    public List<Project> getProjectsByStatus(String status) {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM project WHERE status = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    projects.add(extractProjectFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }

    /**
     * Récupérer les projets actifs
     */
    public List<Project> getActiveProjects() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM project WHERE status IN ('En cours', 'Planifié')";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                projects.add(extractProjectFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }

    /**
     * Extraire un Project depuis un ResultSet
     */
    private Project extractProjectFromResultSet(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setId(rs.getInt("id"));
        project.setName(rs.getString("name"));
        project.setDescription(rs.getString("description"));

        Date startDate = rs.getDate("start_date");
        project.setStartDate(startDate != null ? startDate.toLocalDate() : null);

        Date endDate = rs.getDate("end_date");
        project.setEndDate(endDate != null ? endDate.toLocalDate() : null);

        project.setStatus(rs.getString("status"));

        return project;
    }
}
