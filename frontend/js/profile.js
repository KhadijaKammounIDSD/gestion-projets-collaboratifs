// Gestion du profil utilisateur
class ProfileManager {
    constructor() {
        // Pour l'instant, on utilise un utilisateur fictif
        // Dans une vraie application, on récupérerait l'ID depuis la session
        this.currentUserId = 1;
        this.currentUser = null;
        this.userSkills = [];
        this.init();
    }

    async init() {
        try {
            await this.loadUserProfile();
            this.setupEventListeners();
            this.render();
        } catch (error) {
            console.error('Profile initialization error:', error);
            this.showError('Impossible de charger le profil');
        }
    }

    async loadUserProfile() {
        try {
            // Charger les informations de l'utilisateur
            this.currentUser = await MemberAPI.getById(this.currentUserId);
            
            // Charger toutes les compétences disponibles
            // Note: Would need to create a SkillAPI in api.js
            // Pour l'instant on utilise des données fictives
            this.userSkills = [
                { id: 1, name: 'Python', level: 4 },
                { id: 2, name: 'UI/UX Design', level: 5 },
                { id: 3, name: 'Machine Learning', level: 3 }
            ];

            console.log('Profile loaded:', this.currentUser);
        } catch (error) {
            console.error('Profile loading error:', error);
            // Utiliser des données par défaut pour la démonstration
            this.currentUser = {
                id: 1,
                firstName: 'Adam',
                lastName: 'Ghorbel',
                email: 'adamghorbel@gmail.com',
                role: 'Développeur',
                currentLoad: 120,
                teamId: 1
            };
        }
    }

    setupEventListeners() {
        // Bouton d'ajout de compétence
        const addSkillBtn = document.querySelector('.add-skill-btn');
        if (addSkillBtn) {
            addSkillBtn.addEventListener('click', () => this.showAddSkillModal());
        }

        // Permettre la modification du profil
        document.querySelectorAll('.info-item').forEach(item => {
            item.addEventListener('dblclick', (e) => {
                this.makeEditable(e.currentTarget);
            });
        });
    }

    render() {
        if (!this.currentUser) return;

        // Mettre à jour les informations utilisateur
        const nameElement = document.querySelector('.info-item:nth-child(1) p');
        if (nameElement) {
            nameElement.textContent = `${this.currentUser.firstName} ${this.currentUser.lastName}`;
        }

        const emailElement = document.querySelector('.info-item:nth-child(2) p');
        if (emailElement) {
            emailElement.textContent = this.currentUser.email;
        }

        // Mettre à jour les avatars
        document.querySelectorAll('.avatar, .large-avatar').forEach(avatar => {
            const initials = `${this.currentUser.firstName?.charAt(0) || ''}${this.currentUser.lastName?.charAt(0) || ''}`;
            avatar.textContent = initials.toUpperCase();
        });

        // Rendre les compétences
        this.renderSkills();
    }

    renderSkills() {
        const container = document.querySelector('.skills-container');
        if (!container) return;

        container.innerHTML = this.userSkills.map(skill => `
            <span class="skill-tag" data-skill-id="${skill.id}">
                ${this.escapeHtml(skill.name)}
                <span class="skill-level" title="Niveau ${skill.level}/5">
                    ${'★'.repeat(skill.level)}${'☆'.repeat(5 - skill.level)}
                </span>
                <button class="remove-skill" onclick="profileManager.removeSkill(${skill.id})">
                    <i class="fas fa-times"></i>
                </button>
            </span>
        `).join('');
    }

