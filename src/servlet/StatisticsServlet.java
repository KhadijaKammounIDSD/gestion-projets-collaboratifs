package servlet;

import classes.Connect;
import service.StatisticsService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.util.Map;

/**
 * Servlet for statistics and reporting endpoints
 * API REST endpoints:
 * GET /api/statistics - Get all statistics
 * GET /api/statistics/project - Get project statistics
 * GET /api/statistics/workload - Get workload distribution
 * GET /api/statistics/skills - Get skill coverage
 * GET /api/statistics/assignments - Get assignment details
 * GET /api/statistics/timeline - Get timeline data
 * GET /api/statistics/report - Get complete report
 */
@WebServlet("/api/statistics/*")
public class StatisticsServlet extends HttpServlet {

    private Gson gson;

    @Override
    public void init() throws ServletException {
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .setPrettyPrinting()
                .create();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            Connection conn = Connect.getConnection();
            StatisticsService statsService = new StatisticsService(conn);

            Map<String, Object> result;

            if (pathInfo == null || "/".equals(pathInfo)) {
                // Get all statistics summary
                result = statsService.getProjectStatistics();
            } else if ("/project".equals(pathInfo)) {
                // Project statistics
                result = statsService.getProjectStatistics();
            } else if ("/workload".equals(pathInfo)) {
                // Workload distribution
                result = statsService.getWorkloadDistribution();
            } else if ("/skills".equals(pathInfo)) {
                // Skill coverage
                result = statsService.getSkillCoverage();
            } else if ("/assignments".equals(pathInfo)) {
                // Assignment details
                result = statsService.getAssignmentDetails();
            } else if ("/timeline".equals(pathInfo)) {
                // Timeline data
                result = statsService.getTimelineData();
            } else if ("/report".equals(pathInfo)) {
                // Complete report
                result = statsService.getCompleteReport();
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Endpoint non trouvé\"}");
                return;
            }

            response.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}
