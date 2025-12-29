// ============================================================================
// PROJECT MANAGEMENT APPLICATION - MAIN JAVASCRIPT FILE
// ============================================================================
// This file handles all API interactions and UI updates for:
// - Members, Teams, Skills
// - Projects, Tasks
// - Alerts, Dashboard
// - Timeline data
// ============================================================================

// API_BASE_URL is defined in api-client.js

// ============================================================================
// MEMBER MANAGEMENT
// ============================================================================

async function loadMembers() {
    try {
        const response = await fetch(`${API_BASE_URL}/members`);
        const members = await response.json();
        
        const container = document.getElementById('members-list');
        if (!container) return;
        
        container.innerHTML = '';
        
        if (members.length === 0) {
            container.innerHTML = '<p class="empty-state">No members found. Add one!</p>';
            return;
        }
        
        members.forEach(member => {
            const card = createMemberCard(member);
            container.appendChild(card);
        });
        
        updateMemberCount(members.length);
        
        // Also update member selects in other forms
        updateMemberSelects(members);
    } catch (error) {
        console.error('Error loading members:', error);
        showError('Unable to load members. Check that the server is running.');
    }
}

function createMemberCard(member) {
    const card = document.createElement('div');
    card.className = 'member-card';
    card.innerHTML = `
        <div class="member-header">
            <div class="member-avatar">${(member.firstName || 'U').charAt(0)}${(member.lastName || 'N').charAt(0)}</div>
            <div class="member-info">
                <h3>${member.firstName || ''} ${member.lastName || ''}</h3>
                <p class="member-role">${member.role || 'N/A'}</p>
            </div>
        </div>
        <div class="member-details">
            <p><i class="fas fa-envelope"></i> ${member.email || 'N/A'}</p>
            <p><i class="fas fa-briefcase"></i> Load: ${(member.currentLoad || 0).toFixed(1)}h</p>
            <p><i class="fas fa-circle ${member.available ? 'available' : 'busy'}"></i> 
               ${member.available ? 'Available' : 'Busy'}
            </p>
        </div>
        <div class="member-actions">
            <button onclick="editMember(${member.id})" class="btn-edit">
                <i class="fas fa-edit"></i> Edit
            </button>
            <button onclick="deleteMember(${member.id})" class="btn-delete">
                <i class="fas fa-trash"></i> Delete
            </button>
        </div>
    `;
    return card;
}

async function addMember(formData) {
    try {
        const member = {
            firstName: formData.get('firstName'),
            lastName: formData.get('lastName'),
            email: formData.get('email'),
            role: formData.get('role'),
            currentLoad: 0,
            available: true,
            teamId: parseInt(formData.get('teamId')) || null
        };
        
        const response = await fetch(`${API_BASE_URL}/members`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(member)
        });
        
        if (response.ok) {
            showSuccess('Member added successfully!');
            closeModal('add-member-modal');
            loadMembers();
        } else {
            const error = await response.json();
            showError(error.error || 'Error adding member');
        }
    } catch (error) {
        console.error('Error:', error);
        showError('Server connection error');
    }
}

