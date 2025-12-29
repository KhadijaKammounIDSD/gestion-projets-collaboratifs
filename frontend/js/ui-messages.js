// Centralized UI messages for the frontend (simple, no full i18n)
// All user-visible text should use English strings from here when reused

const UI_MESSAGES = {
  errors: {
    serverConnection: 'Server connection error. Please verify the server is running.',
    genericLoad: 'An error occurred while loading data.',
    loadMembers: 'Unable to load members.',
    loadTasks: 'Unable to load tasks.',
    loadProjects: 'Unable to load projects.',
    loadTeams: 'Unable to load teams.',
    loadAlerts: 'Unable to load alerts.',
  },
  confirmations: {
    deleteMember: 'Are you sure you want to delete this member?',
    deleteTask: 'Are you sure you want to delete this task?',
    deleteTeam: 'Are you sure you want to delete this team? Members will be unassigned.',
    deleteSkill: 'Are you sure you want to delete this skill?',
  },
  emptyStates: {
    noMembers: 'No members found. Add one!',
    noTeams: 'No teams found. Create one!',
    noSkills: 'No skills found. Add one!',
    noTasks: 'No tasks found. Create one!',
    noProjects: 'No projects found. Create one!',
  },
};

// Expose globally for inline scripts
window.UI_MESSAGES = UI_MESSAGES;


