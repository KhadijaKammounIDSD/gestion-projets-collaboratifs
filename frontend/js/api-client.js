/**
 * API Client pour la Plateforme de Gestion de Projets
 * Utiliser ce fichier pour faire des appels AJAX vers le back-end
 * 
 * ⚠️ CONVENTION DE NOMMAGE IMPORTANTE:
 * - Toutes les données JSON utilisent camelCase (firstName, currentLoad, teamId)
 * - NE PAS utiliser snake_case (first_name, current_load, team_id)
 * - Le backend (Java + Gson) convertit automatiquement
 * - Voir NAMING_CONVENTION.md pour plus de détails
 * 
 * Exemple correct:
 * {
 *   "firstName": "Alice",       // ✅ CORRECT
 *   "lastName": "Dupont",       // ✅ CORRECT
 *   "currentLoad": 45.5,        // ✅ CORRECT
 *   "teamId": 1                 // ✅ CORRECT
 * }
 */

const API_BASE_URL = 'http://localhost:8080/mini_projet/api'; // Ajuster selon votre configuration

// ============================================================================
// MEMBERS API
// ============================================================================

/**
 * Récupérer tous les membres
 */
async function getAllMembers() {
    try {
        const response = await fetch(`${API_BASE_URL}/members`);
        if (!response.ok) throw new Error('Error fetching members');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return [];
    }
}

/**
 * Récupérer un membre par ID
 */
async function getMemberById(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/members?id=${id}`);
        if (!response.ok) throw new Error('Member not found');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return null;
    }
}

/**
 * Créer un nouveau membre
 */
async function createMember(memberData) {
    try {
        const response = await fetch(`${API_BASE_URL}/members`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(memberData)
        });
        if (!response.ok) throw new Error('Error creating member');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return null;
    }
}

/**
 * Mettre à jour un membre
 */
async function updateMember(memberData) {
    try {
        const response = await fetch(`${API_BASE_URL}/members`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(memberData)
        });
        if (!response.ok) throw new Error('Error updating member');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return null;
    }
}

/**
 * Supprimer un membre
 */
async function deleteMember(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/members?id=${id}`, {
            method: 'DELETE'
        });
        if (!response.ok) throw new Error('Error deleting member');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return false;
    }
}

// ============================================================================
// TASKS API
// ============================================================================

/**
 * Récupérer toutes les tâches
 */
async function getAllTasks() {
    try {
        const response = await fetch(`${API_BASE_URL}/tasks`);
        if (!response.ok) throw new Error('Error fetching tasks');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return [];
    }
}

/**
 * Récupérer les tâches non assignées
 */
async function getUnassignedTasks() {
    try {
        const response = await fetch(`${API_BASE_URL}/tasks?unassigned=true`);
        if (!response.ok) throw new Error('Error fetching unassigned tasks');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return [];
    }
}

/**
 * Créer une nouvelle tâche
 */
async function createTask(taskData) {
    try {
        // Client-side guard: validate planned dates if provided
        if (taskData.plannedStartDate && taskData.plannedEndDate) {
            const start = new Date(taskData.plannedStartDate);
            const end = new Date(taskData.plannedEndDate);
            if (end < start) {
                throw new Error('End date cannot be before start date');
            }
        }
        const response = await fetch(`${API_BASE_URL}/tasks`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(taskData)
        });
        if (!response.ok) {
            const msg = await response.text();
            throw new Error(msg || 'Error creating task');
        }
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return null;
    }
}

/**
 * Assigner une tâche à un membre
 */
async function assignTask(taskId, memberId) {
    try {
        const response = await fetch(`${API_BASE_URL}/tasks/assign?taskId=${taskId}&memberId=${memberId}`, {
            method: 'POST'
        });
        if (!response.ok) throw new Error('Error assigning task');
        return await response.json();
    } catch (error) {
        console.error('Erreur:', error);
        return null;
    }
}

// ============================================================================
// ASSIGNMENT API (Affectation Automatique)
// ============================================================================

/**
 * Lancer l'affectation automatique de toutes les tâches
 */
async function autoAssignTasks() {
    try {
        const response = await fetch(`${API_BASE_URL}/assignment/auto`, {
            method: 'POST'
        });
        if (!response.ok) throw new Error('Error during automatic assignment');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return null;
    }
}

/**
 * Affecte une tâche urgente
 */