async function editMember(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/members?id=${id}`);
        const member = await response.json();
        
        // Populate edit form
        const editForm = document.getElementById('edit-member-form');
        if (editForm) {
            editForm.querySelector('[name="id"]').value = member.id;
            editForm.querySelector('[name="firstName"]').value = member.firstName || '';
            editForm.querySelector('[name="lastName"]').value = member.lastName || '';
            editForm.querySelector('[name="email"]').value = member.email || '';
            editForm.querySelector('[name="role"]').value = member.role || '';
            const teamSelect = editForm.querySelector('[name="teamId"]');
            if (teamSelect) teamSelect.value = member.teamId || '';
        }
        
        openModal('edit-member-modal');
    } catch (error) {
        console.error('Error:', error);
        showError('Error loading member data');
    }
}

async function updateMember(formData) {
    try {
        const member = {
            id: parseInt(formData.get('id')),
            firstName: formData.get('firstName'),
            lastName: formData.get('lastName'),
            email: formData.get('email'),
            role: formData.get('role'),
            teamId: parseInt(formData.get('teamId')) || null
        };
        
        const response = await fetch(`${API_BASE_URL}/members`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(member)
        });
        
        if (response.ok) {
            showSuccess('Member updated successfully!');
            closeModal('edit-member-modal');
            loadMembers();
        } else {
            showError('Error updating member');
        }
    } catch (error) {
        console.error('Error:', error);
        showError('Connection error');
    }
}

async function deleteMember(id) {
    if (!confirm('Are you sure you want to delete this member?')) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/members?id=${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showSuccess('Member deleted');
            loadMembers();
        } else {
            showError('Error deleting member');
        }
    } catch (error) {
        console.error('Error:', error);
        showError('Connection error');
    }
}

async function updateMemberSelects(members = null) {
    const selects = document.querySelectorAll('select[name="assigneeId"], select[name="memberId"]');
    
    let memberList = members;
    if (!memberList) {
        try {
            const response = await fetch(`${API_BASE_URL}/members`);
            memberList = await response.json();
        } catch (error) {
            return;
        }
    }
    
    selects.forEach(select => {
        const currentValue = select.value;
        
        // Clear except first option
        while (select.options.length > 1) {
            select.remove(1);
        }
        
        memberList.forEach(member => {
            const option = document.createElement('option');
            option.value = member.id;
            option.textContent = `${member.firstName} ${member.lastName}`;
            select.appendChild(option);
        });
        
        if (currentValue) select.value = currentValue;
    });
}

// ============================================================================
// TEAM MANAGEMENT
// ============================================================================

async function loadTeams() {
    try {
        const response = await fetch(`${API_BASE_URL}/teams`);
        const teams = await response.json();
        
        const container = document.getElementById('teams-list');
        if (container) {
            container.innerHTML = '';
            
            if (teams.length === 0) {
                container.innerHTML = '<p class="empty-state">No teams found. Create one!</p>';
            } else {
                teams.forEach(team => {
                    const card = createTeamCard(team);
                    container.appendChild(card);
                });
            }
        }
        
        // Update team selects in member forms
        updateTeamSelects(teams);
        
        return teams;
    } catch (error) {
        console.error('Error loading teams:', error);
        showError('Unable to load teams');
        return [];
    }
}

function createTeamCard(team) {
    const card = document.createElement('div');
    card.className = 'team-card';
    
    // Create member avatars HTML
    let membersHtml = '';
    if (team.members && team.members.length > 0) {
        team.members.slice(0, 5).forEach(member => {
            membersHtml += `<div class="member-avatar">${(member.firstName || 'U').charAt(0)}${(member.lastName || 'N').charAt(0)}</div>`;
        });
        if (team.members.length > 5) {
            membersHtml += `<div class="member-avatar more">+${team.members.length - 5}</div>`;
        }
    }
    
    card.innerHTML = `
        <div class="team-header">
            <div class="team-icon"><i class="fas fa-users"></i></div>
            <div>
                <h3>${team.name}</h3>
            </div>
        </div>
        <div class="team-stats">
            <div class="stat-item">
                <div class="stat-value">${team.memberCount || 0}</div>
                <div class="stat-label">Members</div>
            </div>
        </div>
        <div class="team-members">
            ${membersHtml || '<span class="no-members">No members yet</span>'}
        </div>
        <div class="team-actions">
            <button onclick="editTeam(${team.id})" class="btn-edit">
                <i class="fas fa-edit"></i> Edit
            </button>
            <button onclick="deleteTeam(${team.id})" class="btn-delete">
                <i class="fas fa-trash"></i> Delete
            </button>
        </div>
    `;
    return card;
}

async function addTeam(formData) {
    try {
        const team = {
            name: formData.get('name')
        };
        
        const response = await fetch(`${API_BASE_URL}/teams`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(team)
        });
        
        if (response.ok) {
            showSuccess('Team created successfully!');
            closeModal('add-team-modal');
            loadTeams();
        } else {
            showError('Error creating team');
        }
    } catch (error) {
        console.error('Error:', error);
        showError('Connection error');
    }
}

async function editTeam(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/teams?id=${id}`);
        const team = await response.json();
        
        const editForm = document.getElementById('edit-team-form');
        if (editForm) {
            editForm.querySelector('[name="id"]').value = team.id;
            editForm.querySelector('[name="name"]').value = team.name;
        }
        
        openModal('edit-team-modal');
    } catch (error) {
        console.error('Error:', error);
        showError('Error loading team data');
    }
}

async function updateTeam(formData) {
    try {
        const team = {
            id: parseInt(formData.get('id')),
            name: formData.get('name')
        };
        
        const response = await fetch(`${API_BASE_URL}/teams`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(team)
        });
        
        if (response.ok) {
            showSuccess('Team updated successfully!');
            closeModal('edit-team-modal');
            loadTeams();
        } else {
            showError('Error updating team');
        }
    } catch (error) {
        console.error('Error:', error);
        showError('Connection error');
    }
}

async function deleteTeam(id) {
    if (!confirm('Are you sure you want to delete this team? Members will be unassigned.')) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/teams?id=${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showSuccess('Team deleted');
            loadTeams();
        } else {
            showError('Error deleting team');
        }
    } catch (error) {
        console.error('Error:', error);
        showError('Connection error');
    }
}

async function updateTeamSelects(teams = null) {
    const selects = document.querySelectorAll('select[name="teamId"]');
    
    let teamList = teams;
    if (!teamList) {
        try {
            const response = await fetch(`${API_BASE_URL}/teams`);
            teamList = await response.json();
        } catch (error) {
            console.error('Error loading teams:', error);
            return;
        }
    }
    
    selects.forEach(select => {
        const currentValue = select.value;
        
        // Clear except first option
        while (select.options.length > 1) {
            select.remove(1);
        }
        
        // Add teams from database ONLY
        teamList.forEach(team => {
            const option = document.createElement('option');
            option.value = team.id;
            option.textContent = team.name;
            select.appendChild(option);
        });
        
        if (currentValue) select.value = currentValue;
    });
}

