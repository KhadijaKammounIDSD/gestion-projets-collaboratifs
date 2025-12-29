# Quick Start Testing Guide

## ⚡ Quick Setup (3 Steps)

### 1. Update Database
```bash
mysql -u root -p project_management < sql/add_project_to_task.sql
```

### 2. Restart Server
```bash
# Press Ctrl+C to stop
mvn jetty:run
```

### 3. Open Browser
```
http://localhost:8080/mini_projet/html/login-page.html
```

---

## ✅ Testing Sequence

### Test 1: Create a Project
1. Navigate to **Projects** page
2. Click **"Create Project"** button
3. Fill in:
   - Name: `Website Redesign`
   - Description: `Redesign company website`
   - Start: Today's date
   - End: 30 days from now
4. Click **Create**
5. ✅ Project should appear in the list

### Test 2: Try Creating Task Without Project (Should Fail)
1. Navigate to **Tasks** page
2. Click **"Create Task"** button
3. **Skip** the project dropdown
4. Fill in task name: `Design Homepage`
5. Fill in duration: `5`
6. Click **Create**
7. ❌ Error should appear: *"Please select a project..."*

### Test 3: Create Task With Project (Should Work)
1. Click **"Create Task"** button again
2. **Select** `Website Redesign` from project dropdown
3. Fill in:
   - Name: `Design Homepage`
   - Description: `Create mockups for homepage`
   - Duration: `5` hours
   - Priority: `High`
4. Click **Create**
5. ✅ Task should be created successfully

### Test 4: Verify Language is English
Check these pages - all should be in English:
- [ ] Dashboard (Members, Tasks, Projects)
- [ ] Members page
- [ ] Tasks page (Create Task, Priority, Status)
- [ ] Projects page (Create Project, Start date, End date)

---

## 🔍 What To Check

### ✅ Projects Page
- Button says "Create Project" (not "Créer un Projet")
- Form labels in English
- Can create project successfully
- Project appears in list

### ✅ Tasks Page
- Button says "Create Task" (not "Créer une Tâche")
- **Project dropdown appears FIRST** in form
- Project dropdown populated with projects
- Can't submit without selecting project
- Can submit after selecting project
- Labels: "Priority", "Status", "Sort by" (not French)

### ✅ Navigation
- Sidebar shows: Dashboard, Members, Tasks, Projects
- No French words in navigation

### ✅ Form Validation
- Error message appears when no project selected
- Error message is in English
- Form doesn't submit until project selected

---

## 🐛 Troubleshooting

### Problem: "No projects in dropdown"
**Solution:** Create a project first on the Projects page

### Problem: "Task created but no project_id"
**Solution:** 
1. Stop server
2. Run the SQL update script again
3. Restart server

### Problem: "Server error when creating task"
**Check:**
- Browser console for errors
- Server terminal for Java errors
- Database has project_id column: `DESCRIBE task;`

### Problem: "Still seeing French text"
**Solution:** 
- Hard refresh browser: Ctrl+F5
- Clear browser cache
- Check you're on the correct pages (html/ folder)

---

## 📊 Expected Database State

After creating 1 project and 1 task:

```sql
-- Check projects table
SELECT * FROM project;
-- Should see: 1 row with 'Website Redesign'

-- Check tasks table with project
SELECT id, name, project_id FROM task;
-- Should see: 1 row with project_id NOT NULL

-- Verify relationship
SELECT t.name AS task_name, p.name AS project_name 
FROM task t 
JOIN project p ON t.project_id = p.id;
-- Should show: Design Homepage | Website Redesign
```

---

## ✨ Success Criteria

You're done when:
- ✅ Can create projects via UI
- ✅ Can't create tasks without projects
- ✅ Can create tasks with projects
- ✅ All UI text is in English
- ✅ Tasks have valid project_id in database
- ✅ No French text visible anywhere

---

## 🎯 Quick Verification Commands

```sql
-- Verify project_id column exists
SHOW COLUMNS FROM task LIKE 'project_id';

-- Count tasks with projects
SELECT COUNT(*) FROM task WHERE project_id IS NOT NULL;

-- List all projects
SELECT id, name FROM project;
```

---

**You're all set! 🚀**
