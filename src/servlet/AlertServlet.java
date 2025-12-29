package servlet;

import classes.Alert;
import classes.Connect;
import dao.AlertDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

/**
 * Servlet for Alert CRUD operations
 * API REST endpoints:
 * GET /api/alerts - List all alerts
 * GET /api/alerts?id=X - Get alert by ID
 * GET /api/alerts?severity=X - Get alerts by severity
 * GET /api/alerts?type=X - Get alerts by type
 * POST /api/alerts - Create a new alert
 * PUT /api/alerts - Update an alert
 * DELETE /api/alerts?id=X - Delete an alert
 */
@WebServlet("/api/alerts/*")
public class AlertServlet extends HttpServlet {

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
            AlertDAO alertDAO = new AlertDAO(conn);

            String idParam = request.getParameter("id");
            String severityParam = request.getParameter("severity");
            String typeParam = request.getParameter("type");

            if (idParam != null) {
                // Get specific alert
                int id = Integer.parseInt(idParam);
                Alert alert = alertDAO.getAlertById(id);

                if (alert != null) {
                    response.getWriter().write(gson.toJson(alert));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"error\": \"Alert not found\"}");
                }
            } else if (severityParam != null) {
                // Get alerts by severity
                List<Alert> alerts = alertDAO.getAlertsBySeverity(severityParam);
                response.getWriter().write(gson.toJson(alerts));
            } else if (typeParam != null) {
                // Get alerts by type
                List<Alert> alerts = alertDAO.getAlertsByType(typeParam);
                response.getWriter().write(gson.toJson(alerts));
            } else {
                // Get all alerts
                List<Alert> alerts = alertDAO.getAllAlerts();
                response.getWriter().write(gson.toJson(alerts));
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
            Alert alert = gson.fromJson(request.getReader(), Alert.class);

            Connection conn = Connect.getConnection();
            AlertDAO alertDAO = new AlertDAO(conn);

            if (alertDAO.addAlert(alert)) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(gson.toJson(alert));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Unable to create alert\"}");
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
            Alert alert = gson.fromJson(request.getReader(), Alert.class);

            Connection conn = Connect.getConnection();
            AlertDAO alertDAO = new AlertDAO(conn);

            if (alertDAO.updateAlert(alert)) {
                response.getWriter().write(gson.toJson(alert));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Alert not found\"}");
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
            AlertDAO alertDAO = new AlertDAO(conn);

            if (alertDAO.deleteAlert(id)) {
                response.getWriter().write("{\"success\": true, \"message\": \"Alert deleted\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Alert not found\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}
