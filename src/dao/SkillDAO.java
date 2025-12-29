package dao;

import classes.Alert;
import classes.Member;
import classes.Task;
import classes.Project;
import classes.Team;
import classes.Skill;
import classes.MemberSkill;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des compétences dans la base de données
 */
public class SkillDAO {

    private Connection connection;

    public SkillDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Ajouter une nouvelle compétence
     */
    public boolean addSkill(Skill skill) {
        String sql = "INSERT INTO skill (name) VALUES (?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, skill.getName());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        skill.setId(generatedKeys.getInt(1));
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
     * Récupérer une compétence par son ID
     */
    public Skill getSkillById(int id) {
        String sql = "SELECT * FROM skill WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractSkillFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupérer une compétence par son nom
     */
    public Skill getSkillByName(String name) {
        String sql = "SELECT * FROM skill WHERE name = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractSkillFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupérer toutes les compétences
     */
    public List<Skill> getAllSkills() {
        List<Skill> skills = new ArrayList<>();
        String sql = "SELECT * FROM skill";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                skills.add(extractSkillFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return skills;
    }

    /**
     * Mettre à jour une compétence
     */
    public boolean updateSkill(Skill skill) {
        String sql = "UPDATE skill SET name = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, skill.getName());
            ps.setInt(2, skill.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Supprimer une compétence
     */
    public boolean deleteSkill(int id) {
        String sql = "DELETE FROM skill WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Extraire une Skill depuis un ResultSet
     */
    private Skill extractSkillFromResultSet(ResultSet rs) throws SQLException {
        Skill skill = new Skill();
        skill.setId(rs.getInt("id"));
        skill.setName(rs.getString("name"));
        return skill;
    }
}
