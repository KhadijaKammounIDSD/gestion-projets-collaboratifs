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
 * DAO pour la gestion des alertes dans la base de données
 */
public class AlertDAO {

    private Connection connection;

    public AlertDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Ajouter une nouvelle alerte
     */
    public boolean addAlert(Alert alert) {
        String sql = "INSERT INTO alert (type, message, issued_date, severity_level) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, alert.getType());
            ps.setString(2, alert.getMessage());
            ps.setDate(3, alert.getIssuedDate() != null ? Date.valueOf(alert.getIssuedDate()) : null);
            ps.setString(4, alert.getSeverityLevel());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        alert.setId(generatedKeys.getInt(1));
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
     * Récupérer une alerte par son ID
     */
    public Alert getAlertById(int id) {
        String sql = "SELECT * FROM alert WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractAlertFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupérer toutes les alertes
     */
    public List<Alert> getAllAlerts() {
        List<Alert> alerts = new ArrayList<>();
        String sql = "SELECT * FROM alert ORDER BY issued_date DESC";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                alerts.add(extractAlertFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alerts;
    }

    /**
     * Récupérer les alertes par niveau de sévérité
     */
    public List<Alert> getAlertsBySeverity(String severity) {
        List<Alert> alerts = new ArrayList<>();
        String sql = "SELECT * FROM alert WHERE severity_level = ? ORDER BY issued_date DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, severity);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    alerts.add(extractAlertFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alerts;
    }

    /**
     * Récupérer les alertes par type
     */
    public List<Alert> getAlertsByType(String type) {
        List<Alert> alerts = new ArrayList<>();
        String sql = "SELECT * FROM alert WHERE type = ? ORDER BY issued_date DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, type);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    alerts.add(extractAlertFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alerts;
    }

    /**
     * Récupérer les alertes récentes (derniers N jours)
     */
    public List<Alert> getRecentAlerts(int daysBack) {
        List<Alert> alerts = new ArrayList<>();
        String sql = "SELECT * FROM alert WHERE issued_date >= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
                "ORDER BY issued_date DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, daysBack);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    alerts.add(extractAlertFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alerts;
    }

    /**
     * Supprimer une alerte
     */
    public boolean deleteAlert(int id) {
        String sql = "DELETE FROM alert WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mettre à jour une alerte
     */
    public boolean updateAlert(Alert alert) {
        String sql = "UPDATE alert SET type = ?, message = ?, issued_date = ?, severity_level = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, alert.getType());
            ps.setString(2, alert.getMessage());
            ps.setDate(3, alert.getIssuedDate() != null ? Date.valueOf(alert.getIssuedDate()) : null);
            ps.setString(4, alert.getSeverityLevel());
            ps.setInt(5, alert.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Supprimer les anciennes alertes
     */
    public boolean deleteOldAlerts(int daysOld) {
        String sql = "DELETE FROM alert WHERE issued_date < DATE_SUB(CURDATE(), INTERVAL ? DAY)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, daysOld);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Compter les alertes par sévérité
     */
    public int countAlertsBySeverity(String severity) {
        String sql = "SELECT COUNT(*) FROM alert WHERE severity_level = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, severity);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Extraire une Alert depuis un ResultSet
     */
    private Alert extractAlertFromResultSet(ResultSet rs) throws SQLException {
        Alert alert = new Alert();
        alert.setId(rs.getInt("id"));
        alert.setType(rs.getString("type"));
        alert.setMessage(rs.getString("message"));

        Date issuedDate = rs.getDate("issued_date");
        alert.setIssuedDate(issuedDate != null ? issuedDate.toLocalDate() : null);

        alert.setSeverityLevel(rs.getString("severity_level"));

        return alert;
    }
}
