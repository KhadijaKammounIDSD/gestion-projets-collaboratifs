/**
 * Authentication JavaScript for Login and Signup
 */

const API_BASE_URL = 'http://localhost:8080/mini_projet/api';

// ============================================================================
// SIGNUP FUNCTIONALITY
// ============================================================================

function initSignupPage() {
    const form = document.querySelector('form');
    const loginBtn = document.querySelector('.header .login-btn');
    
    // Redirect to login page when clicking login button
    if (loginBtn) {
        loginBtn.addEventListener('click', () => {
            window.location.href = 'login-page.html';
        });
    }
    
    if (form) {
        form.addEventListener('submit', handleSignup);
    }
}

async function handleSignup(e) {
    e.preventDefault();
    
    // Get form values
    const firstName = document.getElementById('firstname').value.trim();
    const lastName = document.getElementById('lastname').value.trim();
    const email = document.getElementById('email').value.trim();
    const roleInput = document.getElementById('role');
    const role = roleInput && roleInput.value ? roleInput.value.trim() : 'Member';
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirm-password').value;
    
    // Validate required fields
    if (!firstName) {
        showAlert('error', 'First name is required');
        document.getElementById('firstname').focus();
        return;
    }
    
    if (!lastName) {
        showAlert('error', 'Last name is required');
        document.getElementById('lastname').focus();
        return;
    }
    
    if (!email) {
        showAlert('error', 'Email is required');
        document.getElementById('email').focus();
        return;
    }
    
    // Validate email format
    if (!isValidEmail(email)) {
        showAlert('error', 'Please enter a valid email address');
        document.getElementById('email').focus();
        return;
    }
    
    if (!password) {
        showAlert('error', 'Password is required');
        document.getElementById('password').focus();
        return;
    }
    
    // Validate password length
    if (password.length < 8) {
        showAlert('error', 'Password must be at least 8 characters');
        document.getElementById('password').focus();
        return;
    }
    
    if (!confirmPassword) {
        showAlert('error', 'Please confirm your password');
        document.getElementById('confirm-password').focus();
        return;
    }
    
    // Validate password match
    if (password !== confirmPassword) {
        showAlert('error', 'Passwords do not match');
        document.getElementById('confirm-password').focus();
        document.getElementById('confirm-password').value = '';
        return;
    }
    
    // Get selected skills
    const skills = getSelectedSkills();
    
    // Prepare signup data
    const signupData = {
        firstName: firstName,
        lastName: lastName,
        email: email,
        password: password,
        role: role || 'Member'
    };
    
    try {
        // Show loading
        const submitBtn = e.target.querySelector('button[type="submit"]');
        const originalText = submitBtn.textContent;
        submitBtn.disabled = true;
        submitBtn.textContent = 'Signing up...';
        
        const response = await fetch(`${API_BASE_URL}/auth/signup`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(signupData)
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            // Store skills for new member if any were selected
            const createdMemberId = data.memberId || (data.member && data.member.id);
            if (skills.length > 0 && createdMemberId) {
                await storeSignupSkills(createdMemberId, skills);
            }
            
            showAlert('success', 'Account created successfully! Redirecting to login...');
            
            // Clear form
            e.target.reset();
            
            // Redirect to login page after 2 seconds
            setTimeout(() => {
                window.location.href = 'login-page.html';
            }, 2000);
        } else {
            showAlert('error', data.error || 'Signup failed. Please try again.');
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }
    } catch (error) {
        console.error('Signup error:', error);
        showAlert('error', 'Network error. Please check if the server is running.');
        const submitBtn = e.target.querySelector('button[type="submit"]');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Sign Up';
    }
}

async function storeSignupSkills(memberId, skills) {
    if (!skills || skills.length === 0) return;
    try {
        const needsLookup = skills.some(s => !s.id);
        let availableSkills = [];
        if (needsLookup) {
            const skillsResponse = await fetch(`${API_BASE_URL}/skills`);
            if (!skillsResponse.ok) {
                console.error('Failed to fetch skills');
                return;
            }
            availableSkills = await skillsResponse.json();
        }
        for (const skillEntry of skills) {
            const skillName = typeof skillEntry === 'string' ? skillEntry : (skillEntry?.name || '');
            const skillId = skillEntry?.id || (availableSkills.find(s => s.name.toLowerCase() === skillName.toLowerCase())?.id);
            if (!skillId) {
                console.warn(`Skill not found in system: ${skillName}`);
                continue;
            }
            const memberSkillData = {
                memberId: memberId,
                skillId: skillId
            };
            try {
                const addSkillResponse = await fetch(`${API_BASE_URL}/member-skills`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(memberSkillData)
                });
                if (!addSkillResponse.ok) {
                    console.warn(`Failed to add skill: ${skillName || skillId}`);
                }
            } catch (error) {
                console.warn(`Error adding skill ${skillName || skillId}:`, error);
            }
        }
    } catch (error) {
        console.error('Error storing signup skills:', error);
    }
}

