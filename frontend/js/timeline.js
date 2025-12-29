// timeline.js est maintenant intégré avec api.js
// La variable API_BASE_URL n'est plus nécessaire car elle est dans api.js

let calendar;
let allTasks = [];
let allMembers = [];
let allTeams = [];

// Initialisation
document.addEventListener('DOMContentLoaded', function() {
    initializeCalendar();
    loadAllData();
    setupEventListeners();
});

/**
 * Initialiser le calendrier FullCalendar
 */
function initializeCalendar() {
    const calendarEl = document.getElementById('calendar');
    
    calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,timeGridWeek,timeGridDay,listMonth'
        },
        locale: 'en',
        buttonText: {
            today: 'Today',
            month: 'Month',
            week: 'Week',
            day: 'Day',
            list: 'List'
        },
        weekends: true,
        editable: true,
        droppable: true,
        eventClick: function(info) {
            showTaskDetails(info.event);
        },
        eventDrop: function(info) {
            updateTaskDates(info.event);
        },
        eventResize: function(info) {
            updateTaskDates(info.event);
        },
        events: []
    });
    
    calendar.render();
}

/**
 * Charger toutes les données
 */
async function loadAllData() {
    try {
        await Promise.all([
            loadTasks(),
            loadMembers(),
            loadTeams()
        ]);
        
        updateCalendar();
        updateStatistics();
        updateWorkloadBars();
        populateFilters();
    } catch (error) {
        console.error('Error loading data:', error);
        showNotification('Error loading data', 'error');
    }
}

/**
 * Charger les tâches
 */
async function loadTasks() {
    try {
        allTasks = await TaskAPI.getAll() || [];
    } catch (error) {
        console.error('Error loading tasks:', error);
        allTasks = [];
    }
}

/**
 * Charger les membres
 */
async function loadMembers() {
    try {
        allMembers = await MemberAPI.getAll() || [];
    } catch (error) {
        console.error('Error loading members:', error);
        allMembers = [];
    }
}

/**
 * Charger les équipes
 */
async function loadTeams() {
    try {
        allTeams = await TeamAPI.getAll() || [];
    } catch (error) {
        console.error('Error loading teams:', error);
        allTeams = [];
    }
}

/**
 * Mettre à jour le calendrier avec les tâches
 */
function updateCalendar() {
    const events = allTasks
        .filter(task => task.plannedStartDate && task.plannedEndDate)
        .map(task => {
            const member = allMembers.find(m => m.id === task.assigneeId);
            const priorityClass = getPriorityClass(task.priority);
            
            return {
                id: task.id,
                title: task.name,
                start: task.plannedStartDate,
                end: task.plannedEndDate,
                className: priorityClass,
                extendedProps: {
                    task: task,
                    member: member,
                    description: task.description,
                    priority: task.priority,
                    status: task.status,
                    duration: task.estimatedDuration
                }
            };
        });
    
    calendar.removeAllEvents();
    calendar.addEventSource(events);
}

/**
 * Obtenir la classe CSS selon la priorité
 */
function getPriorityClass(priority) {
    if (!priority) return 'fc-event-low';
    
    switch (priority.toUpperCase()) {
        case 'HAUTE':
        case 'HIGH':
            return 'fc-event-high';
        case 'MOYENNE':
        case 'MEDIUM':
            return 'fc-event-medium';
        case 'BASSE':
        case 'LOW':
            return 'fc-event-low';
        default:
            return 'fc-event-low';
    }
}

/**
 * Mettre à jour les statistiques
 */
function updateStatistics() {
    const totalTasks = allTasks.length;
    const inProgressTasks = allTasks.filter(t => 
        t.status && (t.status.toUpperCase() === 'EN_COURS' || t.status === 'En cours')
    ).length;
    const completedTasks = allTasks.filter(t => 
        t.status && (t.status.toUpperCase() === 'TERMINÉE' || t.status === 'Terminée')
    ).length;
    const activeMembers = allMembers.filter(m => m.available).length;
    
    document.getElementById('totalTasks').textContent = totalTasks;
    document.getElementById('inProgressTasks').textContent = inProgressTasks;
    document.getElementById('completedTasks').textContent = completedTasks;
    document.getElementById('activeMembers').textContent = activeMembers;
}

/**
 * Mettre à jour les barres de charge de travail
 */
function updateWorkloadBars() {
    const container = document.getElementById('workloadBars');
    container.innerHTML = '';
    
    if (allMembers.length === 0) {
        container.innerHTML = '<p style="color: #999; text-align: center;">Aucun membre disponible</p>';
        return;
    }
    
    const maxLoad = 160; // Seuil maximum
    
    allMembers.forEach(member => {
        const load = member.currentLoad || 0;
        const percentage = (load / maxLoad) * 100;
        const displayPercentage = Math.min(percentage, 100);
        
        let barClass = 'workload-normal';
        if (load > 160) {
            barClass = 'workload-critical';
        } else if (load > 140) {
            barClass = 'workload-warning';
        }
        
        const itemDiv = document.createElement('div');
        itemDiv.className = 'workload-item';
        itemDiv.innerHTML = `
            <div class="workload-name">${member.firstName} ${member.lastName}</div>
            <div class="workload-bar-container">
                <div class="workload-bar ${barClass}" style="width: ${displayPercentage}%">
                    ${load}h
                </div>
            </div>
        `;
        
        container.appendChild(itemDiv);
    });
}

