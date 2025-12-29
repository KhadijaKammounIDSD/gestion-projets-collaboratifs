@echo off
REM Database Setup Script for Project Management Application
REM This script will create and configure the MySQL database

echo ============================================================
echo  Project Management - Database Setup
echo ============================================================
echo.

REM Check if MySQL is accessible
mysql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: MySQL is not installed or not in PATH
    echo Please install MySQL or add it to your PATH environment variable
    pause
    exit /b 1
)

echo MySQL found!
echo.

REM Get MySQL credentials
set /p MYSQL_USER="Enter MySQL username (default: root): "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

echo.
echo Enter MySQL password (will not be displayed):
set /p MYSQL_PASSWORD=

echo.
echo ============================================================
echo  Step 1: Creating database schema
echo ============================================================
echo.

mysql -u %MYSQL_USER% -p%MYSQL_PASSWORD% < sql\database_complete.sql
if %errorlevel% neq 0 (
    echo ERROR: Failed to create database schema
    pause
    exit /b 1
)

echo Database schema created successfully!
echo.

echo ============================================================
echo  Step 2: Adding authentication support
echo ============================================================
echo.

mysql -u %MYSQL_USER% -p%MYSQL_PASSWORD% < sql\add_authentication.sql
if %errorlevel% neq 0 (
    echo ERROR: Failed to add authentication support
    pause
    exit /b 1
)

echo Authentication support added successfully!
echo.

echo ============================================================
echo  Database Setup Complete!
echo ============================================================
echo.
echo Next steps:
echo 1. Update src\classes\Connect.java with your MySQL password
echo 2. Build the project: mvn clean package
echo 3. Deploy to Tomcat
echo 4. Access: http://localhost:8080/project-management/
echo.
echo For detailed instructions, see SETUP_GUIDE.md
echo.

pause