function getSelectedSkills() {
    if (typeof selectedSkills !== 'undefined') {
        return selectedSkills.map(s => {
            if (typeof s === 'string') {
                return { name: s };
            }
            return s;
        });
    }
    return [];
}

// ============================================================================
// LOGIN FUNCTIONALITY
// ============================================================================

function initLoginPage() {
    const form = document.querySelector('form');
    const signupBtn = document.querySelector('.header .login-btn');
    
    // Redirect to signup page when clicking signup button
    if (signupBtn) {
        signupBtn.addEventListener('click', () => {
            window.location.href = 'sign-up-page.html';
        });
    }
    
    if (form) {
        form.addEventListener('submit', handleLogin);
    }
}

async function handleLogin(e) {
    e.preventDefault();
    
    // Get form values
    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;
    
    // Validate required fields
    if (!email) {
        showAlert('error', 'Email is required');
        document.getElementById('login-email').focus();
        return;
    }
    
    if (!isValidEmail(email)) {
        showAlert('error', 'Please enter a valid email address');
        document.getElementById('login-email').focus();
        return;
    }
    
    if (!password) {
        showAlert('error', 'Password is required');
        document.getElementById('login-password').focus();
        return;
    }
    
    // Prepare login data
    const loginData = {
        email: email,
        password: password
    };
    
    try {
        // Show loading
        const submitBtn = e.target.querySelector('button[type="submit"]');
        const originalText = submitBtn.textContent;
        submitBtn.disabled = true;
        submitBtn.textContent = 'Logging in...';
        
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginData)
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            // Store user data in session storage
            sessionStorage.setItem('user', JSON.stringify(data.member));
            sessionStorage.setItem('isLoggedIn', 'true');
            
            showAlert('success', 'Login successful! Redirecting...');
            
            // Redirect to dashboard/home page after 1 second
            setTimeout(() => {
                window.location.href = 'dashboard-page.html';
            }, 1000);
        } else {
            showAlert('error', data.error || 'Invalid email or password');
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
            
            // Clear password field
            document.getElementById('login-password').value = '';
        }
    } catch (error) {
        console.error('Login error:', error);
        showAlert('error', 'Network error. Please check if the server is running.');
        const submitBtn = e.target.querySelector('button[type="submit"]');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Log in';
    }
}

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function showAlert(type, message) {
    // Remove existing alerts
    const existingAlert = document.querySelector('.auth-alert');
    if (existingAlert) {
        existingAlert.remove();
    }
    
    // Create alert element
    const alert = document.createElement('div');
    alert.className = `auth-alert auth-alert-${type}`;
    alert.innerHTML = `
        <div class="auth-alert-content">
            <span class="auth-alert-icon">${type === 'success' ? '✓' : '⚠'}</span>
            <span class="auth-alert-message">${message}</span>
            <button class="auth-alert-close" onclick="this.parentElement.parentElement.remove()">×</button>
        </div>
    `;
    
    // Add to page
    const container = document.querySelector('.container');
    if (container) {
        container.insertBefore(alert, container.firstChild);
    }
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (alert.parentElement) {
            alert.remove();
        }
    }, 5000);
}

// Check if user is already logged in
function checkAuth() {
    const isLoggedIn = sessionStorage.getItem('isLoggedIn');
    const currentPage = window.location.pathname;
    
    // If logged in and on login/signup page, redirect to dashboard
    if (isLoggedIn === 'true' && (currentPage.includes('login') || currentPage.includes('sign-up'))) {
        window.location.href = 'dashboard-page.html';
    }
}

// Logout function
async function logout() {
    try {
        await fetch(`${API_BASE_URL}/auth/logout`, {
            method: 'POST'
        });
    } catch (error) {
        console.error('Logout error:', error);
    }
    
    // Clear session storage
    sessionStorage.clear();
    
    // Redirect to login page
    window.location.href = 'login-page.html';
}

// Get current user
function getCurrentUser() {
    const userStr = sessionStorage.getItem('user');
    if (userStr) {
        return JSON.parse(userStr);
    }
    return null;
}

// ============================================================================
// INITIALIZATION
// ============================================================================

// Auto-initialize based on page
document.addEventListener('DOMContentLoaded', () => {
    const currentPage = window.location.pathname;
    
    if (currentPage.includes('sign-up')) {
        initSignupPage();
    } else if (currentPage.includes('login')) {
        initLoginPage();
    }
    
    // Check authentication status
    checkAuth();
});
