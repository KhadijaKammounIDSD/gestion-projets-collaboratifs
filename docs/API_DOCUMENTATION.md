# API Documentation

## Base URL
```
http://localhost:8080/mini_projet/api
```

## Authentication

All requests require a valid session. Users must login first via `/auth/login`.

## Response Format

All endpoints return JSON with proper `Content-Type: application/json` header.

### Success Response (200, 201)
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  ...
}
```

### Error Response (4xx, 5xx)
```json
{
  "error": "Error message describing what went wrong"
}
```

## Endpoints

### Members

#### GET /api/members
List all members with skills.

**Query Parameters:**
- `id` - Get specific member by ID
- `available` - Filter available members (true/false)
- `teamId` - Get members of a specific team
- `_t` - Cache-busting timestamp (recommended)

**Response:**
```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "role": "Developer",
    "currentLoad": 20.5,
    "available": true,
    "weeklyAvailability": 40.0,
    "remainingHours": 19.5,
    "memberSkills": [
      {
        "id": 1,
        "skillId": 1,
        "level": 3,
        "skill": { "id": 1, "name": "Java" }
      }
    ]
  }
]
```

#### POST /api/members
Create a new member.

**Request Body:**
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane@example.com",
  "password": "hashedpassword",
  "role": "Tester",
  "currentLoad": 0,
  "available": true
}
```

#### PUT /api/members
Update member information.

**Request Body:**
```json
{
  "id": 1,
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@example.com",
  "role": "QA Engineer",
  "available": true
}
```

#### DELETE /api/members?id=1
Delete a member.

---

### Teams

#### GET /api/teams
List all teams with member counts and member details.

**Query Parameters:**
- `id` - Get specific team
- `_t` - Cache-busting timestamp

**Response:**
```json
[
  {
    "id": 1,
    "name": "Team Alpha",
    "memberCount": 3,
    "members": [
      {
        "id": 1,
        "firstName": "John",
        "lastName": "Doe",
        "email": "john@example.com",
        "currentLoad": 20.5,
        "available": true
      }
    ]
  }
]
```

#### POST /api/teams
Create a new team.

**Request Body:**
```json
{
  "name": "Team Beta"
}
```

#### PUT /api/teams
Update team information.

**Request Body:**
```json
{
  "id": 1,
  "name": "Team Alpha (Updated)"
}
```

#### DELETE /api/teams?id=1
Delete a team.

---

### Projects

#### GET /api/projects
List all projects.

**Query Parameters:**
- `id` - Get specific project
- `status` - Filter by status (In Progress, Completed, etc.)
- `_t` - Cache-busting timestamp

**Response:**
```json
[
  {
    "id": 1,
    "name": "E-Commerce Platform",
    "description": "Build a complete e-commerce system",
    "startDate": "2025-01-15",
    "endDate": "2025-04-30",
    "status": "In Progress"
  }
]
```

#### POST /api/projects
Create a new project.

**Request Body:**
```json
{
  "name": "Mobile App",
  "description": "Build iOS and Android apps",
  "startDate": "2025-02-01",
  "endDate": "2025-06-30",
  "status": "In Progress"
}
```

#### PUT /api/projects
Update project information.

#### DELETE /api/projects?id=1
Delete a project.

---

### Tasks

#### GET /api/tasks
List all tasks with assignee and project information.

**Query Parameters:**
- `id` - Get specific task
- `projectId` - Get tasks for a project
- `assigneeId` - Get tasks assigned to a member
- `status` - Filter by status
- `_t` - Cache-busting timestamp

**Response:**
```json
[
  {
    "id": 1,
    "name": "Design Database Schema",
    "description": "Create normalized schema",
    "estimatedDuration": 16.0,
    "plannedStartDate": "2025-01-15",
    "plannedEndDate": "2025-01-17",
    "priority": "High",
    "status": "Assigned",
    "projectId": 1,
    "assigneeId": 1,
    "dependencyIds": [],
    "requiredSkillIds": [2, 5]
  }
]
```

#### POST /api/tasks
Create a new task.

**Request Body:**
```json
{
  "name": "API Development",
  "description": "Develop REST API endpoints",
  "estimatedDuration": 24.0,
  "plannedStartDate": "2025-01-20",
  "plannedEndDate": "2025-01-25",
  "priority": "High",
  "status": "Unassigned",
  "projectId": 1,
  "assigneeId": null,
  "dependencyIds": [1],
  "requiredSkillIds": [1, 2]
}
```

#### PUT /api/tasks
Update task information and assignee.

#### DELETE /api/tasks?id=1
Delete a task.

---

### Skills

#### GET /api/skills
List all available skills.

**Query Parameters:**
- `id` - Get specific skill
- `_t` - Cache-busting timestamp

**Response:**
```json
[
  {
    "id": 1,
    "name": "Java"
  },
  {
    "id": 2,
    "name": "Python"
  }
]
```

#### POST /api/skills
Create a new skill.

**Request Body:**
```json
{
  "name": "React.js"
}
```

#### DELETE /api/skills?id=1
Delete a skill.

---

### Task Assignment

#### POST /api/task-assignment/auto-assign
Automatically assign all unassigned tasks to members.

**Response:**
```json
{
  "success": true,
  "message": "Auto-assignment completed",
  "assignedCount": 5,
  "unassignedCount": 2
}
```

---

### Alerts

#### GET /api/alerts
List all alerts in the system.

**Query Parameters:**
- `_t` - Cache-busting timestamp

**Response:**
```json
[
  {
    "id": 1,
    "type": "OVERLOAD",
    "message": "Member John Doe is overloaded (75h / 40h weekly)",
    "memberId": 1,
    "timestamp": "2025-12-29T10:30:00"
  }
]
```

#### DELETE /api/alerts?id=1
Clear an alert.

---

### Dashboard Statistics

#### GET /api/dashboard
Get dashboard statistics and metrics.

**Response:**
```json
{
  "totalMembers": 7,
  "totalTeams": 2,
  "totalProjects": 2,
  "totalTasks": 10,
  "assignedTasks": 7,
  "unassignedTasks": 3,
  "overloadedMembers": 2,
  "teamStats": [...]
}
```

---

### Authentication

#### POST /api/auth/signup
Register a new user.

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "securepassword",
  "role": "Developer",
  "skills": ["Java", "Python"]
}
```

#### POST /api/auth/login
Login with credentials (starts session).

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "securepassword"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "member": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "role": "Developer"
  }
}
```

#### POST /api/auth/logout
Logout (invalidates session).

**Response:**
```json
{
  "success": true,
  "message": "Logout successful"
}
```

---

## Error Codes

| Code | Message | Cause |
|------|---------|-------|
| 200 | OK | Successful GET/PUT request |
| 201 | Created | Resource successfully created (POST) |
| 400 | Bad Request | Invalid request data |
| 401 | Unauthorized | User not authenticated |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Resource already exists (e.g., duplicate email) |
| 500 | Server Error | Database or server error |

---

## Important Notes

1. **Always use camelCase in JSON responses** - Gson uses IDENTITY naming policy
2. **Cache-busting is recommended** - Add `?_t=${timestamp}` to GET requests
3. **Session management** - Login creates a session, logout invalidates it
4. **Workload calculation** - Automatically updated when tasks are assigned/unassigned
5. **Task dependencies** - Enforced in business logic, not database constraints

---

**Last Updated:** December 29, 2025
