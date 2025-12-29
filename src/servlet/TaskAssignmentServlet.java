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
import service.*;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;

/**
 * Servlet pour l'affectation automatique des tâches
 * API REST endpoints:
 * POST /api/assignment/auto - Lance l'affectation automatique
 * POST /api/assignment/urgent - Affecte une tâche urgente
 * GET /api/assignment/report - Génère un rapport d'affectation
 */
@WebServlet("/api/assignment/*")
public class TaskAssignmentServlet extends HttpServlet {

    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            Connection conn = Connect.getConnection();
            MemberDAO memberDAO = new MemberDAO(conn);
            TaskDAO taskDAO = new TaskDAO(conn);

            if ("/auto".equals(pathInfo)) {
                // Affectation automatique de toutes les tâches non assignées
                // Use getAllMembersWithSkills to ensure skill matching works
                List<Member> members = memberDAO.getAllMembersWithSkills();
                List<Task> tasks = taskDAO.getUnassignedTasks();

                TaskAssignmentService service = new TaskAssignmentService(members, tasks);
                AssignmentResult result = service.assignTasksAutomatically();

                // Sauvegarder les affectations dans la base de données
                Set<Integer> affectedMemberIds = new HashSet<>();
                for (Map.Entry<Task, Member> entry : result.getSuccessfulAssignments().entrySet()) {
                    Task task = entry.getKey();
                    Member member = entry.getValue();
                    taskDAO.assignTaskToMember(task.getId(), member.getId());
                    affectedMemberIds.add(member.getId());
                }
                
                // Recalculate workload for all affected members (single source of truth)
                for (Integer memberId : affectedMemberIds) {
                    memberDAO.recalculateMemberWorkload(memberId);
                }

                // Sauvegarder les alertes
                AlertDAO alertDAO = new AlertDAO(conn);
                for (Alert alert : result.getAlerts()) {
                    alertDAO.addAlert(alert);
                }

                response.getWriter().write(gson.toJson(result));

            } else if ("/urgent".equals(pathInfo)) {
                // Affectation d'une tâche urgente (Scenario 5)
                String taskIdParam = request.getParameter("taskId");

                if (taskIdParam == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\": \"taskId requis\"}");
                    return;
                }

                int taskId = Integer.parseInt(taskIdParam);
                Task urgentTask = taskDAO.getTaskById(taskId);

                if (urgentTask == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"error\": \"Tâche non trouvée\"}");
                    return;
                }

                // Use getAllMembersWithSkills for skill matching
                List<Member> members = memberDAO.getAllMembersWithSkills();
                List<Task> tasks = taskDAO.getAllTasks();

                TaskAssignmentService service = new TaskAssignmentService(members, tasks);
                AssignmentResult result = service.reassignUrgentTask(urgentTask);

                // Sauvegarder l'affectation
                Set<Integer> affectedMemberIds = new HashSet<>();
                for (Map.Entry<Task, Member> entry : result.getSuccessfulAssignments().entrySet()) {
                    Task task = entry.getKey();
                    Member member = entry.getValue();
                    taskDAO.assignTaskToMember(task.getId(), member.getId());
                    affectedMemberIds.add(member.getId());
                }
                
                // Recalculate workload for all affected members (single source of truth)
                for (Integer memberId : affectedMemberIds) {
                    memberDAO.recalculateMemberWorkload(memberId);
                }

                // Sauvegarder les alertes
                AlertDAO alertDAO = new AlertDAO(conn);
                for (Alert alert : result.getAlerts()) {
                    alertDAO.addAlert(alert);
                }

                response.getWriter().write(gson.toJson(result));

            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Endpoint non trouvé\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            Connection conn = Connect.getConnection();

            if ("/report".equals(pathInfo)) {
                // Générer un rapport d'affectation
                MemberDAO memberDAO = new MemberDAO(conn);
                TaskDAO taskDAO = new TaskDAO(conn);

                List<Member> members = memberDAO.getAllMembers();
                List<Task> tasks = taskDAO.getAllTasks();

                TaskAssignmentService service = new TaskAssignmentService(members, tasks);
                String report = service.generateAssignmentReport();

                response.getWriter().write(gson.toJson(new ReportResponse(report)));

            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Endpoint non trouvé\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // Classe helper pour la réponse du rapport
    private static class ReportResponse {
        private String report;

        public ReportResponse(String report) {
            this.report = report;
        }

        public String getReport() {
            return report;
        }
    }
}