    showAddSkillModal() {
        const modal = this.createModal('Add Skill', `
            <form id="skillForm">
                <div class="form-group">
                    <label>Nom de la compétence *</label>
                    <input type="text" name="skillName" required class="form-input" 
                           placeholder="e.g., JavaScript, Design, Project management...">
                </div>
                <div class="form-group">
                    <label>Niveau de maîtrise (1-5)</label>
                    <div class="skill-level-selector">
                        <input type="range" name="level" min="1" max="5" value="3" 
                               class="form-range" id="skillLevelRange">
                        <span id="skillLevelValue">3</span>/5
                    </div>
                </div>
                <div class="modal-actions">
                    <button type="button" class="btn-cancel" onclick="profileManager.closeModal()">Annuler</button>
                    <button type="submit" class="btn-primary">Add</button>
                </div>
            </form>
        `);

        // Mise à jour en temps réel du niveau
        const rangeInput = document.getElementById('skillLevelRange');
        const levelValue = document.getElementById('skillLevelValue');
        rangeInput?.addEventListener('input', (e) => {
            levelValue.textContent = e.target.value;
        });

        document.getElementById('skillForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            await this.addSkill(new FormData(e.target));
        });
    }

    async addSkill(formData) {
        try {
            const skillName = formData.get('skillName');
            const level = parseInt(formData.get('level'));

            // Create the new skill
            const newSkill = {
                id: Date.now(), // ID temporaire
                name: skillName,
                level: level
            };

            this.userSkills.push(newSkill);
            this.renderSkills();
            this.closeModal();
            this.showSuccess('Compétence ajoutée avec succès!');

            // TODO: Enregistrer dans la base de données via API
            // await SkillAPI.create(newSkill);
        } catch (error) {
            this.showError('Erreur lors de l\'ajout de la compétence');
        }
    }

    async removeSkill(skillId) {
        if (!confirm('Are you sure you want to delete this skill?')) return;

        try {
            this.userSkills = this.userSkills.filter(s => s.id !== skillId);
            this.renderSkills();
            this.showSuccess('Compétence supprimée');

            // TODO: Supprimer de la base de données via API
            // await SkillAPI.delete(skillId);
        } catch (error) {
            this.showError('Erreur lors de la suppression');
        }
    }

    makeEditable(element) {
        const paragraph = element.querySelector('p');
        if (!paragraph) return;

        const currentValue = paragraph.textContent;
        const input = document.createElement('input');
        input.type = 'text';
        input.value = currentValue;
        input.className = 'form-input';

        paragraph.replaceWith(input);
        input.focus();

        const saveEdit = async () => {
            const newValue = input.value;
            const newParagraph = document.createElement('p');
            newParagraph.textContent = newValue;
            input.replaceWith(newParagraph);

            // TODO: Sauvegarder dans la base de données
            // await MemberAPI.update(this.currentUserId, { ... });
        };

        input.addEventListener('blur', saveEdit);
        input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') saveEdit();
        });
    }

    // Utility Methods
    createModal(title, content) {
        const existingModal = document.querySelector('.modal-overlay');
        if (existingModal) existingModal.remove();

        const modal = document.createElement('div');
        modal.className = 'modal-overlay';
        modal.innerHTML = `
            <div class="modal-content">
                <div class="modal-header">
                    <h2>${title}</h2>
                    <button class="btn-close-modal" onclick="profileManager.closeModal()">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                <div class="modal-body">
                    ${content}
                </div>
            </div>
        `;

        document.body.appendChild(modal);
        return modal;
    }

    closeModal() {
        const modal = document.querySelector('.modal-overlay');
        if (modal) modal.remove();
    }

    showSuccess(message) {
        this.showNotification(message, 'success');
    }

    showError(message) {
        this.showNotification(message, 'error');
    }

    showNotification(message, type) {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.innerHTML = `
            <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
            <span>${message}</span>
        `;

        document.body.appendChild(notification);

        setTimeout(() => {
            notification.classList.add('show');
        }, 100);

        setTimeout(() => {
            notification.classList.remove('show');
            setTimeout(() => notification.remove(), 300);
        }, 3000);
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Initialiser le gestionnaire de profil
let profileManager;
document.addEventListener('DOMContentLoaded', () => {
    profileManager = new ProfileManager();
});