async function assignUrgentTask(taskId) {
    try {
        const response = await fetch(`${API_BASE_URL}/assignment/urgent?taskId=${taskId}`, {
            method: 'POST'
        });
        if (!response.ok) throw new Error('Erreur lors de l\'affectation de la tâche urgente');
        return await response.json();
    } catch (error) {
        console.error('Erreur:', error);
        return null;
    }
}

/**
 * Récupérer le rapport d'affectation
 */
async function getAssignmentReport() {
    try {
        const response = await fetch(`${API_BASE_URL}/assignment/report`);
        if (!response.ok) throw new Error('Erreur lors de la récupération du rapport');
        return await response.json();
    } catch (error) {
        console.error('Erreur:', error);
        return null;
    }
}

// ============================================================================
// DASHBOARD API
// ============================================================================

/**
 * Récupérer les statistiques du dashboard
 */
async function getDashboardStats() {
    try {
        const response = await fetch(`${API_BASE_URL}/dashboard/stats`);
        if (!response.ok) throw new Error('Erreur lors de la récupération des statistiques');
        return await response.json();
    } catch (error) {
        console.error('Erreur:', error);
        return null;
    }
}

/**
 * Récupérer la répartition de la charge
 */
async function getWorkloadDistribution() {
    try {
        const response = await fetch(`${API_BASE_URL}/dashboard/workload`);
        if (!response.ok) throw new Error('Erreur lors de la récupération de la charge');
        return await response.json();
    } catch (error) {
        console.error('Erreur:', error);
        return null;
    }
}

/**
 * Récupérer les alertes actives
 */
async function getActiveAlerts() {
    try {
        const response = await fetch(`${API_BASE_URL}/dashboard/alerts`);
        if (!response.ok) throw new Error('Error fetching alerts');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return [];
    }
}

/**
 * Récupérer l'avancement du projet
 */
async function getProjectProgress() {
    try {
        const response = await fetch(`${API_BASE_URL}/dashboard/progress`);
        if (!response.ok) throw new Error('Error fetching project progress');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return null;
    }
}

// ============================================================================
// PROJECTS API
// ============================================================================

/**
 * Récupérer tous les projets
 */
async function getAllProjects() {
    try {
        const response = await fetch(`${API_BASE_URL}/projects`);
        if (!response.ok) throw new Error('Error fetching projects');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return [];
    }
}

/**
 * Créer un nouveau projet
 */
async function createProject(projectData) {
    try {
        const response = await fetch(`${API_BASE_URL}/projects`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(projectData)
        });
        if (!response.ok) throw new Error('Error creating project');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return null;
    }
}

// ============================================================================
// FONCTIONS UTILITAIRES
// ============================================================================

/**
 * Afficher un message de succès
 */
function showSuccessMessage(message) {
    // À personnaliser selon votre UI
    console.log('✓', message);
    // Exemple: afficher une notification toast
}

/**
 * Afficher un message d'erreur
 */
function showErrorMessage(message) {
    // À personnaliser selon votre UI
    console.error('✗', message);
    // Exemple: afficher une notification toast d'erreur
}

/**
 * Formater une date au format français
 */
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

/**
 * Calculer le pourcentage
 */
function calculatePercentage(part, total) {
    if (total === 0) return 0;
    return Math.round((part / total) * 100);
}

// ============================================================================
// EXEMPLE D'UTILISATION
// ============================================================================

/**
 * Initialiser le dashboard au chargement de la page
 */
async function initDashboard() {
    // Charger les statistiques
    const stats = await getDashboardStats();
    if (stats) {
        document.getElementById('totalMembers').textContent = stats.totalMembers;
        document.getElementById('totalTasks').textContent = stats.totalTasks;
        document.getElementById('activeProjects').textContent = stats.activeProjects;
    }

    // Charger les alertes
    const alerts = await getActiveAlerts();
    displayAlerts(alerts);

    // Charger la répartition de la charge
    const workload = await getWorkloadDistribution();
    displayWorkloadChart(workload);
}

/**
 * Afficher les alertes dans le dashboard
 */
