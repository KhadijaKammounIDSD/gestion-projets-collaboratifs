package servlet;

import classes.Team;
import classes.Member;
import classes.Connect;
import dao.TeamDAO;
import dao.MemberDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet for Team CRUD operations
 * API REST endpoints:
 * GET /api/teams - List all teams
 * GET /api/teams?id=X - Get team by ID
 * GET /api/teams/X/members - Get team members
 * POST /api/teams - Create a new team
 * PUT /api/teams - Update a team
 * DELETE /api/teams?id=X - Delete a team
 */
@WebServlet(urlPatterns = {"/api/teams", "/api/teams/*"})
public class TeamServlet extends HttpServlet {

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
            TeamDAO teamDAO = new TeamDAO(conn);
            MemberDAO memberDAO = new MemberDAO(conn);

            String pathInfo = request.getPathInfo();
            String idParam = request.getParameter("id");

            if (pathInfo != null && pathInfo.contains("/members")) {
                // Get members of a specific team
                String[] parts = pathInfo.split("/");
                if (parts.length >= 2) {
                    int teamId = Integer.parseInt(parts[1]);
                    List<Member> members = memberDAO.getMembersByTeam(teamId);
                    response.getWriter().write(gson.toJson(members));
                }
            } else if (idParam != null) {
                // Get specific team
                int id = Integer.parseInt(idParam);
                Team team = teamDAO.getTeamById(id);

                if (team != null) {
                    // Get member count for this team
                    List<Member> members = memberDAO.getMembersByTeam(id);
                    Map<String, Object> teamWithCount = new HashMap<>();
                    teamWithCount.put("id", team.getId());
                    teamWithCount.put("name", team.getName());
                    teamWithCount.put("memberCount", members.size());
                    teamWithCount.put("members", members);
                    response.getWriter().write(gson.toJson(teamWithCount));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"error\": \"Team not found\"}");
                }
            } else {
                // Get all teams with member counts
                List<Team> teams = teamDAO.getAllTeams();
                List<Map<String, Object>> teamsWithCounts = new java.util.ArrayList<>();
                
                for (Team team : teams) {
                    List<Member> members = memberDAO.getMembersByTeam(team.getId());
                    Map<String, Object> teamData = new HashMap<>();
                    teamData.put("id", team.getId());
                    teamData.put("name", team.getName());
                    teamData.put("memberCount", members.size());
                    teamData.put("members", members);
                    teamsWithCounts.add(teamData);
                }
                
                response.getWriter().write(gson.toJson(teamsWithCounts));
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
            Team team = gson.fromJson(request.getReader(), Team.class);

            Connection conn = Connect.getConnection();
            TeamDAO teamDAO = new TeamDAO(conn);

            if (teamDAO.addTeam(team)) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(gson.toJson(team));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Unable to create team\"}");
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
            Team team = gson.fromJson(request.getReader(), Team.class);

            Connection conn = Connect.getConnection();
            TeamDAO teamDAO = new TeamDAO(conn);

            if (teamDAO.updateTeam(team)) {
                response.getWriter().write(gson.toJson(team));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Team not found\"}");
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
            TeamDAO teamDAO = new TeamDAO(conn);

            if (teamDAO.deleteTeam(id)) {
                response.getWriter().write("{\"success\": true, \"message\": \"Team deleted\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Team not found\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}
