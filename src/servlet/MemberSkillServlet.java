package servlet;

import classes.MemberSkill;
import classes.Skill;
import classes.Connect;
import dao.MemberSkillDAO;
import dao.SkillDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

/**
 * Servlet for Member Skill management
 * API REST endpoints:
 * GET /api/member-skills?memberId=X - Get skills for a member
 * GET /api/member-skills?id=X - Get a member skill by ID
 * POST /api/member-skills - Add a skill to a member
 * PUT /api/member-skills - Update a member skill
 * DELETE /api/member-skills?id=X - Remove a skill from a member
 */
@WebServlet("/api/member-skills")
public class MemberSkillServlet extends HttpServlet {

    private Gson gson;

    @Override
    public void init() throws ServletException {
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Connection conn = Connect.getConnection();
            MemberSkillDAO memberSkillDAO = new MemberSkillDAO(conn);
            SkillDAO skillDAO = new SkillDAO(conn);

            String memberIdParam = request.getParameter("memberId");
            String idParam = request.getParameter("id");

            if (memberIdParam != null) {
                // Get all skills for a member
                int memberId = Integer.parseInt(memberIdParam);
                List<MemberSkill> skills = memberSkillDAO.getSkillsByMember(memberId);
                
                // Enrich with skill details
                for (MemberSkill ms : skills) {
                    Skill skill = skillDAO.getSkillById(ms.getSkillId());
                    ms.setSkill(skill);
                }
                
                response.getWriter().write(gson.toJson(skills));
            } else if (idParam != null) {
                // Get a specific member skill
                int id = Integer.parseInt(idParam);
                MemberSkill memberSkill = memberSkillDAO.getMemberSkillById(id);
                
                if (memberSkill != null) {
                    Skill skill = skillDAO.getSkillById(memberSkill.getSkillId());
                    memberSkill.setSkill(skill);
                    response.getWriter().write(gson.toJson(memberSkill));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"error\": \"Member skill not found\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Missing parameters\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Connection conn = Connect.getConnection();
            MemberSkillDAO memberSkillDAO = new MemberSkillDAO(conn);
            SkillDAO skillDAO = new SkillDAO(conn);

            MemberSkill memberSkill = gson.fromJson(request.getReader(), MemberSkill.class);

            // Validate input
            if (memberSkill.getMemberId() <= 0 || memberSkill.getSkillId() <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid member ID or skill ID\"}");
                return;
            }

            if (memberSkill.getLevel() < 1 || memberSkill.getLevel() > 5) {
                memberSkill.setLevel(3); // Default level
            }

            // Check if skill already exists for this member
            List<MemberSkill> existingSkills = memberSkillDAO.getSkillsByMember(memberSkill.getMemberId());
            for (MemberSkill existing : existingSkills) {
                if (existing.getSkillId() == memberSkill.getSkillId()) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    response.getWriter().write("{\"error\": \"Skill already assigned to this member\"}");
                    return;
                }
            }

            if (memberSkillDAO.addMemberSkill(memberSkill)) {
                Skill skill = skillDAO.getSkillById(memberSkill.getSkillId());
                memberSkill.setSkill(skill);
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(gson.toJson(memberSkill));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Could not add skill\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Connection conn = Connect.getConnection();
            MemberSkillDAO memberSkillDAO = new MemberSkillDAO(conn);
            SkillDAO skillDAO = new SkillDAO(conn);

            MemberSkill memberSkill = gson.fromJson(request.getReader(), MemberSkill.class);

            // Validate level
            if (memberSkill.getLevel() < 1 || memberSkill.getLevel() > 5) {
                memberSkill.setLevel(3);
            }

            if (memberSkillDAO.updateMemberSkill(memberSkill)) {
                Skill skill = skillDAO.getSkillById(memberSkill.getSkillId());
                memberSkill.setSkill(skill);
                response.getWriter().write(gson.toJson(memberSkill));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Member skill not found\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Connection conn = Connect.getConnection();
            MemberSkillDAO memberSkillDAO = new MemberSkillDAO(conn);

            String idParam = request.getParameter("id");

            if (idParam == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Missing id parameter\"}");
                return;
            }

            int id = Integer.parseInt(idParam);

            if (memberSkillDAO.deleteMemberSkill(id)) {
                response.getWriter().write("{\"success\": true}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Member skill not found\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}