function displayAlerts(alerts) {
    const alertContainer = document.getElementById('alertContainer');
    if (!alertContainer) return;

    alertContainer.innerHTML = '';

    if (alerts.length === 0) {
        alertContainer.innerHTML = '<p>No active alerts</p>';
        return;
    }

    alerts.forEach(alert => {
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${alert.severityLevel.toLowerCase()}`;
        alertDiv.innerHTML = `
            <strong>${alert.type}</strong>: ${alert.message}
            <span class="date">${formatDate(alert.issuedDate)}</span>
        `;
        alertContainer.appendChild(alertDiv);
    });
}

/**
 * Afficher le graphique de charge de travail
 */
function displayWorkloadChart(workload) {
    // Utiliser Chart.js ou une autre bibliothèque de graphiques
    // Exemple avec Chart.js

    const ctx = document.getElementById('workloadChart');
    if (!ctx) return;

    const memberNames = workload.members.map(m => m.memberName);
    const loads = workload.members.map(m => m.currentLoad);

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: memberNames,
            datasets: [{
                label: 'Workload (hours)',
                data: loads,
                backgroundColor: 'rgba(54, 162, 235, 0.5)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

// ============================================================================
// TEAMS
// ============================================================================

async function getAllTeams() {
    try {
        const response = await fetch(`${API_BASE_URL}/teams`);
        if (!response.ok) throw new Error('Error fetching teams');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return [];
    }
}

async function createTeam(teamData) {
    try {
        const response = await fetch(`${API_BASE_URL}/teams`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(teamData)
        });
        if (!response.ok) throw new Error('Error creating team');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return null;
    }
}

// ============================================================================
// ALERTS
// ============================================================================

async function getAllAlerts() {
    try {
        const response = await fetch(`${API_BASE_URL}/alerts`);
        if (!response.ok) throw new Error('Error fetching alerts');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return [];
    }
}

// ============================================================================
// SKILLS
// ============================================================================

async function getAllSkills() {
    try {
        const response = await fetch(`${API_BASE_URL}/skills`);
        if (!response.ok) throw new Error('Error fetching skills');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return [];
    }
}

// ============================================================================
// TASK DELETE
// ============================================================================

async function deleteTask(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/tasks?id=${id}`, {
            method: 'DELETE'
        });
        if (!response.ok) throw new Error('Error deleting task');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return null;
    }
}

// ============================================================================
// STATISTICS API
// ============================================================================

/**
 * Get project statistics
 */
async function getProjectStatistics() {
    try {
        const response = await fetch(`${API_BASE_URL}/statistics/project`);
        if (!response.ok) throw new Error('Error fetching statistics');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return null;
    }
}

/**
 * Get workload statistics with detailed member info
 */
async function getWorkloadStatistics() {
    try {
        const response = await fetch(`${API_BASE_URL}/statistics/workload`);
        if (!response.ok) throw new Error('Error fetching workload statistics');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return null;
    }
}

/**
 * Get skill coverage report
 */
async function getSkillCoverage() {
    try {
        const response = await fetch(`${API_BASE_URL}/statistics/skills`);
        if (!response.ok) throw new Error('Error fetching skills');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return null;
    }
}

/**
 * Get assignment details
 */
async function getAssignmentDetails() {
    try {
        const response = await fetch(`${API_BASE_URL}/statistics/assignments`);
        if (!response.ok) throw new Error('Error fetching assignments');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return null;
    }
}

/**
 * Get timeline data for visualization
 */
async function getTimelineData() {
    try {
        const response = await fetch(`${API_BASE_URL}/statistics/timeline`);
        if (!response.ok) throw new Error('Error fetching timeline data');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return null;
    }
}

/**
 * Get complete report
 */
async function getCompleteReport() {
    try {
        const response = await fetch(`${API_BASE_URL}/statistics/report`);
        if (!response.ok) throw new Error('Error generating report');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return null;
    }
}

// Export functions for global use
window.API = {
    // Members
    getAllMembers,
    getMemberById,
    createMember,
    updateMember,
    deleteMember,

    // Tasks
    getAllTasks,
    getUnassignedTasks,
    createTask,
    assignTask,
    deleteTask,

    // Assignment
    autoAssignTasks,
    assignUrgentTask,
    getAssignmentReport,

    // Dashboard
    getDashboardStats,
    getWorkloadDistribution,
    getActiveAlerts,
    getProjectProgress,

    // Projects
    getAllProjects,
    createProject,

    // Teams
    getAllTeams,
    createTeam,

    // Alerts
    getAllAlerts,

    // Skills
    getAllSkills,

    // Statistics
    getProjectStatistics,
    getWorkloadStatistics,
    getSkillCoverage,
    getAssignmentDetails,
    getTimelineData,
    getCompleteReport,

    // Utils
    formatDate,
    calculatePercentage
};
