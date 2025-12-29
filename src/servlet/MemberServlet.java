package servlet;

import classes.Alert;
import classes.Member;
import classes.Task;
import classes.Project;
import classes.Team;
import classes.Skill;
import classes.MemberSkill;
import classes.Connect;

import dao.*;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

/**
 * Servlet pour gérer les opérations CRUD sur les membres
 * API REST endpoints:
 * GET /api/members - Liste tous les membres
 * GET /api/members?id=X - Récupère un membre par ID
 * POST /api/members - Crée un nouveau membre
 * PUT /api/members - Met à jour un membre
 * DELETE /api/members?id=X - Supprime un membre
 */
@WebServlet("/api/members")
public class MemberServlet extends HttpServlet {

    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Connection conn = Connect.getConnection();
            MemberDAO memberDAO = new MemberDAO(conn);
            MemberSkillDAO memberSkillDAO = new MemberSkillDAO(conn);
            SkillDAO skillDAO = new SkillDAO(conn);

            String idParam = request.getParameter("id");
            String availableParam = request.getParameter("available");
            String teamIdParam = request.getParameter("teamId");

            if (idParam != null) {
                // Récupérer un membre spécifique
                int id = Integer.parseInt(idParam);
                Member member = memberDAO.getMemberById(id);

                if (member != null) {
                    // Load member skills with skill details
                    List<MemberSkill> memberSkills = memberSkillDAO.getSkillsByMember(id);
                    for (MemberSkill ms : memberSkills) {
                        Skill skill = skillDAO.getSkillById(ms.getSkillId());
                        ms.setSkill(skill);
                    }
                    member.setMemberSkills(new java.util.ArrayList<>(memberSkills));
                    
                    response.getWriter().write(gson.toJson(member));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"error\": \"Membre non trouvé\"}");
                }
            } else if (availableParam != null) {
                // Récupérer les membres disponibles
                List<Member> members = memberDAO.getAvailableMembers();
                
                // Load skills for each member
                for (Member member : members) {
                    List<MemberSkill> memberSkills = memberSkillDAO.getSkillsByMember(member.getId());
                    for (MemberSkill ms : memberSkills) {
                        Skill skill = skillDAO.getSkillById(ms.getSkillId());
                        ms.setSkill(skill);
                    }
                    member.setMemberSkills(new java.util.ArrayList<>(memberSkills));
                }
                
                response.getWriter().write(gson.toJson(members));
            } else if (teamIdParam != null) {
                // Récupérer les membres d'une équipe
                int teamId = Integer.parseInt(teamIdParam);
                List<Member> members = memberDAO.getMembersByTeam(teamId);
                
                // Load skills for each member
                for (Member member : members) {
                    List<MemberSkill> memberSkills = memberSkillDAO.getSkillsByMember(member.getId());
                    for (MemberSkill ms : memberSkills) {
                        Skill skill = skillDAO.getSkillById(ms.getSkillId());
                        ms.setSkill(skill);
                    }
                    member.setMemberSkills(new java.util.ArrayList<>(memberSkills));
                }
                
                response.getWriter().write(gson.toJson(members));
            } else {
                // Récupérer tous les membres
                List<Member> members = memberDAO.getAllMembers();
                
                // Load skills for each member
                for (Member member : members) {
                    List<MemberSkill> memberSkills = memberSkillDAO.getSkillsByMember(member.getId());
                    for (MemberSkill ms : memberSkills) {
                        Skill skill = skillDAO.getSkillById(ms.getSkillId());
                        ms.setSkill(skill);
                    }
                    member.setMemberSkills(new java.util.ArrayList<>(memberSkills));
                }
                
                response.getWriter().write(gson.toJson(members));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Lire le JSON du corps de la requête
            Member member = gson.fromJson(request.getReader(), Member.class);
            
            // Handle teamId if provided (converts teamId to Team object)
            Integer teamId = member.getTeamId();
            if (teamId != null && teamId > 0 && member.getTeam() == null) {
                Team team = new Team();
                team.setId(teamId);
                member.setTeam(team);
            }
            
            // Set default password if not provided
            if (member.getPassword() == null || member.getPassword().isEmpty()) {
                member.setPassword("password123"); // Default password
            }

            Connection conn = Connect.getConnection();
            MemberDAO memberDAO = new MemberDAO(conn);

            if (memberDAO.addMember(member)) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(gson.toJson(member));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Impossible de créer le membre\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Member memberUpdate = gson.fromJson(request.getReader(), Member.class);

            Connection conn = Connect.getConnection();
            MemberDAO memberDAO = new MemberDAO(conn);
            
            // Get existing member to preserve fields not being updated
            Member existingMember = memberDAO.getMemberById(memberUpdate.getId());
            if (existingMember == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Membre non trouvé\"}");
                return;
            }
            
            // Merge: update only provided fields, keep existing values for others
            existingMember.setFirstName(memberUpdate.getFirstName() != null ? memberUpdate.getFirstName() : existingMember.getFirstName());
            existingMember.setLastName(memberUpdate.getLastName() != null ? memberUpdate.getLastName() : existingMember.getLastName());
            existingMember.setEmail(memberUpdate.getEmail() != null ? memberUpdate.getEmail() : existingMember.getEmail());
            existingMember.setRole(memberUpdate.getRole() != null ? memberUpdate.getRole() : existingMember.getRole());
            // Password is not updated via this endpoint unless explicitly provided
            if (memberUpdate.getPassword() != null && !memberUpdate.getPassword().isEmpty()) {
                existingMember.setPassword(memberUpdate.getPassword());
            }
            // Team handling - check if teamId was provided in the JSON
            Integer newTeamId = memberUpdate.getTeamId();
            if (newTeamId != null) {
                if (newTeamId > 0) {
                    Team team = new Team();
                    team.setId(newTeamId);
                    existingMember.setTeam(team);
                } else {
                    existingMember.setTeam(null);
                }
            }

            if (memberDAO.updateMember(existingMember)) {
                response.getWriter().write(gson.toJson(existingMember));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Erreur lors de la mise à jour\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String idParam = request.getParameter("id");

            if (idParam == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"ID requis\"}");
                return;
            }

            int id = Integer.parseInt(idParam);
            Connection conn = Connect.getConnection();
            MemberDAO memberDAO = new MemberDAO(conn);

            if (memberDAO.deleteMember(id)) {
                response.getWriter().write("{\"success\": true, \"message\": \"Membre supprimé\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Membre non trouvé\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
