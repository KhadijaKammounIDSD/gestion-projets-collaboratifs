# Project Setup and Quick Reference

## 🚀 For New Developers

### Clone and Setup (First Time)
```bash
# 1. Clone repository
git clone https://github.com/yourusername/mini-projet.git
cd mini-projet

# 2. Setup database
# Windows:
setup_database.bat

# Linux/Mac:
mysql -u root -p < sql/database_complete.sql

# 3. Build project
mvn clean package

# 4. Run locally
mvn jetty:run

# 5. Open browser
# http://localhost:8080/mini_projet/html/login-page.html
```

## 📝 Default Test Accounts

After database setup, login with:
- Email: `kammounkhadija03@gmail.com` | Password: `123456kk`
- Email: `sh@gmail.com` | Password: `123456sh`

(Additional users can be created via signup)

## 📚 Documentation Quick Links

- **Getting Started** → `docs/SETUP_GUIDE.md`
- **Testing the App** → `docs/TESTING_GUIDE.md`
- **API Reference** → `docs/API_DOCUMENTATION.md`
- **Deployment** → `docs/DEPLOYMENT_GUIDE.md`
- **Technical Details** → `docs/IMPLEMENTATION_REPORT.md`
- **Contributing** → `CONTRIBUTING.md`

## 🔑 Key Technologies

| Layer | Tech | Version |
|-------|------|---------|
| Backend | Java | 1.8+ |
| Build | Maven | 3.6+ |
| Dev Server | Jetty | 9.4.53 |
| Prod Server | Tomcat | 9+ |
| Database | MySQL | 5.7+ |
| Frontend | HTML5/CSS3 | - |
| Frontend | Vanilla JS | ES6+ |
| Database Driver | MySQL Connector/J | 8.3.0 |

## 📂 Project Structure at a Glance

```
Backend:
  src/classes/     → Entity models (Member, Team, Task, etc.)
  src/dao/         → Database access objects
  src/service/     → Business logic
  src/servlet/     → REST API endpoints

Frontend:
  frontend/html/   → Web pages
  frontend/js/     → JavaScript logic
  frontend/css/    → Styling

Database:
  sql/             → SQL scripts
```

## 🎯 Architecture Highlights

- **No ORM** - Pure JDBC with DAO pattern
- **No Frontend Framework** - Vanilla JS, direct DOM manipulation
- **Stateless API** - REST endpoints return JSON
- **Session-based Auth** - HttpSession for authenticated users
- **Real-time Sync** - localStorage events for cross-tab updates
- **Smart Assignment** - Skill-based auto-assignment algorithm

## 🔐 Important Notes

### 1. JSON Field Names
Always use **camelCase**, NOT snake_case:
```javascript
✅ firstName, currentLoad, teamId
❌ first_name, current_load, team_id
```

### 2. API Base URL
Frontend configured for:
```javascript
http://localhost:8080/mini_projet/api
```

### 3. Database Credentials
Check `src/classes/Connect.java` for database connection settings.

## 🧪 Running Tests

```bash
# Unit tests (if configured)
mvn test

# Integration testing steps in:
docs/TESTING_GUIDE.md
docs/QUICK_START_TESTING.md
```

## 🐛 Common Commands

```bash
# Clean build
mvn clean package

# Run with Jetty
mvn jetty:run

# Run only tests
mvn test

# Skip tests during build
mvn clean package -DskipTests

# View dependencies
mvn dependency:tree

# Generate Java docs
mvn javadoc:javadoc
```

## 🔍 Troubleshooting

### Port 8080 already in use
```bash
# Find and kill process on port 8080
# Windows:
netstat -ano | findstr :8080
taskkill /PID [PID] /F

# Linux/Mac:
lsof -i :8080
kill -9 [PID]
```

### Database connection fails
1. Verify MySQL is running
2. Check credentials in `src/classes/Connect.java`
3. Ensure database exists: `mysql -u root -p < sql/database_complete.sql`

### Build compilation errors
```bash
# Clean and rebuild
mvn clean compile

# Check for Java version
java -version
```

### Frontend not updating
1. Hard refresh browser: `Ctrl+Shift+R`
2. Clear browser cache: `Ctrl+Shift+Delete`
3. Check browser console for JavaScript errors: `F12`

## 📊 Key Features Quick Reference

| Feature | Location | How to Access |
|---------|----------|---------------|
| Team Management | Teams page | `/html/teams-page.html` |
| Member Management | Members page | `/html/members-page.html` |
| Project Management | Projects page | `/html/projects-page.html` |
| Task Management | Tasks page | `/html/tasks-page.html` |
| Auto-Assignment | Tasks page | "Auto-Assign All Tasks" button |
| Settings | Settings page | `/html/settings-page.html` |
| Dashboard | Dashboard | `/html/dashboard-page.html` |
| User Profile | Profile | `/html/profile-page.html` |

## 🔄 Development Workflow

1. **Make changes** to Java code in `src/`
2. **Rebuild** with `mvn clean package`
3. **Restart** Jetty (Ctrl+C, then `mvn jetty:run`)
4. **Hard refresh** browser (Ctrl+Shift+R)
5. **Check console** (F12) for errors
6. **Test functionality** following scenario guides

## 📤 Deploying to Tomcat (Production)

```bash
# 1. Build project
mvn clean package

# 2. Copy WAR file
cp target/project-management-1.0-SNAPSHOT.war $TOMCAT_HOME/webapps/mini_projet.war

# 3. Start Tomcat
$TOMCAT_HOME/bin/startup.sh

# 4. Access application
# http://yourserver:8080/mini_projet/html/login-page.html
```

## 🤝 Contributing

1. Read `CONTRIBUTING.md`
2. Create feature branch: `git checkout -b feature/your-feature`
3. Make changes and test thoroughly
4. Commit with clear messages: `git commit -m "feat: description"`
5. Push and create Pull Request

## 📞 Need Help?

- Check documentation in `docs/` folder
- Review `CONTRIBUTING.md` for coding standards
- Check `docs/API_DOCUMENTATION.md` for API details
- Review existing code for patterns and examples

---

**Happy coding!** 🎉

---

**Last Updated:** December 29, 2025
