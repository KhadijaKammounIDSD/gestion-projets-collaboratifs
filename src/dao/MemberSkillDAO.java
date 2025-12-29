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
 * DAO pour la gestion des associations membre-compétence dans la base de
 * données
 */
public class MemberSkillDAO {

    private Connection connection;

    public MemberSkillDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Ajouter une nouvelle association membre-compétence
     */
    public boolean addMemberSkill(MemberSkill memberSkill) {
        String sql = "INSERT INTO member_skill (member_id, skill_id, level) VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, memberSkill.getMemberId());
            ps.setInt(2, memberSkill.getSkillId());
            ps.setInt(3, memberSkill.getLevel());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        memberSkill.setId(generatedKeys.getInt(1));
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
     * Récupérer une association par son ID
     */
    public MemberSkill getMemberSkillById(int id) {
        String sql = "SELECT * FROM member_skill WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractMemberSkillFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupérer toutes les compétences d'un membre
     */
    public List<MemberSkill> getSkillsByMember(int memberId) {
        List<MemberSkill> skills = new ArrayList<>();
        String sql = "SELECT * FROM member_skill WHERE member_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, memberId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    skills.add(extractMemberSkillFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return skills;
    }

    /**
     * Récupérer tous les membres ayant une compétence donnée
     */
    public List<MemberSkill> getMembersBySkill(int skillId) {
        List<MemberSkill> members = new ArrayList<>();
        String sql = "SELECT * FROM member_skill WHERE skill_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, skillId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(extractMemberSkillFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    /**
     * Récupérer les membres ayant une compétence avec un niveau minimum
     */
    public List<MemberSkill> getMembersBySkillAndLevel(int skillId, int minLevel) {
        List<MemberSkill> members = new ArrayList<>();
        String sql = "SELECT * FROM member_skill WHERE skill_id = ? AND level >= ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, skillId);
            ps.setInt(2, minLevel);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(extractMemberSkillFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    /**
     * Mettre à jour une association membre-compétence
     */
    public boolean updateMemberSkill(MemberSkill memberSkill) {
        String sql = "UPDATE member_skill SET level = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, memberSkill.getLevel());
            ps.setInt(2, memberSkill.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mettre à jour le niveau d'une compétence
     */
    public boolean updateMemberSkillLevel(int id, int newLevel) {
        String sql = "UPDATE member_skill SET level = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, newLevel);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Supprimer une association membre-compétence
     */
    public boolean deleteMemberSkill(int id) {
        String sql = "DELETE FROM member_skill WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Supprimer toutes les compétences d'un membre
     */
    public boolean deleteAllSkillsOfMember(int memberId) {
        String sql = "DELETE FROM member_skill WHERE member_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Extraire un MemberSkill depuis un ResultSet
     */
    private MemberSkill extractMemberSkillFromResultSet(ResultSet rs) throws SQLException {
        MemberSkill memberSkill = new MemberSkill();
        memberSkill.setId(rs.getInt("id"));
        memberSkill.setMemberId(rs.getInt("member_id"));
        memberSkill.setSkillId(rs.getInt("skill_id"));
        memberSkill.setLevel(rs.getInt("level"));
        return memberSkill;
    }
}
