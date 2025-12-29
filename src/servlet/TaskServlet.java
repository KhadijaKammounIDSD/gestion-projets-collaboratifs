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
 * Servlet pour gérer les opérations CRUD sur les tâches
 * API REST endpoints:
 * GET /api/tasks - Liste toutes les tâches
 * GET /api/tasks?id=X - Récupère une tâche par ID
 * POST /api/tasks - Crée une nouvelle tâche
 * PUT /api/tasks - Met à jour une tâche
 * DELETE /api/tasks?id=X - Supprime une tâche
 * POST /api/tasks/assign - Assigne une tâche à un membre
 */
@WebServlet("/api/tasks/*")
public class TaskServlet extends HttpServlet {

    private Gson gson;

    @Override
    public void init() throws ServletException {
        // Configurer Gson pour gérer les LocalDate
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
            TaskDAO taskDAO = new TaskDAO(conn);

            String pathInfo = request.getPathInfo();
            String idParam = request.getParameter("id");
            String statusParam = request.getParameter("status");
            String priorityParam = request.getParameter("priority");
            String memberIdParam = request.getParameter("memberId");
            String unassignedParam = request.getParameter("unassigned");

            if (idParam != null) {
                // Récupérer une tâche spécifique
                int id = Integer.parseInt(idParam);
                Task task = taskDAO.getTaskById(id);

                if (task != null) {
                    response.getWriter().write(gson.toJson(task));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"error\": \"Tâche non trouvée\"}");
                }
            } else if (unassignedParam != null && unassignedParam.equals("true")) {
                // Récupérer les tâches non assignées
                List<Task> tasks = taskDAO.getUnassignedTasks();
                response.getWriter().write(gson.toJson(tasks));
            } else if (statusParam != null) {
                // Récupérer les tâches par statut
                List<Task> tasks = taskDAO.getTasksByStatus(statusParam);
                response.getWriter().write(gson.toJson(tasks));
            } else if (priorityParam != null) {
                // Récupérer les tâches par priorité
                List<Task> tasks = taskDAO.getTasksByPriority(priorityParam);
                response.getWriter().write(gson.toJson(tasks));
            } else if (memberIdParam != null) {
                // Récupérer les tâches d'un membre
                int memberId = Integer.parseInt(memberIdParam);
                List<Task> tasks = taskDAO.getTasksByMember(memberId);
                response.getWriter().write(gson.toJson(tasks));
            } else {
                // Récupérer toutes les tâches
                List<Task> tasks = taskDAO.getAllTasks();
                response.getWriter().write(gson.toJson(tasks));
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

        String pathInfo = request.getPathInfo();

        try {
            Connection conn = Connect.getConnection();

            // Endpoint pour assigner une tâche manuellement (Scenario 4)
            if ("/assign".equals(pathInfo)) {
                String taskIdParam = request.getParameter("taskId");
                String memberIdParam = request.getParameter("memberId");

                if (taskIdParam == null || memberIdParam == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\": \"taskId et memberId requis\"}");
                    return;
                }

                int taskId = Integer.parseInt(taskIdParam);
                int memberId = Integer.parseInt(memberIdParam);

                TaskDAO taskDAO = new TaskDAO(conn);
                MemberDAO memberDAO = new MemberDAO(conn);
                AlertDAO alertDAO = new AlertDAO(conn);
                
                // Get task and member to calculate workload
                Task task = taskDAO.getTaskById(taskId);
                Member member = memberDAO.getMemberById(memberId);
                
                if (task == null || member == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"error\": \"Tâche ou membre non trouvé\"}");
                    return;
                }
                
                if (taskDAO.assignTaskToMember(taskId, memberId)) {
                    // Recalculate member workload from assigned tasks (single source of truth)
                    memberDAO.recalculateMemberWorkload(memberId);
                    
                    // Get updated member data for response
                    Member updatedMember = memberDAO.getMemberById(memberId);
                    double newLoad = updatedMember.getCurrentLoad();
                    double newRemainingHours = updatedMember.getRemainingHours();
                    boolean stillAvailable = updatedMember.isAvailable();
                    
                    // Check for overload (Scenario 4) - threshold is 160h or remaining < 0
                    boolean isOverloaded = newLoad > 160.0 || newRemainingHours < 0;
                    String alertMessage = null;
                    
                    if (isOverloaded) {
                        // Create overload alert
                        Alert alert = new Alert();
                        alert.setType("OVERLOAD");
                        alert.setMessage("⚠️ SURCHARGE: " + updatedMember.getFirstName() + " " + updatedMember.getLastName() + 
                            " est en surcharge après l'assignation de '" + task.getName() + 
                            "'. Charge: " + newLoad + "h, Heures restantes: " + newRemainingHours + "h");
                        alert.setIssuedDate(java.time.LocalDate.now());
                        alert.setSeverityLevel("HIGH");
                        alertDAO.addAlert(alert);
                        alertMessage = alert.getMessage();
                    }
                    
                    // Return detailed response
                    StringBuilder jsonResponse = new StringBuilder();
                    jsonResponse.append("{\"success\": true, \"message\": \"Tâche assignée\", ");
                    jsonResponse.append("\"memberLoad\": ").append(newLoad).append(", ");
                    jsonResponse.append("\"remainingHours\": ").append(newRemainingHours).append(", ");
                    jsonResponse.append("\"isOverloaded\": ").append(isOverloaded);
                    if (alertMessage != null) {
                        jsonResponse.append(", \"overloadAlert\": \"").append(alertMessage.replace("\"", "\\\"")).append("\"");
                    }
                    jsonResponse.append("}");
                    
                    response.getWriter().write(jsonResponse.toString());
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\": \"Impossible d'assigner la tâche\"}");
                }
            } else {
                // Créer une nouvelle tâche
                Task task = gson.fromJson(request.getReader(), Task.class);

                // Server-side validation: prevent impossible dates
                if (task.getPlannedStartDate() != null && task.getPlannedEndDate() != null
                        && task.getPlannedEndDate().isBefore(task.getPlannedStartDate())) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\": \"plannedEndDate cannot be before plannedStartDate\"}");
                    return;
                }

                TaskDAO taskDAO = new TaskDAO(conn);

                if (taskDAO.addTask(task)) {
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    response.getWriter().write(gson.toJson(task));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\": \"Impossible de créer la tâche\"}");
                }
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
            Task task = gson.fromJson(request.getReader(), Task.class);

            Connection conn = Connect.getConnection();
            TaskDAO taskDAO = new TaskDAO(conn);

            if (taskDAO.updateTask(task)) {
                response.getWriter().write(gson.toJson(task));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Tâche non trouvée\"}");
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
            TaskDAO taskDAO = new TaskDAO(conn);

            if (taskDAO.deleteTask(id)) {
                response.getWriter().write("{\"success\": true, \"message\": \"Tâche supprimée\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Tâche non trouvée\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
