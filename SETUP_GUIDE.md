# Project Management Web Application - Setup Guide

## 🚀 Complete Authentication System

This web application now includes a complete authentication system with:
- ✅ User signup with skill selection
- ✅ User login with session management
- ✅ Form validation with alerts
- ✅ Password confirmation
- ✅ Automatic redirects
- ✅ Protected pages
- ✅ Database integration

---

## 📋 Prerequisites

1. **MySQL Server** (version 5.7 or higher)
2. **Java Development Kit (JDK)** (version 8 or higher)
3. **Apache Tomcat** (version 9.0 or higher)
4. **Maven** (for building the project)

---

## 🗄️ Database Setup

### Step 1: Create the Database

1. Open MySQL Workbench or command line:
```sql
mysql -u root -p
```

2. Run the main database script:
```sql
source d:/Semestre_3/Java/Java_TP/mini_projet/sql/database_complete.sql
```

### Step 2: Add Authentication Support

Run the authentication script to add password field:
```sql
source d:/Semestre_3/Java/Java_TP/mini_projet/sql/add_authentication.sql
```

### Step 3: Verify Database Connection

Update `src/classes/Connect.java` with your MySQL credentials:
```java
String url = "jdbc:mysql://localhost:3306/project_management?useSSL=false&serverTimezone=UTC";
String user = "root";
String password = "YOUR_MYSQL_PASSWORD"; // Change this!
```

---

## 🏗️ Build and Deploy

### Option 1: Using Maven (Recommended)

```bash
# Navigate to project directory
cd d:/Semestre_3/Java/Java_TP/mini_projet

# Clean and build
mvn clean package

# The WAR file will be created in target/ directory
# Copy it to Tomcat's webapps folder
copy target\project-management-1.0-SNAPSHOT.war C:\path\to\tomcat\webapps\project-management.war
```

### Option 2: Manual Build

1. Compile Java files:
```bash
javac -d build/classes -cp "lib/*" src/**/*.java
```

2. Create WAR file and deploy to Tomcat

---

## 🌐 Running the Application

### Step 1: Start Tomcat Server

```bash
# Windows
cd C:\path\to\tomcat\bin
startup.bat

# Linux/Mac
cd /path/to/tomcat/bin
./startup.sh
```

### Step 2: Access the Application

Open your browser and navigate to:
```
http://localhost:8080/project-management/
```

The application will automatically redirect to the login page.

---

## 🔐 Using the Application

### Sign Up (New Users)

1. Click on **"Sign Up"** button or go to: `http://localhost:8080/project-management/html/sign-up-page.html`

2. Fill in the required fields:
   - **First Name** (required)
   - **Last Name** (required)
   - **Email** (required, valid format)
   - **Password** (required, minimum 8 characters)
   - **Confirm Password** (required, must match)
   - **Skills** (optional)

3. Validation alerts will appear if:
   - Any required field is empty
   - Email format is invalid
   - Password is less than 8 characters
   - Passwords don't match

4. Upon successful signup:
   - Success message appears
   - Automatically redirects to login page after 2 seconds

### Log In (Existing Users)

1. Enter your email and password
2. Validation alerts will appear if fields are empty or invalid
3. Upon successful login:
   - User data is stored in session
   - Automatically redirects to dashboard
4. If credentials are wrong:
   - Error message displays
   - Password field is cleared

### Dashboard (Home Page)

After logging in, you'll have access to:
- Member management
- Task management
- Project management
- Timeline view
- Profile settings

---

## 📁 Project Structure

```
mini_projet/
├── sql/
│   ├── database_complete.sql       # Main database schema
│   └── add_authentication.sql      # Authentication support
├── src/
│   ├── classes/
│   │   ├── Connect.java            # Database connection
│   │   ├── Member.java             # Member model (with password)
│   │   └── ...
│   ├── dao/                        # Data Access Objects
│   ├── service/                    # Business logic
│   └── servlet/
│       ├── AuthServlet.java        # 🆕 Authentication endpoints
│       ├── MemberServlet.java
│       └── ...
├── frontend/
│   ├── html/
│   │   ├── login-page.html         # ✅ Updated with auth
│   │   ├── sign-up-page.html       # ✅ Updated with auth
│   │   ├── dashboard-page.html     # ✅ Protected page
│   │   └── ...
│   ├── js/
│   │   ├── auth.js                 # 🆕 Authentication logic
│   │   ├── auth-check.js           # 🆕 Page protection
│   │   ├── api-client.js           # API wrapper
│   │   ├── app.js                  # Main app logic
│   │   └── ...
│   └── css/
│       └── sign-up-page-style.css  # ✅ Includes alert styles
└── pom.xml                          # Maven configuration
```

