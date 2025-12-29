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
import com.google.gson.GsonBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

/**
 * Servlet pour gérer les opérations CRUD sur les projets
 * API REST endpoints:
 * GET /api/projects - Liste tous les projets
 * GET /api/projects?id=X - Récupère un projet par ID
 * POST /api/projects - Crée un nouveau projet
 * PUT /api/projects - Met à jour un projet
 * DELETE /api/projects?id=X - Supprime un projet
 */
@WebServlet("/api/projects")
public class ProjectServlet extends HttpServlet {

    private Gson gson;

    @Override
    public void init() throws ServletException {
        gson = new GsonBuilder()
                .registerTypeAdapter(java.time.LocalDate.class,
                        (com.google.gson.JsonSerializer<java.time.LocalDate>) (src, typeOfSrc,
                                context) -> src == null ? null : new com.google.gson.JsonPrimitive(src.toString()))
                .registerTypeAdapter(java.time.LocalDate.class,
                        (com.google.gson.JsonDeserializer<java.time.LocalDate>) (json, typeOfT,
                                context) -> json == null || json.isJsonNull() ? null
                                        : java.time.LocalDate.parse(json.getAsString()))
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
            ProjectDAO projectDAO = new ProjectDAO(conn);

            String idParam = request.getParameter("id");
            String statusParam = request.getParameter("status");

            if (idParam != null) {
                // Récupérer un projet spécifique
                int id = Integer.parseInt(idParam);
                Project project = projectDAO.getProjectById(id);

                if (project != null) {
                    response.getWriter().write(gson.toJson(project));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"error\": \"Projet non trouvé\"}");
                }
            } else if (statusParam != null) {
                // Récupérer les projets par statut
                List<Project> projects = projectDAO.getProjectsByStatus(statusParam);
                response.getWriter().write(gson.toJson(projects));
            } else {
                // Récupérer tous les projets
                List<Project> projects = projectDAO.getAllProjects();
                response.getWriter().write(gson.toJson(projects));
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
            Project project = gson.fromJson(request.getReader(), Project.class);

            Connection conn = Connect.getConnection();
            ProjectDAO projectDAO = new ProjectDAO(conn);

            if (projectDAO.addProject(project)) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(gson.toJson(project));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Impossible de créer le projet\"}");
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
            Project project = gson.fromJson(request.getReader(), Project.class);

            Connection conn = Connect.getConnection();
            ProjectDAO projectDAO = new ProjectDAO(conn);

            if (projectDAO.updateProject(project)) {
                response.getWriter().write(gson.toJson(project));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Projet non trouvé\"}");
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
            ProjectDAO projectDAO = new ProjectDAO(conn);

            if (projectDAO.deleteProject(id)) {
                response.getWriter().write("{\"success\": true, \"message\": \"Projet supprimé\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Projet non trouvé\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