// ============================================================================
// SKILL MANAGEMENT
// ============================================================================

async function loadSkills() {
    try {
        const response = await fetch(`${API_BASE_URL}/skills`);
        const skills = await response.json();
        
        const container = document.getElementById('skills-list');
        if (container) {
            container.innerHTML = '';
            
            if (skills.length === 0) {
                container.innerHTML = '<p class="empty-state">No skills found. Add one!</p>';
            } else {
                skills.forEach(skill => {
                    const item = document.createElement('div');
                    item.className = 'skill-item';
                    item.innerHTML = `
                        <span class="skill-name">${skill.name}</span>
                        <button onclick="deleteSkill(${skill.id})" class="btn-delete-small">
                            <i class="fas fa-times"></i>
                        </button>
                    `;
                    container.appendChild(item);
                });
            }
        }
        
        updateSkillSelects(skills);
        return skills;
    } catch (error) {
        console.error('Error loading skills:', error);
        return [];
    }
}

async function addSkill(formData) {
    try {
        const skill = {
            name: formData.get('name')
        };
        
        const response = await fetch(`${API_BASE_URL}/skills`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(skill)
        });
        
        if (response.ok) {
            showSuccess('Skill added successfully!');
            closeModal('add-skill-modal');
            loadSkills();
        } else {
            showError('Error adding skill (may already exist)');
        }
    } catch (error) {
        console.error('Error:', error);
        showError('Connection error');
    }
}

async function deleteSkill(id) {
    if (!confirm('Are you sure you want to delete this skill?')) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/skills?id=${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showSuccess('Skill deleted');
            loadSkills();
        } else {
            showError('Error deleting skill');
        }
    } catch (error) {
        console.error('Error:', error);
        showError('Connection error');
    }
}

async function updateSkillSelects(skills = null) {
    const selects = document.querySelectorAll('select[name="skillId"], select[name="skills"]');
    
    let skillList = skills;
    if (!skillList) {
        try {
            const response = await fetch(`${API_BASE_URL}/skills`);
            skillList = await response.json();
        } catch (error) {
            return;
        }
    }
    
    selects.forEach(select => {
        while (select.options.length > 1) {
            select.remove(1);
        }
        
        skillList.forEach(skill => {
            const option = document.createElement('option');
            option.value = skill.id;
            option.textContent = skill.name;
            select.appendChild(option);
        });
    });
}

// ============================================================================
// TASK MANAGEMENT
// ============================================================================

async function loadTasks() {
    try {
        const response = await fetch(`${API_BASE_URL}/tasks`);
        const tasks = await response.json();
        
        const container = document.getElementById('tasks-list');
        if (!container) return;
        
        container.innerHTML = '';
        
        if (tasks.length === 0) {
            container.innerHTML = '<p class="empty-state">No tasks found. Create one!</p>';
            return;
        }
        
        tasks.forEach(task => {
            const card = createTaskCard(task);
            container.appendChild(card);
        });
        
        updateTaskCount(tasks.length);
    } catch (error) {
        console.error('Error loading tasks:', error);
        showError('Unable to load tasks');
    }
}

function createTaskCard(task) {
    const card = document.createElement('div');
    const priority = (task.priority || 'medium').toLowerCase();
    card.className = `task-card priority-${priority}`;
    card.innerHTML = `
        <div class="task-header">
            <h3>${task.name}</h3>
            <span class="badge badge-${priority}">${task.priority || 'Medium'}</span>
        </div>
        <p class="task-description">${task.description || 'No description'}</p>
        <div class="task-meta">
            <span><i class="fas fa-clock"></i> ${task.estimatedDuration || 0}h</span>
            <span><i class="fas fa-calendar"></i> ${task.plannedStartDate || 'Not set'}</span>
            <span class="status-badge status-${(task.status || 'planned').toLowerCase().replace(/\s+/g, '-')}">${task.status || 'Planned'}</span>
        </div>
        <div class="task-actions">
            <button onclick="assignTaskModal(${task.id})" class="btn-assign">
                <i class="fas fa-user-plus"></i> Assign
            </button>
            <button onclick="editTask(${task.id})" class="btn-edit">
                <i class="fas fa-edit"></i>
            </button>
            <button onclick="deleteTask(${task.id})" class="btn-delete">
                <i class="fas fa-trash"></i>
            </button>
        </div>
    `;
    return card;
}

