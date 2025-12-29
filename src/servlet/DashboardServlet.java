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
import java.util.*;

/**
 * Servlet pour le tableau de bord et les statistiques
 * API REST endpoints:
 * GET /api/dashboard/stats - Statistiques générales
 * GET /api/dashboard/workload - Répartition de la charge
 * GET /api/dashboard/alerts - Alertes actives
 * GET /api/dashboard/progress - Avancement du projet
 */
@WebServlet("/api/dashboard/*")
public class DashboardServlet extends HttpServlet {

    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            Connection conn = Connect.getConnection();

            if ("/stats".equals(pathInfo)) {
                // Statistiques générales
                DashboardStats stats = generateDashboardStats(conn);
                response.getWriter().write(gson.toJson(stats));

            } else if ("/workload".equals(pathInfo)) {
                // Répartition de la charge de travail
                WorkloadDistribution workload = calculateWorkloadDistribution(conn);
                response.getWriter().write(gson.toJson(workload));

            } else if ("/alerts".equals(pathInfo)) {
                // Alertes actives
                AlertDAO alertDAO = new AlertDAO(conn);
                List<Alert> alerts = alertDAO.getRecentAlerts(30); // 30 derniers jours
                response.getWriter().write(gson.toJson(alerts));

            } else if ("/progress".equals(pathInfo)) {
                // Avancement du projet
                ProjectProgress progress = calculateProjectProgress(conn);
                response.getWriter().write(gson.toJson(progress));

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

    /**
     * Génère les statistiques du tableau de bord
     */
    private DashboardStats generateDashboardStats(Connection conn) {
        DashboardStats stats = new DashboardStats();

        MemberDAO memberDAO = new MemberDAO(conn);
        TaskDAO taskDAO = new TaskDAO(conn);
        ProjectDAO projectDAO = new ProjectDAO(conn);
        AlertDAO alertDAO = new AlertDAO(conn);

        stats.totalMembers = memberDAO.getAllMembers().size();
        stats.availableMembers = memberDAO.getAvailableMembers().size();
        stats.totalTasks = taskDAO.getAllTasks().size();
        stats.unassignedTasks = taskDAO.getUnassignedTasks().size();
        stats.completedTasks = taskDAO.getTasksByStatus("Terminée").size();
        stats.totalProjects = projectDAO.getAllProjects().size();
        stats.activeProjects = projectDAO.getActiveProjects().size();
        stats.highPriorityAlerts = alertDAO.countAlertsBySeverity("Haute");

        return stats;
    }

    /**
     * Calcule la répartition de la charge de travail
     */
    private WorkloadDistribution calculateWorkloadDistribution(Connection conn) {
        WorkloadDistribution workload = new WorkloadDistribution();

        MemberDAO memberDAO = new MemberDAO(conn);
        List<Member> members = memberDAO.getAllMembers();

        double totalLoad = 0;
        double minLoad = Double.MAX_VALUE;
        double maxLoad = 0;

        workload.members = new ArrayList<>();

        for (Member member : members) {
            MemberWorkload mw = new MemberWorkload();
            mw.memberId = member.getId();
            mw.memberName = member.getFirstName() + " " + member.getLastName();
            mw.currentLoad = member.getCurrentLoad();
            mw.available = member.isAvailable();

            workload.members.add(mw);

            totalLoad += member.getCurrentLoad();
            minLoad = Math.min(minLoad, member.getCurrentLoad());
            maxLoad = Math.max(maxLoad, member.getCurrentLoad());
        }

        workload.averageLoad = members.isEmpty() ? 0 : totalLoad / members.size();
        workload.minLoad = minLoad == Double.MAX_VALUE ? 0 : minLoad;
        workload.maxLoad = maxLoad;

        // Calculer l'écart-type
        double variance = 0;
        for (Member member : members) {
            variance += Math.pow(member.getCurrentLoad() - workload.averageLoad, 2);
        }
        workload.standardDeviation = members.isEmpty() ? 0 : Math.sqrt(variance / members.size());

        // Déterminer si l'équilibre est bon
        workload.isBalanced = workload.standardDeviation < 30.0;

        return workload;
    }

    /**
     * Calcule l'avancement du projet
     */
    private ProjectProgress calculateProjectProgress(Connection conn) {
        ProjectProgress progress = new ProjectProgress();

        TaskDAO taskDAO = new TaskDAO(conn);
        List<Task> allTasks = taskDAO.getAllTasks();

        progress.totalTasks = allTasks.size();
        progress.completedTasks = 0;
        progress.inProgressTasks = 0;
        progress.plannedTasks = 0;
        progress.totalEstimatedHours = 0;
        progress.completedHours = 0;

        for (Task task : allTasks) {
            String status = task.getStatus();
            progress.totalEstimatedHours += task.getEstimatedDuration();

            if ("Terminée".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
                progress.completedTasks++;
                progress.completedHours += task.getEstimatedDuration();
            } else if ("En cours".equalsIgnoreCase(status) || "In Progress".equalsIgnoreCase(status)) {
                progress.inProgressTasks++;
            } else if ("Planifiée".equalsIgnoreCase(status) || "Planned".equalsIgnoreCase(status)) {
                progress.plannedTasks++;
            }
        }

        progress.completionPercentage = progress.totalTasks == 0 ? 0
                : (double) progress.completedTasks / progress.totalTasks * 100;

        progress.hoursCompletionPercentage = progress.totalEstimatedHours == 0 ? 0
                : progress.completedHours / progress.totalEstimatedHours * 100;

        return progress;
    }

    // Classes internes pour les réponses JSON

    private static class DashboardStats {
        int totalMembers;
        int availableMembers;
        int totalTasks;
        int unassignedTasks;
        int completedTasks;
        int totalProjects;
        int activeProjects;
        int highPriorityAlerts;
    }

    private static class WorkloadDistribution {
        List<MemberWorkload> members;
        double averageLoad;
        double minLoad;
        double maxLoad;
        double standardDeviation;
        boolean isBalanced;
    }

    private static class MemberWorkload {
        int memberId;
        String memberName;
        double currentLoad;
        boolean available;
    }

    private static class ProjectProgress {
        int totalTasks;
        int completedTasks;
        int inProgressTasks;
        int plannedTasks;
        double completionPercentage;
        double totalEstimatedHours;
        double completedHours;
        double hoursCompletionPercentage;
    }
}
