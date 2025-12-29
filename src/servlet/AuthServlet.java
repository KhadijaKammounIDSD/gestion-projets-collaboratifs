package servlet;

import classes.Connect;
import classes.Member;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null) {
                sendError(response, 400, "Endpoint not specified");
                return;
            }

            switch (pathInfo) {
                case "/signup":
                    handleSignup(request, response);
                    break;
                case "/login":
                    handleLogin(request, response);
                    break;
                case "/logout":
                    handleLogout(request, response);
                    break;
                default:
                    sendError(response, 404, "Endpoint not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void handleSignup(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        try {
            // Read JSON from request
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            JsonObject jsonData = gson.fromJson(sb.toString(), JsonObject.class);
            
            String firstName = jsonData.get("firstName").getAsString();
            String lastName = jsonData.get("lastName").getAsString();
            String email = jsonData.get("email").getAsString();
            String password = jsonData.get("password").getAsString();
            String role = "Member";

            if (jsonData.has("role") && !jsonData.get("role").isJsonNull()) {
                role = jsonData.get("role").getAsString().trim();
            }

            Set<String> allowedRoles = new HashSet<>(Arrays.asList(
                "Member",
                "Developer",
                "Designer",
                "Tester",
                "QA Engineer",
                "DevOps",
                "Manager",
                "Project Manager",
                "Product Manager",
                "Scrum Master",
                "Tech Lead",
                "Architect",
                "Data Scientist",
                "Data Engineer",
                "Security Engineer",
                "Business Analyst",
                "Support Engineer",
                "UX/UI Designer",
                "Intern"
            ));

            if (role == null || role.isEmpty() || !allowedRoles.contains(role)) {
                role = "Member";
            }
            
            // Get skills if provided
            ArrayList<String> skills = new ArrayList<>();
            if (jsonData.has("skills") && jsonData.get("skills").isJsonArray()) {
                jsonData.get("skills").getAsJsonArray().forEach(
                    skill -> skills.add(skill.getAsString())
                );
            }

            Connection conn = Connect.getConnection();
            
            // Check if email already exists
            String checkSql = "SELECT id FROM member WHERE email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                sendError(response, 409, "Email already exists");
                return;
            }
            
            // Insert new member
            String insertSql = "INSERT INTO member (first_name, last_name, email, password, role, current_load, available) " +
                             "VALUES (?, ?, ?, ?, ?, 0.0, 1)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, firstName);
            insertStmt.setString(2, lastName);
            insertStmt.setString(3, email);
            insertStmt.setString(4, password); // In production, hash the password!
            insertStmt.setString(5, role);
            
            int affectedRows = insertStmt.executeUpdate();
            
            if (affectedRows == 0) {
                sendError(response, 500, "Creating user failed");
                return;
            }
            
            // Get generated ID
            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            int memberId = 0;
            if (generatedKeys.next()) {
                memberId = generatedKeys.getInt(1);
            }
            
            // Insert skills if provided
            if (!skills.isEmpty() && memberId > 0) {
                insertMemberSkills(conn, memberId, skills);
            }
            
            // Create response
            JsonObject responseObj = new JsonObject();
            responseObj.addProperty("success", true);
            responseObj.addProperty("message", "User registered successfully");
            responseObj.addProperty("memberId", memberId);
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(responseObj));
            
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, 500, "Signup failed: " + e.getMessage());
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        try {
            // Read JSON from request
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            JsonObject jsonData = gson.fromJson(sb.toString(), JsonObject.class);
            
            String email = jsonData.get("email").getAsString();
            String password = jsonData.get("password").getAsString();

            Connection conn = Connect.getConnection();
            
            // Check credentials
            String sql = "SELECT id, first_name, last_name, email, role, current_load, available " +
                        "FROM member WHERE email = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password); // In production, compare hashed passwords!
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Create session
                HttpSession session = request.getSession(true);
                int memberId = rs.getInt("id");
                session.setAttribute("memberId", memberId);
                session.setAttribute("email", rs.getString("email"));
                session.setAttribute("firstName", rs.getString("first_name"));
                session.setAttribute("lastName", rs.getString("last_name"));
                
                // Create response with member data
                Member member = new Member();
                member.setId(memberId);
                member.setFirstName(rs.getString("first_name"));
                member.setLastName(rs.getString("last_name"));
                member.setEmail(rs.getString("email"));
                member.setRole(rs.getString("role"));
                member.setCurrentLoad(rs.getDouble("current_load"));
                member.setAvailable(rs.getBoolean("available"));
                
                JsonObject responseObj = new JsonObject();
                responseObj.addProperty("success", true);
                responseObj.addProperty("message", "Login successful");
                responseObj.add("member", gson.toJsonTree(member));
                
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(responseObj));
            } else {
                sendError(response, 401, "Invalid email or password");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, 500, "Login failed: " + e.getMessage());
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        JsonObject responseObj = new JsonObject();
        responseObj.addProperty("success", true);
        responseObj.addProperty("message", "Logout successful");
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(responseObj));
    }

    private void insertMemberSkills(Connection conn, int memberId, ArrayList<String> skills) {
        try {
            for (String skillName : skills) {
                // First, get or create skill
                String getSkillSql = "SELECT id FROM skill WHERE name = ?";
                PreparedStatement getSkillStmt = conn.prepareStatement(getSkillSql);
                getSkillStmt.setString(1, skillName);
                ResultSet rs = getSkillStmt.executeQuery();
                
                int skillId = 0;
                if (rs.next()) {
                    skillId = rs.getInt("id");
                } else {
                    // Create skill
                    String createSkillSql = "INSERT INTO skill (name) VALUES (?)";
                    PreparedStatement createSkillStmt = conn.prepareStatement(createSkillSql, Statement.RETURN_GENERATED_KEYS);
                    createSkillStmt.setString(1, skillName);
                    createSkillStmt.executeUpdate();
                    
                    ResultSet generatedKeys = createSkillStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        skillId = generatedKeys.getInt(1);
                    }
                }
                
                // Insert member_skill with default level 3
                if (skillId > 0) {
                    String insertMemberSkillSql = "INSERT INTO member_skill (member_id, skill_id, level) VALUES (?, ?, 3)";
                    PreparedStatement insertMemberSkillStmt = conn.prepareStatement(insertMemberSkillSql);
                    insertMemberSkillStmt.setInt(1, memberId);
                    insertMemberSkillStmt.setInt(2, skillId);
                    insertMemberSkillStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sendError(HttpServletResponse response, int statusCode, String message) 
            throws IOException {
        JsonObject errorObj = new JsonObject();
        errorObj.addProperty("success", false);
        errorObj.addProperty("error", message);
        
        response.setStatus(statusCode);
        response.getWriter().write(gson.toJson(errorObj));
    }
}