async function addTask(formData) {
    try {
        const projectId = formData.get('projectId');
        
        if (!projectId || projectId === '') {
            showError('Please select a project. Tasks must belong to a project.');
            return;
        }
        
        // Read dates
        const startDateStr = formData.get('plannedStartDate') || null;
        const endDateStr = formData.get('plannedEndDate') || null;

        // Validate date order client-side
        if (startDateStr && endDateStr) {
            const start = new Date(startDateStr);
            const end = new Date(endDateStr);
            if (end < start) {
                showError('End date cannot be before start date.');
                return;
            }
        }

        const task = {
            name: formData.get('name'),
            description: formData.get('description'),
            estimatedDuration: parseFloat(formData.get('estimatedDuration')) || 0,
            plannedStartDate: startDateStr,
            plannedEndDate: endDateStr,
            priority: formData.get('priority') || 'Medium',
            status: 'Planned',
            projectId: parseInt(projectId)
        };
        
        const response = await fetch(`${API_BASE_URL}/tasks`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(task)
        });
        
        if (response.ok) {
            showSuccess('Task created successfully!');
            closeModal('add-task-modal');
            loadTasks();
        } else {
            showError('Error creating task');
        }
    } catch (error) {
        console.error('Error:', error);
        showError('Connection error');
    }
}

async function editTask(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/tasks?id=${id}`);
        const task = await response.json();
        
        const editForm = document.getElementById('edit-task-form');
        if (editForm) {
            editForm.querySelector('[name="id"]').value = task.id;
            editForm.querySelector('[name="name"]').value = task.name || '';
            editForm.querySelector('[name="description"]').value = task.description || '';
            editForm.querySelector('[name="estimatedDuration"]').value = task.estimatedDuration || '';
            editForm.querySelector('[name="priority"]').value = task.priority || 'Medium';
            editForm.querySelector('[name="status"]').value = task.status || 'Planned';
        }
        
        openModal('edit-task-modal');
    } catch (error) {
        console.error('Error:', error);
        showError('Error loading task data');
    }
}

async function updateTask(formData) {
    try {
        const task = {
            id: parseInt(formData.get('id')),
            name: formData.get('name'),
            description: formData.get('description'),
            estimatedDuration: parseFloat(formData.get('estimatedDuration')) || 0,
            priority: formData.get('priority'),
            status: formData.get('status')
        };
        
        const response = await fetch(`${API_BASE_URL}/tasks`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(task)
        });
        
        if (response.ok) {
            showSuccess('Task updated successfully!');
            closeModal('edit-task-modal');
            loadTasks();
        } else {
            showError('Error updating task');
        }
    } catch (error) {
        console.error('Error:', error);
        showError('Connection error');
    }
}

async function deleteTask(id) {
    if (!confirm('Are you sure you want to delete this task?')) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/tasks?id=${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showSuccess('Task deleted');
            loadTasks();
        } else {
            showError('Error deleting task');
        }
    } catch (error) {
        console.error('Error:', error);
        showError('Connection error');
    }
}

function assignTaskModal(taskId) {
    const input = document.getElementById('assign-task-id');
    if (input) input.value = taskId;
    updateMemberSelects();
    openModal('assign-task-modal');
}

async function assignTask(formData) {
    try {
        const taskId = formData.get('taskId');
        const memberId = formData.get('memberId');
        
        const response = await fetch(`${API_BASE_URL}/tasks`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                id: parseInt(taskId),
                assigneeId: parseInt(memberId)
            })
        });
        
        if (response.ok) {
            showSuccess('Task assigned successfully!');
            closeModal('assign-task-modal');
            loadTasks();
        } else {
            showError('Error assigning task');
        }
    } catch (error) {
        console.error('Error:', error);
        showError('Connection error');
    }
}

// ============================================================================
// PROJECT MANAGEMENT
// ============================================================================

async function loadProjects() {
    try {
        const response = await fetch(`${API_BASE_URL}/projects`);
        const projects = await response.json();
        
        const container = document.getElementById('projects-list');
        if (container) {
            container.innerHTML = '';
            
            if (projects.length === 0) {
                container.innerHTML = '<p class="empty-state">No projects found. Create one!</p>';
            } else {
                projects.forEach(project => {
                    const card = createProjectCard(project);
                    container.appendChild(card);
                });
            }
        }
        
        updateProjectSelects(projects);
        return projects;
    } catch (error) {
        console.error('Error loading projects:', error);
        showError('Unable to load projects');
        return [];
    }
}

function createProjectCard(project) {
    const card = document.createElement('div');
    card.className = 'project-card';
    const status = (project.status || 'unknown').toLowerCase().replace(/\s+/g, '-');
    card.innerHTML = `
        <h3>${project.name}</h3>
        <p>${project.description || 'No description'}</p>
        <div class="project-dates">
            <span><i class="fas fa-calendar-alt"></i> Start: ${project.startDate || 'N/A'}</span>
            <span><i class="fas fa-calendar-check"></i> End: ${project.endDate || 'N/A'}</span>
        </div>
        <span class="badge badge-${status}">${project.status || 'N/A'}</span>
        <div class="project-actions">
            <button onclick="editProject(${project.id})" class="btn-edit">
                <i class="fas fa-edit"></i> Edit
            </button>
            <button onclick="deleteProject(${project.id})" class="btn-delete">
                <i class="fas fa-trash"></i> Delete
            </button>
        </div>
    `;
    return card;
}

async function addProject(formData) {
    try {
        const project = {
            name: formData.get('name'),
            description: formData.get('description'),
            startDate: formData.get('startDate') || null,
            endDate: formData.get('endDate') || null,
            status: formData.get('status') || 'In Progress'
        };
        
        const response = await fetch(`${API_BASE_URL}/projects`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(project)
        });
        
        if (response.ok) {
            showSuccess('Project created successfully!');
            closeModal('add-project-modal');
            loadProjects();
        } else {
            showError('Error creating project');
        }
    } catch (error) {
        console.error('Error:', error);
        showError('Connection error');
    }
}

async function editProject(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/projects?id=${id}`);
        const project = await response.json();
        
        const editForm = document.getElementById('edit-project-form');
        if (editForm) {
            editForm.querySelector('[name="id"]').value = project.id;
            editForm.querySelector('[name="name"]').value = project.name || '';
            editForm.querySelector('[name="description"]').value = project.description || '';
            editForm.querySelector('[name="startDate"]').value = project.startDate || '';
            editForm.querySelector('[name="endDate"]').value = project.endDate || '';
            editForm.querySelector('[name="status"]').value = project.status || '';
        }
        
        openModal('edit-project-modal');
    } catch (error) {
        console.error('Error:', error);
        showError('Error loading project data');
    }
}