/**
 * Peupler les filtres
 */
function populateFilters() {
    // Peupler le sélecteur d'équipes
    const teamSelector = document.getElementById('teamSelector');
    teamSelector.innerHTML = '<option value="">All teams</option>';
    allTeams.forEach(team => {
        const option = document.createElement('option');
        option.value = team.id;
        option.textContent = team.name;
        teamSelector.appendChild(option);
    });
    
    // Populate member selector
    const memberSelector = document.getElementById('memberSelector');
    memberSelector.innerHTML = '<option value="">All members</option>';
    allMembers.forEach(member => {
        const option = document.createElement('option');
        option.value = member.id;
        option.textContent = `${member.firstName} ${member.lastName}`;
        memberSelector.appendChild(option);
    });
}

/**
 * Configurer les écouteurs d'événements
 */
function setupEventListeners() {
    document.getElementById('viewSelector').addEventListener('change', function(e) {
        calendar.changeView(e.target.value);
    });
    
    document.getElementById('memberSelector').addEventListener('change', filterTasks);
    document.getElementById('priorityFilter').addEventListener('change', filterTasks);
}

/**
 * Filtrer les tâches
 */
function filterTasks() {
    const memberId = document.getElementById('memberSelector').value;
    const priority = document.getElementById('priorityFilter').value;
    
    let filteredTasks = allTasks;
    
    if (memberId) {
        filteredTasks = filteredTasks.filter(t => t.assigneeId == memberId);
    }
    
    if (priority) {
        filteredTasks = filteredTasks.filter(t => 
            t.priority && t.priority.toUpperCase() === priority.toUpperCase()
        );
    }
    
    // Mettre à jour le calendrier avec les tâches filtrées
    const events = filteredTasks
        .filter(task => task.plannedStartDate && task.plannedEndDate)
        .map(task => {
            const member = allMembers.find(m => m.id === task.assigneeId);
            const priorityClass = getPriorityClass(task.priority);
            
            return {
                id: task.id,
                title: task.name,
                start: task.plannedStartDate,
                end: task.plannedEndDate,
                className: priorityClass,
                extendedProps: {
                    task: task,
                    member: member,
                    description: task.description,
                    priority: task.priority,
                    status: task.status,
                    duration: task.estimatedDuration
                }
            };
        });
    
    calendar.removeAllEvents();
    calendar.addEventSource(events);
}

/**
 * Afficher les détails d'une tâche
 */
function showTaskDetails(event) {
    const props = event.extendedProps;
    const memberName = props.member ? 
        `${props.member.firstName} ${props.member.lastName}` : 
        'Unassigned';
    
    const details = `
        Task: ${event.title}
        Description: ${props.description || 'N/A'}
        Assigned to: ${memberName}
        Priority: ${props.priority || 'N/A'}
        Status: ${props.status || 'N/A'}
        Estimated duration: ${props.duration}h
        Start: ${formatDate(event.start)}
        End: ${formatDate(event.end)}
    `;
    
    alert(details);
}

/**
 * Mettre à jour les dates d'une tâche après déplacement
 */
async function updateTaskDates(event) {
    const task = event.extendedProps.task;
    task.plannedStartDate = event.start.toISOString().split('T')[0];
    task.plannedEndDate = event.end ? event.end.toISOString().split('T')[0] : task.plannedStartDate;
    
    try {
        await TaskAPI.update(task.id, task);
        showNotification('Task updated successfully', 'success');
    } catch (error) {
        console.error('Error:', error);
        showNotification('Connection error', 'error');
    }
}

/**
 * Rafraîchir la timeline
 */
function refreshTimeline() {
    loadAllData();
    showNotification('Timeline refreshed', 'success');
}

/**
 * Exporter le calendrier
 */
function exportCalendar() {
    const events = calendar.getEvents();
    const csvContent = generateCSV(events);
    downloadFile(csvContent, 'timeline.csv', 'text/csv');
}

/**
 * Générer un fichier CSV
 */
function generateCSV(events) {
    let csv = 'Task,Start,End,Assigned to,Priority,Status,Duration\n';
    
    events.forEach(event => {
        const props = event.extendedProps;
        const memberName = props.member ? 
            `${props.member.firstName} ${props.member.lastName}` : 
            'Unassigned';
        
        csv += `"${event.title}",`;
        csv += `"${formatDate(event.start)}",`;
        csv += `"${formatDate(event.end)}",`;
        csv += `"${memberName}",`;
        csv += `"${props.priority || 'N/A'}",`;
        csv += `"${props.status || 'N/A'}",`;
        csv += `"${props.duration}h"\n`;
    });
    
    return csv;
}

/**
 * Télécharger un fichier
 */
function downloadFile(content, filename, contentType) {
    const blob = new Blob([content], { type: contentType });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    window.URL.revokeObjectURL(url);
}

/**
 * Afficher une notification
 */
function showNotification(message, type) {
    // Implémentation simple avec alert
    // À remplacer par une bibliothèque de notifications si souhaité
    if (type === 'success') {
        console.log('✓ ' + message);
    } else {
        console.error('✗ ' + message);
    }
}

/**
 * Formater une date
 */
function formatDate(date) {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString('en-US');
}
