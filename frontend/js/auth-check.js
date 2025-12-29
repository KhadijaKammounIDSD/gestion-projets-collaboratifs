/**
 * Authentication Check for Protected Pages
 * Include this script in pages that require authentication
 */

// Check if user is logged in
function requireAuth() {
    const isLoggedIn = sessionStorage.getItem('isLoggedIn');
    const currentPage = window.location.pathname;
    
    // If not logged in and not on login/signup page, redirect to login
    if (isLoggedIn !== 'true' && !currentPage.includes('login') && !currentPage.includes('sign-up')) {
        window.location.href = 'login-page.html';
        return false;
    }
    
    return true;
}

// Display current user info
function displayUserInfo() {
    const user = getCurrentUser();
    if (user) {
        const userElements = document.querySelectorAll('.user-name');
        userElements.forEach(el => {
            el.textContent = `${user.firstName} ${user.lastName}`;
        });
        
        const userEmailElements = document.querySelectorAll('.user-email');
        userEmailElements.forEach(el => {
            el.textContent = user.email;
        });
    }
}

// Get current user from session storage
function getCurrentUser() {
    const userStr = sessionStorage.getItem('user');
    if (userStr) {
        try {
            return JSON.parse(userStr);
        } catch (e) {
            return null;
        }
    }
    return null;
}

// Logout function
async function logout() {
    try {
        const API_BASE_URL = 'http://localhost:8080/mini_projet/api';
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

// Initialize authentication on page load
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        requireAuth();
        displayUserInfo();
    });
} else {
    requireAuth();
    displayUserInfo();
}
