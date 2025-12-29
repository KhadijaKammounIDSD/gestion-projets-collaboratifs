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
 * DAO pour la gestion des équipes dans la base de données
 */
public class TeamDAO {

    private Connection connection;

    public TeamDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Ajouter une nouvelle équipe
     */
    public boolean addTeam(Team team) {
        String sql = "INSERT INTO team (name) VALUES (?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, team.getName());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        team.setId(generatedKeys.getInt(1));
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
     * Récupérer une équipe par son ID
     */
    public Team getTeamById(int id) {
        String sql = "SELECT * FROM team WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractTeamFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupérer toutes les équipes
     */
    public List<Team> getAllTeams() {
        List<Team> teams = new ArrayList<>();
        String sql = "SELECT * FROM team";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                teams.add(extractTeamFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teams;
    }

    /**
     * Mettre à jour une équipe
     */
    public boolean updateTeam(Team team) {
        String sql = "UPDATE team SET name = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, team.getName());
            ps.setInt(2, team.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Supprimer une équipe
     */
    public boolean deleteTeam(int id) {
        String sql = "DELETE FROM team WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Récupérer une équipe avec ses membres
     */
    public Team getTeamWithMembers(int teamId) {
        Team team = getTeamById(teamId);

        if (team != null) {
            MemberDAO memberDAO = new MemberDAO(connection);
            List<Member> members = memberDAO.getMembersByTeam(teamId);
            team.setMembers(new ArrayList<>(members));
        }

        return team;
    }

    /**
     * Extraire une Team depuis un ResultSet
     */
    private Team extractTeamFromResultSet(ResultSet rs) throws SQLException {
        Team team = new Team();
        team.setId(rs.getInt("id"));
        team.setName(rs.getString("name"));
        return team;
    }
}
