// Configuration API
const API_CONFIG = {
    baseURL: 'http://localhost:8080/mini_projet/api',
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
};

// Classe pour gérer les appels API
class API {
    static async request(endpoint, options = {}) {
        const url = `${API_CONFIG.baseURL}${endpoint}`;
        const config = {
            ...options,
            headers: {
                ...API_CONFIG.headers,
                ...options.headers
            }
        };

        try {
            const response = await fetch(url, config);
            
            if (!response.ok) {
                throw new Error(`HTTP Error: ${response.status}`);
            }

            // Si la réponse est vide (204 No Content), retourner null
            if (response.status === 204) {
                return null;
            }

            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }

    // Méthodes HTTP
    static get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    }

    static post(endpoint, data) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    static put(endpoint, data) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }

    static delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
}

// Services API spécifiques
const MemberAPI = {
    getAll: () => API.get('/members'),
    getById: (id) => API.get(`/members?id=${id}`),
    getByTeam: (teamId) => API.get(`/members?teamId=${teamId}`),
    create: (data) => API.post('/members', data),
    update: (id, data) => API.put('/members', data),
    delete: (id) => API.delete(`/members?id=${id}`)
};

const TaskAPI = {
    getAll: () => API.get('/tasks'),
    getById: (id) => API.get(`/tasks?id=${id}`),
    getByStatus: (status) => API.get(`/tasks?status=${status}`),
    getByAssignee: (assigneeId) => API.get(`/tasks?memberId=${assigneeId}`),
    create: (data) => API.post('/tasks', data),
    update: (id, data) => API.put('/tasks', data),
    delete: (id) => API.delete(`/tasks?id=${id}`),
    assignAutomatically: (teamId) => API.post('/tasks/assign', { teamId })
};

const ProjectAPI = {
    getAll: () => API.get('/projects'),
    getById: (id) => API.get(`/projects?id=${id}`),
    create: (data) => API.post('/projects', data),
    update: (id, data) => API.put('/projects', data),
    delete: (id) => API.delete(`/projects?id=${id}`)
};

const AlertAPI = {
    getAll: () => API.get('/alerts'),
    getById: (id) => API.get(`/alerts?id=${id}`),
    getBySeverity: (severity) => API.get(`/alerts?severity=${severity}`),
    create: (data) => API.post('/alerts', data),
    delete: (id) => API.delete(`/alerts?id=${id}`)
};

const TeamAPI = {
    getAll: () => API.get('/teams'),
    getById: (id) => API.get(`/teams?id=${id}`),
    create: (data) => API.post('/teams', data),
    update: (id, data) => API.put('/teams', data),
    delete: (id) => API.delete(`/teams?id=${id}`)
};
