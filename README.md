# Collaborative Project Management System

> A Java servlet-based web application for team collaboration, project management, and intelligent task assignment with skill-based auto-assignment algorithm.

## 🎯 Features

- **Team Management** - Create and manage project teams
- **Member Management** - Add members with skills and availability tracking
- **Project & Task Management** - Organize projects and tasks with dependencies
- **Skill-Based Assignment** - Intelligent auto-assignment algorithm based on member skills and workload
- **Real-time Synchronization** - Automatic data refresh across browser tabs
- **Workload Tracking** - Monitor member availability and weekly hour limits
- **Alert System** - Notifications for overloaded members and unassigned tasks
- **Session Management** - Proper authentication and logout functionality
- **Dashboard Analytics** - View team statistics and project progress

## 🛠️ Tech Stack

### Backend
- **Java 8** - Core language
- **Apache Servlets** - RESTful API endpoints
- **JDBC** - Database access with DAO pattern
- **Gson** - JSON serialization
- **MySQL** - Database

### Frontend
- **HTML5 / CSS3** - Markup and styling
- **Vanilla JavaScript** - No frameworks, pure DOM manipulation
- **FontAwesome 6.4** - Icons

### Build & Deployment
- **Maven 3.x** - Build automation
- **Jetty 9.4** - Development server
- **Apache Tomcat** - Production server
- **WAR packaging** - Java web archive

## 📋 Prerequisites

- Java 8+
- Maven 3.6+
- MySQL 5.7+
- Tomcat 9+ (for production deployment)
- Modern web browser

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/mini-project-management.git
cd mini-projet
```

### 2. Setup Database
```bash
# Windows
setup_database.bat

# Or manually:
mysql -u root -p < sql/database_complete.sql
```

### 3. Build the Project
```bash
mvn clean package
```

### 4. Run with Jetty (Development)
```bash
mvn jetty:run
```

Access the application at: `http://localhost:8080/mini_projet/html/login-page.html`

### 5. Deploy to Tomcat (Production)
```bash
# Copy WAR file
cp target/project-management-1.0-SNAPSHOT.war $TOMCAT_HOME/webapps/mini_projet.war

# Start Tomcat
$TOMCAT_HOME/bin/startup.sh
```

## 📚 Documentation

- **[Setup Guide](docs/SETUP_GUIDE.md)** - Detailed installation instructions
- **[Testing Guide](docs/TESTING_GUIDE.md)** - Test scenarios and verification steps
- **[Deployment Guide](docs/DEPLOYMENT_GUIDE.md)** - Production deployment instructions
- **[Implementation Report](docs/IMPLEMENTATION_REPORT.md)** - Technical architecture and design decisions
- **[API Documentation](docs/API_DOCUMENTATION.md)** - REST API endpoints reference

## 🏗️ Project Structure

```
mini-projet/
├── src/                          # Java source code
│   ├── classes/                 # Entity classes (Member, Team, Task, etc.)
│   ├── dao/                     # Data Access Objects (CRUD operations)
│   ├── service/                 # Business logic (Task assignment, statistics)
│   └── servlet/                 # REST API endpoints
├── frontend/                    # Web application files
│   ├── html/                   # HTML pages (login, dashboard, settings, etc.)
│   ├── js/                     # JavaScript (api-client, app logic)
│   └── css/                    # Stylesheets
├── sql/                         # Database schemas and migrations
├── docs/                        # Documentation
├── pom.xml                      # Maven configuration
└── setup_database.bat          # Database initialization script
```

## 🔑 Key Components

### Backend Architecture

**DAO Pattern** - Single source of truth for database access:
- `MemberDAO`, `TeamDAO`, `ProjectDAO`, `TaskDAO`, `SkillDAO`
- Each DAO handles CRUD operations and complex queries
- Automatic workload calculation when tasks are assigned/unassigned

**Service Layer** - Business logic:
- `TaskAssignmentService` - Intelligent auto-assignment algorithm
- `StatisticsService` - Dashboard metrics and reporting
- `AlertService` - Alert generation for system events

**Servlets** - RESTful API:
- `@WebServlet("/api/*")` - All endpoints return JSON
- No request routing framework - pure servlet annotation
- CORS support via `CorsFilter`

### Frontend Architecture

**Single Page Application** - No framework:
- `app.js` - Main application logic (1500+ lines)
- `api-client.js` - API wrapper functions
- Direct DOM manipulation for updates
- `localStorage` for cross-tab synchronization

### Database Schema

**Key Tables:**
- `member` - System users with skills and workload
- `team` - Project teams with member assignments
- `project` - Projects with start/end dates
- `task` - Tasks with dependencies and assignments
- `skill` - Available skills in the system
- `member_skill` - Skills assigned to members with proficiency levels

## 🎯 Naming Convention (IMPORTANT)

**All JSON fields must use camelCase:**
```javascript
✅ CORRECT:
{ "firstName": "Alice", "currentLoad": 45.5, "teamId": 1 }

❌ WRONG (will break):
{ "first_name": "Alice", "current_load": 45.5, "team_id": 1 }
```

Database uses snake_case, but DAOs map to camelCase Java fields. Gson serializes to camelCase.

## 🔐 Default Credentials

After database setup, use these test accounts:
- **Email:** `kammounkhadija03@gmail.com` | **Password:** `123456kk`
- **Email:** `sh@gmail.com` | **Password:** `123456sh`

(Add more users via signup)

## 🤖 Task Assignment Algorithm

The system intelligently assigns tasks based on:
1. **Skill Matching** - Task required skills vs. member skills
2. **Workload Balance** - Current load vs. weekly availability (40 hours)
3. **Availability** - Member availability flag
4. **Task Priority** - High priority tasks assigned first
5. **Task Dependencies** - Respects task dependency chains

Algorithm location: `src/service/TaskAssignmentService.java`

## 🧪 Testing

Run the quick start testing guide:
```bash
# Follow the scenarios in docs/QUICK_START_TESTING.md
# Tests cover:
# - Team management and member assignments
# - Project and task creation
# - Auto-assignment functionality
# - Dashboard statistics
# - Alert generation
```

## 🐛 Troubleshooting

### "Loading..." appears but no data displays
1. Check browser console (F12) for errors
2. Verify API endpoints: `http://localhost:8080/mini_projet/api/members`
3. Ensure MySQL is running and accessible
4. Check database credentials in `src/classes/Connect.java`

### Build fails
```bash
mvn clean compile
# Check for compilation errors in output
```

### Jetty won't start
- Ensure port 8080 is not in use
- Check Java version: `java -version` (need 1.8+)
- Review Jetty logs for errors

## 📝 CVE Fixes

All critical and high-severity vulnerabilities have been patched:
- MySQL Connector upgraded to 8.3.0 (CVE-2023-22102 fixed)

## 🤝 Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines.

## 📄 License

This project is for educational purposes as part of a Java TP (Practical Work) course.

## 👥 Authors

- Khadija Kammoun
- And team members...

## 📞 Support

For issues and questions, please open a GitHub issue or contact the team.

---

**Last Updated:** December 29, 2025  
**Status:** ✅ Production Ready
