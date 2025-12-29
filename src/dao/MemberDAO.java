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
 * DAO pour la gestion des membres dans la base de données
 */
public class MemberDAO {

    private Connection connection;

    public MemberDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Ajouter un nouveau membre
     * Note: remaining_hours is always recalculated from weekly_availability - current_load
     */
    public boolean addMember(Member member) {
        // Recalculate remaining_hours from weekly_availability - current_load (no incremental updates)
        double remainingHours = member.getWeeklyAvailability() - member.getCurrentLoad();
        boolean available = remainingHours > 0;
        
        String sql = "INSERT INTO member (first_name, last_name, email, password, role, current_load, available, weekly_availability, remaining_hours, team_id) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, member.getFirstName());
            ps.setString(2, member.getLastName());
            ps.setString(3, member.getEmail());
            ps.setString(4, member.getPassword());
            ps.setString(5, member.getRole());
            ps.setDouble(6, member.getCurrentLoad());
            ps.setBoolean(7, available);
            ps.setDouble(8, member.getWeeklyAvailability());
            ps.setDouble(9, remainingHours); // Always recalculated
            ps.setObject(10, member.getTeam() != null ? member.getTeam().getId() : null);

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        member.setId(generatedKeys.getInt(1));
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
     * Récupérer un membre par son ID
     */
    public Member getMemberById(int id) {
        String sql = "SELECT * FROM member WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractMemberFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupérer tous les membres
     */
    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM member";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                members.add(extractMemberFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    /**
     * Mettre à jour un membre (preserves password if not provided)
     * Note: remaining_hours is always recalculated from weekly_availability - current_load
     */
    public boolean updateMember(Member member) {
        // Recalculate remaining_hours from weekly_availability - current_load (no incremental updates)
        double remainingHours = member.getWeeklyAvailability() - member.getCurrentLoad();
        boolean available = remainingHours > 0;
        
        // If password is null or empty, don't update it
        String sql;
        if (member.getPassword() == null || member.getPassword().isEmpty()) {
            sql = "UPDATE member SET first_name = ?, last_name = ?, email = ?, role = ?, " +
                    "current_load = ?, available = ?, weekly_availability = ?, remaining_hours = ?, team_id = ? WHERE id = ?";
        } else {
            sql = "UPDATE member SET first_name = ?, last_name = ?, email = ?, password = ?, role = ?, " +
                    "current_load = ?, available = ?, weekly_availability = ?, remaining_hours = ?, team_id = ? WHERE id = ?";
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int paramIndex = 1;
            ps.setString(paramIndex++, member.getFirstName());
            ps.setString(paramIndex++, member.getLastName());
            ps.setString(paramIndex++, member.getEmail());
            
            if (member.getPassword() != null && !member.getPassword().isEmpty()) {
                ps.setString(paramIndex++, member.getPassword());
            }
            
            ps.setString(paramIndex++, member.getRole());
            ps.setDouble(paramIndex++, member.getCurrentLoad());
            ps.setBoolean(paramIndex++, available);
            ps.setDouble(paramIndex++, member.getWeeklyAvailability());
            ps.setDouble(paramIndex++, remainingHours); // Always recalculated
            ps.setObject(paramIndex++, member.getTeam() != null ? member.getTeam().getId() : null);
            ps.setInt(paramIndex++, member.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Supprimer un membre
     */
    public boolean deleteMember(int id) {
        String sql = "DELETE FROM member WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Récupérer les membres disponibles
     */
    public List<Member> getAvailableMembers() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM member WHERE available = 1";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                members.add(extractMemberFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    /**
     * Récupérer les membres par équipe
     */
    public List<Member> getMembersByTeam(int teamId) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM member WHERE team_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, teamId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(extractMemberFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    /**
     * Récupérer les membres avec une charge inférieure à un seuil
     */
    public List<Member> getMembersWithLoadBelow(double maxLoad) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM member WHERE current_load < ? AND available = 1";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, maxLoad);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(extractMemberFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    /**
     * Mettre à jour la charge d'un membre
     */
    public boolean updateMemberLoad(int memberId, double newLoad) {
        String sql = "UPDATE member SET current_load = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, newLoad);
            ps.setInt(2, memberId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Extraire un Member depuis un ResultSet
     */
    private Member extractMemberFromResultSet(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setId(rs.getInt("id"));
        member.setFirstName(rs.getString("first_name"));
        member.setLastName(rs.getString("last_name"));
        member.setEmail(rs.getString("email"));
        member.setPassword(rs.getString("password"));
        member.setRole(rs.getString("role"));
        member.setCurrentLoad(rs.getDouble("current_load"));
        member.setAvailable(rs.getBoolean("available"));
        // Capture the team reference so frontend can display it without another lookup
        int teamId = rs.getInt("team_id");
        if (!rs.wasNull()) {
            member.setTeamId(teamId);
        }
        
        // New availability tracking fields
        member.setWeeklyAvailability(rs.getDouble("weekly_availability"));
        member.setRemainingHours(rs.getDouble("remaining_hours"));

        // La team sera chargée séparément si nécessaire
        return member;
    }
    
    /**
     * Update member availability after task assignment
     */
    public boolean updateMemberAvailability(int memberId, double remainingHours, boolean available) {
        String sql = "UPDATE member SET remaining_hours = ?, available = ? WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, remainingHours);
            ps.setBoolean(2, available);
            ps.setInt(3, memberId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Reset weekly availability for all members
     * Note: remaining_hours is recalculated from weekly_availability - current_load
     */
    public boolean resetAllMembersWeeklyAvailability() {
        String sql = "UPDATE member SET remaining_hours = weekly_availability - current_load, " +
                     "available = CASE WHEN (weekly_availability - current_load) > 0 THEN 1 ELSE 0 END";
        
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(sql) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get all members with their skills fully loaded (including Skill objects)
     * This is critical for the auto-assignment algorithm
     */
    public List<Member> getAllMembersWithSkills() {
        List<Member> members = getAllMembers();
        MemberSkillDAO memberSkillDAO = new MemberSkillDAO(connection);
        SkillDAO skillDAO = new SkillDAO(connection);
        
        for (Member member : members) {
            List<MemberSkill> memberSkills = memberSkillDAO.getSkillsByMember(member.getId());
            // Load the Skill object for each MemberSkill
            for (MemberSkill ms : memberSkills) {
                Skill skill = skillDAO.getSkillById(ms.getSkillId());
                ms.setSkill(skill);
            }
            member.setMemberSkills(new ArrayList<>(memberSkills));
        }
        
        return members;
    }

    /**
     * Get a single member with skills fully loaded
     */
    public Member getMemberByIdWithSkills(int id) {
        Member member = getMemberById(id);
        if (member != null) {
            MemberSkillDAO memberSkillDAO = new MemberSkillDAO(connection);
            SkillDAO skillDAO = new SkillDAO(connection);
            
            List<MemberSkill> memberSkills = memberSkillDAO.getSkillsByMember(member.getId());
            for (MemberSkill ms : memberSkills) {
                Skill skill = skillDAO.getSkillById(ms.getSkillId());
                ms.setSkill(skill);
            }
            member.setMemberSkills(new ArrayList<>(memberSkills));
        }
        return member;
    }

    /**
     * Get members with tasks assigned to them
     */
    public List<Member> getMembersWithAssignedTasks() {
        List<Member> membersWithTasks = new ArrayList<>();
        String sql = "SELECT DISTINCT m.* FROM member m " +
                     "INNER JOIN task t ON t.assignee_id = m.id";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                membersWithTasks.add(extractMemberFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return membersWithTasks;
    }
    
    /**
     * Recalculate member workload from assigned tasks (single source of truth)
     * This ensures workload = sum of durations of assigned tasks
     */
    public boolean recalculateMemberWorkload(int memberId) {
        String sql = "SELECT COALESCE(SUM(estimated_duration), 0) as total_load " +
                     "FROM task WHERE assignee_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double calculatedLoad = rs.getDouble("total_load");
                    
                    // Get current member to calculate remaining hours
                    Member member = getMemberById(memberId);
                    if (member == null) {
                        return false;
                    }
                    
                    // Calculate remaining hours based on weekly availability
                    double weeklyAvailability = member.getWeeklyAvailability();
                    double remainingHours = weeklyAvailability - calculatedLoad;
                    boolean available = remainingHours > 0;
                    
                    // Update both current_load and availability
                    String updateSql = "UPDATE member SET current_load = ?, remaining_hours = ?, available = ? WHERE id = ?";
                    try (PreparedStatement updatePs = connection.prepareStatement(updateSql)) {
                        updatePs.setDouble(1, calculatedLoad);
                        updatePs.setDouble(2, remainingHours);
                        updatePs.setBoolean(3, available);
                        updatePs.setInt(4, memberId);
                        return updatePs.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