---

## 🔌 API Endpoints

### Authentication API

#### Signup
```
POST /api/auth/signup
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123",
  "skills": ["Java", "JavaScript", "HTML"]
}

Response (201):
{
  "success": true,
  "message": "User registered successfully",
  "memberId": 1
}
```

#### Login
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}

Response (200):
{
  "success": true,
  "message": "Login successful",
  "member": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "role": "Member",
    ...
  }
}
```

#### Logout
```
POST /api/auth/logout

Response (200):
{
  "success": true,
  "message": "Logout successful"
}
```

---

## 🛡️ Security Notes

### ⚠️ IMPORTANT: Password Security

The current implementation stores passwords in **plain text** for development purposes. For production:

1. **Hash passwords** using BCrypt or similar:
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hashedPassword = encoder.encode(plainPassword);
```

2. Update `AuthServlet` to hash during signup and verify during login

3. Add HTTPS/SSL for encrypted communication

---

## ✨ Features Implemented

### ✅ Form Validation
- Required field checks
- Email format validation
- Password length validation (minimum 8 characters)
- Password confirmation match
- Real-time visual alerts

### ✅ User Feedback
- Success alerts (green)
- Error alerts (red)
- Auto-dismissing alerts after 5 seconds
- Manual close button
- Loading states on buttons

### ✅ Navigation Flow
- Index → Login Page (automatic)
- Sign Up → Login Page (after successful registration)
- Login → Dashboard (after successful login)
- Protected pages redirect to login if not authenticated

### ✅ Session Management
- User data stored in sessionStorage
- Login status tracking
- Auto-redirect for already logged-in users
- Logout functionality

### ✅ Database Integration
- Member table with password field
- Skill assignment during signup
- Email uniqueness validation
- Session-based authentication

---

## 🐛 Troubleshooting

### Database Connection Issues
```
Error: com.mysql.cj.jdbc.exceptions.CommunicationsException
Solution: Check MySQL server is running and credentials in Connect.java
```

### CORS Errors
```
Error: CORS policy blocking request
Solution: CorsFilter is already configured in servlet/CorsFilter.java
```

### 404 Errors
```
Error: 404 - Servlet not found
Solution: Verify Tomcat deployment and context path (/project-management)
```

### Session Issues
```
Error: Not redirecting after login
Solution: Check browser console for JavaScript errors
         Clear sessionStorage and try again
```

---

## 📝 Removed Files

The following unused JavaScript files were removed:
- ❌ `frontend/js/board.js` (not referenced in any HTML)
- ❌ `frontend/js/app-examples.js` (only contained example code)

---

## 🎯 Next Steps for Production

1. **Security Enhancements**
   - Implement password hashing (BCrypt)
   - Add CSRF protection
   - Implement rate limiting
   - Add SSL/HTTPS

2. **User Experience**
   - "Remember me" functionality
   - Password reset/forgot password
   - Email verification
   - Profile picture upload

3. **Performance**
   - Connection pooling
   - Cache management
   - Lazy loading

4. **Testing**
   - Unit tests for servlets
   - Integration tests
   - Frontend validation tests

---

## 📞 Support

If you encounter any issues:
1. Check the browser console for JavaScript errors
2. Check Tomcat logs: `tomcat/logs/catalina.out`
3. Verify database connection and table structure
4. Ensure all dependencies are in the classpath

---

## ✅ Summary

Your web application is now fully functional with:
- Complete authentication system
- Form validation and user feedback
- Seamless navigation flow
- Database integration
- Session management
- Protected pages

**Ready to go! 🚀**
