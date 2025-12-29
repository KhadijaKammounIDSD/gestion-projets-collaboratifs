package servlet;

import classes.Skill;
import classes.Connect;
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
 * Servlet for Skill CRUD operations
 * API REST endpoints:
 * GET /api/skills - List all skills
 * GET /api/skills?id=X - Get skill by ID
 * POST /api/skills - Create a new skill
 * PUT /api/skills - Update a skill
 * DELETE /api/skills?id=X - Delete a skill
 */
@WebServlet("/api/skills/*")
public class SkillServlet extends HttpServlet {

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
            SkillDAO skillDAO = new SkillDAO(conn);

            String idParam = request.getParameter("id");

            if (idParam != null) {
                // Get specific skill
                int id = Integer.parseInt(idParam);
                Skill skill = skillDAO.getSkillById(id);

                if (skill != null) {
                    response.getWriter().write(gson.toJson(skill));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"error\": \"Skill not found\"}");
                }
            } else {
                // Get all skills
                List<Skill> skills = skillDAO.getAllSkills();
                response.getWriter().write(gson.toJson(skills));
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
            Skill skill = gson.fromJson(request.getReader(), Skill.class);

            Connection conn = Connect.getConnection();
            SkillDAO skillDAO = new SkillDAO(conn);

            if (skillDAO.addSkill(skill)) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(gson.toJson(skill));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Unable to create skill\"}");
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
            Skill skill = gson.fromJson(request.getReader(), Skill.class);

            Connection conn = Connect.getConnection();
            SkillDAO skillDAO = new SkillDAO(conn);

            if (skillDAO.updateSkill(skill)) {
                response.getWriter().write(gson.toJson(skill));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Skill not found\"}");
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
            String idParam = request.getParameter("id");

            if (idParam == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"ID required\"}");
                return;
            }

            int id = Integer.parseInt(idParam);

            Connection conn = Connect.getConnection();
            SkillDAO skillDAO = new SkillDAO(conn);

            if (skillDAO.deleteSkill(id)) {
                response.getWriter().write("{\"success\": true, \"message\": \"Skill deleted\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Skill not found\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}
