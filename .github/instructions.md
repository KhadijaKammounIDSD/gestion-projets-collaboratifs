# Copilot Instructions - Project Management Web Application

## Architecture Overview

This is a **Java servlet-based web application** with vanilla JavaScript frontend for collaborative project management. The architecture follows a classic 3-tier pattern:

- **Backend**: Java servlets (DAO pattern) + MySQL database
- **Frontend**: Static HTML/CSS/JS files served by Tomcat
- **API**: RESTful endpoints at `/api/*` with JSON payloads

### Key Architectural Decisions

1. **No Framework**: Pure servlets + JDBC. No Spring, no Hibernate, no JSP.
2. **Frontend-Backend Separation**: Frontend in `frontend/`, backend in `src/`. Frontend calls backend via `fetch()`.
3. **DAO Pattern**: Each entity has a dedicated DAO class (`MemberDAO`, `TaskDAO`, etc.)
4. **Service Layer**: Complex business logic lives in `src/service/` (e.g., `TaskAssignmentService` for auto-assignment algorithm)
5. **Static Connection**: `Connect.java` maintains a single static database connection (initialized at class load)

## Critical Naming Convention

**⚠️ CRITICAL**: JSON field names MUST use camelCase, NOT snake_case:

```javascript
// ✅ CORRECT
{ "firstName": "Alice", "currentLoad": 45.5, "teamId": 1 }

// ❌ WRONG - will break serialization
{ "first_name": "Alice", "current_load": 45.5, "team_id": 1 }
```

**Why**: Gson (backend) uses `FieldNamingPolicy.IDENTITY` which expects camelCase to match Java field names. Database uses snake_case, but DAOs handle the mapping.

## Database Configuration

**Location**: `src/classes/Connect.java`
**Database**: `project_management` on MySQL localhost:3306
**Default credentials**: root/[empty password]

Tables follow snake_case naming (e.g., `member.current_load`), but DAOs map to camelCase Java fields.

## Development Workflow

### Building & Running

```bash
# Clean and package WAR file
mvn clean package

# Run with embedded Jetty (development)
mvn jetty:run

# Deploy to Tomcat (production)
# Copy target/project-management-1.0-SNAPSHOT.war to Tomcat webapps/
# Access at http://localhost:8080/mini_projet/
```

### Database Setup

```bash
# Windows
setup_database.bat

# Or manually in MySQL
mysql -u root -p < sql/database_complete.sql
```

### Testing Frontend Changes

Frontend files are in `frontend/`. For immediate testing:
1. Open `frontend/html/dashboard-page.html` directly in browser, OR
2. Deploy to Tomcat and access via `http://localhost:8080/mini_projet/frontend/html/dashboard-page.html`

**Important**: Frontend uses `API_BASE_URL = 'http://localhost:8080/mini_projet/api'` in `api-client.js`

## Key Components

### Real-time Data Synchronization

The application uses `localStorage` events to synchronize data changes across browser tabs/windows:
- When team/member/project data changes, broadcast via `localStorage.setItem('teamDataChanged', Date.now())`
- Pages listen for `storage` events and refresh automatically
- Each page exposes a global refresh function (e.g., `window.refreshTeamsPage`, `window.refreshSettingsPage`)

Example pattern:
```javascript
// After updating data
localStorage.setItem('teamDataChanged', Date.now().toString());

// In page initialization
window.addEventListener('storage', function(e) {
    if (e.key === 'teamDataChanged') {
        refreshPageData();
    }
});
```

### Servlets (RESTful API)

All in `src/servlet/`, annotated with `@WebServlet("/api/...")`:
- Handle HTTP methods: GET (read), POST (create), PUT (update), DELETE (remove)
- Return JSON via Gson
- Query parameters for filtering (e.g., `?id=5`, `?teamId=2`, `?available=true`)

Example pattern:
```java
@WebServlet("/api/members")
public class MemberServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        String idParam = request.getParameter("id");
        // If id provided, return single member; else return all
    }
}
```

### DAOs (Data Access Layer)

All in `src/dao/`. Each DAO:
- Takes `Connection` in constructor
- Maps ResultSet rows to Java objects (camelCase fields)
- Handles CRUD operations

**Critical method pattern**: `extractXFromResultSet(ResultSet rs)` maps DB columns (snake_case) to Java fields (camelCase)

### Services (Business Logic)

- `TaskAssignmentService`: Auto-assignment algorithm based on skills, workload, and availability
- `StatisticsService`: Dashboard metrics and reporting
- `AlertService`: Generate alerts for overloaded members, unassigned tasks

### Frontend Structure

- `frontend/html/*.html` - Page templates
- `frontend/js/api-client.js` - API wrapper functions (getAllMembers, createTask, etc.)
- `frontend/js/app.js` - Main application logic (1500+ lines)
- `frontend/css/` - Styles

**Pattern**: Each page loads data via `api-client.js` functions, manipulates DOM directly (no framework)

## Common Tasks

### Adding a New Entity

1. Create Java class in `src/classes/` with camelCase fields
2. Create DAO in `src/dao/` with CRUD methods
3. Create Servlet in `src/servlet/` with `@WebServlet` annotation
4. Add API wrapper in `frontend/js/api-client.js`
5. Create corresponding functions in `frontend/js/app.js`

### Adding a New Servlet Endpoint

```java
@WebServlet("/api/newentity")
public class NewEntityServlet extends HttpServlet {
    private Gson gson = new Gson();
    
    protected void doGet(...) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        // Use Connect.getConnection() for DB access
        Connection conn = Connect.getConnection();
        // ... DAO operations
        response.getWriter().write(gson.toJson(result));
    }
}
```

### Modifying Task Assignment Algorithm

Edit `src/service/TaskAssignmentService.java`:
- `findBestMemberForTask(Task)` - Scoring algorithm for member selection
- `assignTasksAutomatically()` - Main assignment workflow
- Considers: skill matching, workload balance, availability, task priority

## Troubleshooting

### "Loading..." appears but no data displays

1. Check browser console for CORS errors
2. Verify Tomcat is running and API accessible at `http://localhost:8080/mini_projet/api/members`
3. Check `Connect.java` database credentials
4. Verify `API_BASE_URL` in `api-client.js` matches your Tomcat context path

### JSON serialization errors

Ensure all JSON uses camelCase (see naming convention above). Check servlet logs for Gson exceptions.

### Database connection fails

Check `Connect.java` credentials and MySQL service status. Connection is initialized statically at class load.

## Project Context

This is a university project (Java TP Mini Projet) demonstrating:
- Servlet-based REST API development
- DAO pattern for database access
- Skill-based task assignment algorithm
- Team collaboration and workload management
- Authentication with sessions

**Notable features**:
- Automatic task assignment based on member skills and availability
- Real-time workload tracking with weekly hour limits
- Alert system for overloaded members
- Team management with member assignments
- Project and task timeline visualization