async function updateProject(formData) {
    try {
        const project = {
            id: parseInt(formData.get('id')),
            name: formData.get('name'),
            description: formData.get('description'),
            startDate: formData.get('startDate') || null,
            endDate: formData.get('endDate') || null,
            status: formData.get('status')
        };
        
        const response = await fetch(`${API_BASE_URL}/projects`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(project)
        });
        
        if (response.ok) {
            showSuccess('Project updated successfully!');
            closeModal('edit-project-modal');
            loadProjects();
        } else {
            showError('Error updating project');
        }
    } catch (error) {
        console.error('Error:', error);
        showError('Connection error');
    }
}

async function deleteProject(id) {
    if (!confirm('Are you sure you want to delete this project? All associated tasks will be unlinked.')) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/projects?id=${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showSuccess('Project deleted');
            loadProjects();
        } else {
            showError('Error deleting project');
        }
    } catch (error) {
        console.error('Error:', error);
        showError('Connection error');
    }
}

async function updateProjectSelects(projects = null) {
    const selects = document.querySelectorAll('select[name="projectId"]');
    
    let projectList = projects;
    if (!projectList) {
        try {
            const response = await fetch(`${API_BASE_URL}/projects`);
            projectList = await response.json();
        } catch (error) {
            console.error('Error loading projects:', error);
            return;
        }
    }
    
    selects.forEach(select => {
        const currentValue = select.value;
        
        while (select.options.length > 1) {
            select.remove(1);
        }
        
        projectList.forEach(project => {
            const option = document.createElement('option');
            option.value = project.id;
            option.textContent = project.name;
            select.appendChild(option);
        });
        
        if (currentValue) select.value = currentValue;
    });
}

// ============================================================================
// ALERT MANAGEMENT
// ============================================================================

async function loadAlerts() {
    try {
        const response = await fetch(`${API_BASE_URL}/alerts`);
        const alerts = await response.json();
        
        const container = document.getElementById('alerts-list');
        if (!container) return;
        
        container.innerHTML = '';
        
        if (alerts.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-check-circle"></i>
                    <p>No alerts! Everything is running smoothly.</p>
                </div>
            `;
            return;
        }
        
        alerts.forEach(alert => {
            const card = createAlertCard(alert);
            container.appendChild(card);
        });
        
        updateAlertCount(alerts.length);
    } catch (error) {
        console.error('Error loading alerts:', error);
        showError('Unable to load alerts');
    }
}

function createAlertCard(alert) {
    const card = document.createElement('div');
    const severity = (alert.severityLevel || 'medium').toLowerCase();
    card.className = `alert-card severity-${severity}`;
    
    let icon = 'fa-info-circle';
    if (alert.type === 'OVERLOAD') icon = 'fa-exclamation-triangle';
    else if (alert.type === 'DELAY') icon = 'fa-clock';
    else if (alert.type === 'CONFLICT') icon = 'fa-times-circle';
    
    card.innerHTML = `
        <div class="alert-icon ${severity}">
            <i class="fas ${icon}"></i>
        </div>
        <div class="alert-content">
            <div class="alert-header">
                <span class="alert-type">${alert.type || 'Alert'}</span>
                <span class="alert-date">${alert.issuedDate || 'Today'}</span>
            </div>
            <p class="alert-message">${alert.message}</p>
            <span class="badge badge-${severity}">${alert.severityLevel || 'Medium'}</span>
        </div>
        <button onclick="dismissAlert(${alert.id})" class="btn-dismiss">
            <i class="fas fa-times"></i>
        </button>
    `;
    return card;
}

async function dismissAlert(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/alerts?id=${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            loadAlerts();
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

async function createAlert(type, message, severity) {
    try {
        const alert = {
            type: type,
            message: message,
            severityLevel: severity,
            issuedDate: new Date().toISOString().split('T')[0]
        };
        
        const response = await fetch(`${API_BASE_URL}/alerts`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(alert)
        });
        
        if (response.ok) {
            loadAlerts();
        }
    } catch (error) {
        console.error('Error creating alert:', error);
    }
}

function updateAlertCount(count) {
    const badge = document.getElementById('alert-count');
    if (badge) {
        badge.textContent = count;
        badge.style.display = count > 0 ? 'inline-block' : 'none';
    }
}

// ============================================================================
// DASHBOARD & STATISTICS
// ============================================================================

async function loadDashboardStats() {
    try {
        const response = await fetch(`${API_BASE_URL}/dashboard/stats`);
        const stats = await response.json();
        
        updateElement('total-members', stats.totalMembers || 0);
        updateElement('available-members', stats.availableMembers || 0);
        updateElement('total-tasks', stats.totalTasks || 0);
        updateElement('assigned-tasks', stats.assignedTasks || 0);
        updateElement('total-projects', stats.totalProjects || 0);
        updateElement('avg-load', (stats.averageLoad || 0).toFixed(1) + 'h');
        updateElement('pending-tasks', stats.pendingTasks || 0);
        updateElement('completed-tasks', stats.completedTasks || 0);
        
    } catch (error) {
        console.error('Error loading dashboard stats:', error);
    }
}

async function loadWorkloadChart() {
    try {
        // Fetch members and tasks
        const [membersRes, tasksRes] = await Promise.all([
            fetch(`${API_BASE_URL}/members`),
            fetch(`${API_BASE_URL}/tasks`)
        ]);
        const members = await membersRes.json();
        const tasks = await tasksRes.json();

        // Compute task counts and load for each member
        let taskCounts = {};
        tasks.forEach(task => {
            if (task.assigneeId) {
                taskCounts[task.assigneeId] = (taskCounts[task.assigneeId] || 0) + 1;
            }
        });

        const container = document.getElementById('workload-chart');
        if (!container) return;
        container.innerHTML = '';

        if (!members || members.length === 0) {
            container.innerHTML = '<p class="empty-state">No members found. Add one!</p>';
            return;
        }

        members.forEach(member => {
            const assignedCount = taskCounts[member.id] || 0;
            const initials = `${(member.firstName || 'U').charAt(0)}${(member.lastName || 'N').charAt(0)}`;
            const weeklyHours = member.weeklyAvailability || 40;
            const remainingHours = member.remainingHours !== undefined ? member.remainingHours : weeklyHours;
            const usedHours = weeklyHours - remainingHours;
            const availabilityPercent = weeklyHours > 0 ? ((remainingHours / weeklyHours) * 100).toFixed(0) : 0;
            const load = member.currentLoad || 0;
            // Check for overload
            const isOverloaded = remainingHours < 0 || load > 160;
            // Color for bar
            let barColor = '#10b981'; // Green
            if (availabilityPercent < 20) {
                barColor = '#ef4444'; // Red
            } else if (availabilityPercent < 50) {
                barColor = '#f59e0b'; // Orange
            }
            // Bar width based on load (max 200h)
            const percentage = Math.min((load / 200) * 100, 100);
            // Build skills display
            let skillsHTML = '<span style="font-size:12px;"><i class="fas fa-star"></i> ';
            if (member.memberSkills && member.memberSkills.length > 0) {
                skillsHTML += member.memberSkills.map(ms =>
                    `<span class="skill-badge">${ms.skill?.name || 'Unknown'}</span>`
                ).join(' ');
            } else {
                skillsHTML += '<span style="color: #9ca3af; font-style: italic;">No skills</span>';
            }
            skillsHTML += '</span>';

            const bar = document.createElement('div');
            bar.className = 'workload-bar';
            bar.innerHTML = `
                <div class="bar-label">
                    <span class="member-avatar" style="${isOverloaded ? 'background: linear-gradient(135deg, #ef4444, #f87171);' : ''}">${initials}</span>
                    <span style="font-weight:600;">${member.firstName || ''} ${member.lastName || ''}</span>
                    ${isOverloaded ? '<span class="overload-warning"><i class="fas fa-exclamation-triangle"></i> Overloaded!</span>' : ''}
                </div>
                <div class="bar-details">
                    <span><i class="fas fa-briefcase"></i> Load: ${load.toFixed(1)}h</span>
                    <span><i class="fas fa-tasks"></i> Tasks: ${assignedCount}</span>
                    <span><i class="fas fa-clock"></i> This week: ${usedHours.toFixed(1)}h / ${weeklyHours}h</span>
                    <span><i class="fas fa-hourglass-half"></i> Remaining: <strong style="color: ${remainingHours < 0 ? '#ef4444' : remainingHours < 10 ? '#f59e0b' : '#10b981'}">${remainingHours.toFixed(1)}h</strong></span>
                    ${skillsHTML}
                </div>
                <div class="bar-fill" style="width: ${percentage}%; background: ${barColor}; margin-top: 6px;">
                    ${load.toFixed(1)}h
                </div>
            `;
            container.appendChild(bar);
        });
    } catch (error) {
        console.error('Error loading workload:', error);
    }
}

// ============================================================================
// TIMELINE DATA
// ============================================================================

async function loadTimelineData() {
    try {
        const [tasksRes, projectsRes, membersRes] = await Promise.all([
            fetch(`${API_BASE_URL}/tasks`),
            fetch(`${API_BASE_URL}/projects`),
            fetch(`${API_BASE_URL}/members`)
        ]);
        
        const tasks = await tasksRes.json();
        const projects = await projectsRes.json();
        const members = await membersRes.json();
        
        return { tasks, projects, members };
    } catch (error) {
        console.error('Error loading timeline data:', error);
        return { tasks: [], projects: [], members: [] };
    }
}

// ============================================================================
// PROFILE DATA
// ============================================================================

async function loadProfileData() {
    try {
        const userData = sessionStorage.getItem('user');
        if (!userData) {
            return null;
        }
        
        const user = JSON.parse(userData);
        
        const response = await fetch(`${API_BASE_URL}/members?id=${user.id}`);
        if (response.ok) {
            return await response.json();
        }
        
        return user;
    } catch (error) {
        console.error('Error loading profile:', error);
        return null;
    }
}

async function loadMemberSkills(memberId) {
    try {
        const response = await fetch(`${API_BASE_URL}/members/${memberId}/skills`);
        if (response.ok) {
            return await response.json();
        }
        return [];
    } catch (error) {
        console.error('Error loading skills:', error);
        return [];
    }
}

// ============================================================================
// AUTO ASSIGNMENT
// ============================================================================

async function runAutoAssignment() {
    const btn = document.getElementById('auto-assign-btn');
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Assigning...';
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/assignment/auto`, {
            method: 'POST'
        });
        
        const result = await response.json();
        
        showSuccess(`Assignment completed! ${result.assignedTasks || 0} tasks assigned`);
        
        loadTasks();
        loadMembers();
        loadDashboardStats();
        
    } catch (error) {
        console.error('Error during assignment:', error);
        showError('Error during automatic assignment');
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = '<i class="fas fa-magic"></i> Auto Assign';
        }
    }
}

// ============================================================================
// UTILITIES
// ============================================================================

function updateElement(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = value;
}

function updateMemberCount(count) {
    updateElement('members-count', count);
}

function updateTaskCount(count) {
    updateElement('tasks-count', count);
}

function showSuccess(message) {
    showNotification(message, 'success');
}

function showError(message) {
    showNotification(message, 'error');
}

function showNotification(message, type = 'info') {
    // Remove existing notifications
    const existing = document.querySelector('.notification-toast');
    if (existing) existing.remove();
    
    const toast = document.createElement('div');
    toast.className = `notification-toast ${type}`;
    toast.innerHTML = `
        <i class="fas ${type === 'success' ? 'fa-check-circle' : type === 'error' ? 'fa-exclamation-circle' : 'fa-info-circle'}"></i>
        <span>${message}</span>
    `;
    
    document.body.appendChild(toast);
    
    setTimeout(() => toast.classList.add('show'), 10);
    
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
        const form = modal.querySelector('form');
        if (form) form.reset();
    }
}

function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.style.display = 'flex';
}

// Close modal when clicking outside
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal')) {
        e.target.style.display = 'none';
    }
});

// ============================================================================
// SETTINGS PAGE FUNCTIONS
// ============================================================================

async function loadSettingsData() {
    await Promise.all([
        loadSkills(),
        loadTeams(),
        loadProjects()
    ]);
}

// ============================================================================
// INITIALIZATION
// ============================================================================

document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Application loaded');
    
    const currentPage = window.location.pathname.toLowerCase();
    
    // Load data based on current page
    if (currentPage.includes('dashboard') || currentPage.endsWith('/') || currentPage.endsWith('index.html')) {
        loadDashboardStats();
        loadWorkloadChart();
    }
    
    if (currentPage.includes('members')) {
        loadMembers();
        updateTeamSelects();
    }
    
    if (currentPage.includes('tasks')) {
        loadTasks();
        updateProjectSelects();
        updateMemberSelects();
    }
    
    if (currentPage.includes('projects')) {
        loadProjects();
    }
    
    if (currentPage.includes('teams')) {
        loadTeams();
    }
    
    if (currentPage.includes('alerts')) {
        loadAlerts();
    }
    
    if (currentPage.includes('settings')) {
        loadSettingsData();
    }
    
    if (currentPage.includes('profile')) {
        initProfilePage();
    }
    
    if (currentPage.includes('timeline')) {
        initTimeline();
    }
    
    // Setup forms
    setupForms();
});

function setupForms() {
    // Member form
    const memberForm = document.getElementById('add-member-form');
    if (memberForm) {
        memberForm.addEventListener('submit', function(e) {
            e.preventDefault();
            addMember(new FormData(this));
        });
    }
    
    // Edit member form
    const editMemberForm = document.getElementById('edit-member-form');
    if (editMemberForm) {
        editMemberForm.addEventListener('submit', function(e) {
            e.preventDefault();
            updateMember(new FormData(this));
        });
    }
    
    // Task form
    const taskForm = document.getElementById('add-task-form');
    if (taskForm && !taskForm.dataset.customSubmit) {
        taskForm.addEventListener('submit', function(e) {
            e.preventDefault();
            addTask(new FormData(this));
        });
    }
    
    // Edit task form
    const editTaskForm = document.getElementById('edit-task-form');
    if (editTaskForm) {
        editTaskForm.addEventListener('submit', function(e) {
            e.preventDefault();
            updateTask(new FormData(this));
        });
    }
    
    // Assign task form
    const assignTaskForm = document.getElementById('assign-task-form');
    if (assignTaskForm) {
        assignTaskForm.addEventListener('submit', function(e) {
            e.preventDefault();
            assignTask(new FormData(this));
        });
    }
    
    // Project form
    const projectForm = document.getElementById('add-project-form');
    if (projectForm && !projectForm.dataset.customSubmit) {
        projectForm.addEventListener('submit', function(e) {
            e.preventDefault();
            addProject(new FormData(this));
        });
    }
    
    // Edit project form
    const editProjectForm = document.getElementById('edit-project-form');
    if (editProjectForm) {
        editProjectForm.addEventListener('submit', function(e) {
            e.preventDefault();
            updateProject(new FormData(this));
        });
    }
    
    // Team form
    const teamForm = document.getElementById('add-team-form');
    if (teamForm) {
        teamForm.addEventListener('submit', function(e) {
            e.preventDefault();
            addTeam(new FormData(this));
        });
    }
    
    // Edit team form
    const editTeamForm = document.getElementById('edit-team-form');
    if (editTeamForm) {
        editTeamForm.addEventListener('submit', function(e) {
            e.preventDefault();
            updateTeam(new FormData(this));
        });
    }
    
    // Skill form
    const skillForm = document.getElementById('add-skill-form');
    if (skillForm) {
        skillForm.addEventListener('submit', function(e) {
            e.preventDefault();
            addSkill(new FormData(this));
        });
    }
}

// Profile page initialization
async function initProfilePage() {
    const member = await loadProfileData();
    if (!member) {
        // If no logged in user, show default or redirect
        document.getElementById('profile-name').textContent = 'Guest User';
        document.getElementById('profile-email').textContent = 'Not logged in';
        return;
    }
    
    // Update profile display
    const nameEl = document.getElementById('profile-name');
    const emailEl = document.getElementById('profile-email');
    const roleEl = document.getElementById('profile-role');
    const avatarEl = document.getElementById('profile-avatar');
    
    if (nameEl) nameEl.textContent = `${member.firstName || ''} ${member.lastName || ''}`;
    if (emailEl) emailEl.textContent = member.email || '';
    if (roleEl) roleEl.textContent = member.role || 'N/A';
    if (avatarEl) avatarEl.textContent = `${(member.firstName || 'U').charAt(0)}${(member.lastName || 'N').charAt(0)}`;
    
    // Load skills
    const skills = await loadMemberSkills(member.id);
    const skillsContainer = document.getElementById('profile-skills');
    if (skillsContainer) {
        skillsContainer.innerHTML = '';
        if (skills.length > 0) {
            skills.forEach(skill => {
                const tag = document.createElement('span');
                tag.className = 'skill-tag';
                tag.textContent = skill.name;
                skillsContainer.appendChild(tag);
            });
        } else {
            skillsContainer.innerHTML = '<span class="no-skills">No skills added</span>';
        }
    }
}

// Timeline initialization
async function initTimeline() {
    const data = await loadTimelineData();
    if (window.renderTimeline) {
        window.renderTimeline(data);
    }
}

// Logout function
async function logout() {
    try {
        // Call backend to destroy session
        await fetch(`${API_BASE_URL}/auth/logout`, {
            method: 'POST',
            credentials: 'include'
        });
    } catch (error) {
        console.error('Logout error:', error);
    }
    
    // Clear session storage
    sessionStorage.clear();
    
    // Redirect to login page
    window.location.href = 'login-page.html';
}
