// API Configuration
const API_BASE = '/api';
let rolesList = [];
let userModal;
let currentUser = null;

// Initialize on page load
$(document).ready(function () {
    userModal = new bootstrap.Modal(document.getElementById('userModal'));
    loadCurrentUser();
    loadRoles();
    loadUsers();

    // Form submit handler
    $('#userForm').on('submit', function (e) {
        e.preventDefault();
        saveUser();
    });
});

// Load current authenticated user
async function loadCurrentUser() {
    try {
        const response = await fetch(`${API_BASE}/user`);
        const result = await response.json();

        if (result.success) {
            currentUser = result.data;
        }
    } catch (error) {
        console.error('Error loading current user:', error);
    }
}

// Load all roles
async function loadRoles() {
    try {
        const response = await fetch(`${API_BASE}/admin/roles`);
        const result = await response.json();

        if (result.success) {
            rolesList = result.data;
            displayRolesCheckboxes();
        }
    } catch (error) {
        console.error('Error loading roles:', error);
        showToast('Error loading roles', 'error');
    }
}

// Display roles as checkboxes
function displayRolesCheckboxes() {
    const container = $('#rolesContainer');
    container.empty();

    if (rolesList.length === 0) {
        container.append('<div class="text-muted">No roles available</div>');
        return;
    }

    rolesList.forEach(role => {
        container.append(`
            <div class="form-check">
                <input class="form-check-input" type="checkbox" value="${role}" id="role_${role}">
                <label class="form-check-label" for="role_${role}">
                    ${role}
                </label>
            </div>
        `);
    });
}

// Load all users
async function loadUsers() {
    try {
        const response = await fetch(`${API_BASE}/admin/users`);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const result = await response.json();

        if (result.success) {
            displayUsers(result.data);
        } else {
            showToast(result.message, 'error');
        }
    } catch (error) {
        console.error('Error loading users:', error);
        $('#usersTableBody').html(`
            <tr>
                <td colspan="8" class="text-center text-danger">
                    Error loading users: ${error.message}
                </td>
            </tr>
        `);
    }
}

// Display users in table
function displayUsers(users) {
    const tbody = $('#usersTableBody');

    if (!users || users.length === 0) {
        tbody.html(`
            <tr>
                <td colspan="8" class="text-center">No users found</td>
            </tr>
        `);
        return;
    }

    tbody.empty();

    users.forEach(user => {
        const rolesDisplay = user.roles ? user.roles.join(', ') : '-';
        const isCurrentUser = currentUser && currentUser.id === user.id;

        tbody.append(`
            <tr>
                <td>${escapeHtml(user.id)}</td>
                <td>${escapeHtml(user.firstname)}</td>
                <td>${escapeHtml(user.surname)}</td>
                <td>${escapeHtml(user.email)}</td>
                <td>${user.birthday || '-'}</td>
                <td>${user.candrive ? 'Yes' : 'No'}</td>
                <td>${escapeHtml(rolesDisplay)}</td>
                <td>
                    <button class="btn btn-sm btn-info" onclick="editUser(${user.id})">Edit</button>
                    <button class="btn btn-sm btn-danger" onclick="deleteUser(${user.id})" 
                        ${isCurrentUser ? 'disabled' : ''}>Delete</button>
                </td>
            </tr>
        `);
    });
}

// Show add modal
function showAddModal() {
    $('#modalTitle').text('Add New User');
    $('#userForm')[0].reset();
    $('#userId').val('');
    $('#passwordField').show();
    $('#password').prop('required', true);
    $('input[type="checkbox"]').prop('checked', false);
    $('.invalid-feedback').hide();
    userModal.show();
}

// Edit user
async function editUser(id) {
    console.log('Editing user:', id);
    try {
        // ✅ Правильный URL для GET запроса
        const response = await fetch(`${API_BASE}/admin/users/${id}`);

        if (!response.ok) {
            if (response.status === 404) {
                showToast('User not found', 'error');
                return;
            }
            throw new Error(`HTTP ${response.status}`);
        }

        const result = await response.json();
        console.log('User data:', result);

        if (result.success) {
            const user = result.data;
            $('#modalTitle').text('Edit User');
            $('#userId').val(user.id);
            $('#firstname').val(user.firstname);
            $('#surname').val(user.surname);
            $('#email').val(user.email);
            $('#birthday').val(user.birthday);
            $('#candrive').prop('checked', user.candrive);
            $('#passwordField').hide();
            $('#password').prop('required', false);

            // Установить чекбоксы ролей
            $('input[type="checkbox"]').prop('checked', false);
            if (user.roles && user.roles.length > 0) {
                user.roles.forEach(role => {
                    const checkboxId = `role_${role.replace(/[^a-zA-Z0-9]/g, '_')}`;
                    $(`#${checkboxId}`).prop('checked', true);
                });
            }

            userModal.show();
        } else {
            showToast(result.message, 'error');
        }
    } catch (error) {
        console.error('Error loading user:', error);
        showToast('Error loading user: ' + error.message, 'error');
    }
}

// Save user (create or update)
async function saveUser() {
    const userId = $('#userId').val();
    const isEdit = userId !== '';

    // Validate form
    if (!validateForm()) {
        return;
    }

    // Collect selected roles
    const selectedRoles = [];
    $('input[type="checkbox"]:checked').each(function () {
        selectedRoles.push($(this).val());
    });

    const userData = {
        firstname: $('#firstname').val().trim(),
        surname: $('#surname').val().trim(),
        email: $('#email').val().trim(),
        birthday: $('#birthday').val(),
        candrive: $('#candrive').is(':checked'),
        roleNames: selectedRoles
    };

    if (!isEdit) {
        userData.password = $('#password').val();
    }

    try {
        let response;

        if (isEdit) {
            response = await fetch(`${API_BASE}/admin/users/${userId}`, {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(userData)
            });
        } else {
            response = await fetch(`${API_BASE}/admin/users`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(userData)
            });
        }

        const result = await response.json();

        if (response.ok && result.success) {
            userModal.hide();
            await loadUsers();
            showToast(isEdit ? 'User updated successfully' : 'User created successfully', 'success');
        } else {
            showToast(result.message || 'Error saving user', 'error');
        }
    } catch (error) {
        console.error('Error saving user:', error);
        showToast('Error saving user', 'error');
    }
}

// Delete user
async function deleteUser(id) {
    if (!confirm('Are you sure you want to delete this user?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/admin/users/${id}`, {
            method: 'DELETE'
        });

        const result = await response.json();

        if (response.ok && result.success) {
            await loadUsers();
            showToast('User deleted successfully', 'success');
        } else {
            showToast(result.message || 'Error deleting user', 'error');
        }
    } catch (error) {
        console.error('Error deleting user:', error);
        showToast('Error deleting user', 'error');
    }
}

// Form validation
function validateForm() {
    let isValid = true;

    if (!$('#firstname').val().trim()) {
        $('#firstname').addClass('is-invalid');
        isValid = false;
    } else {
        $('#firstname').removeClass('is-invalid');
    }

    if (!$('#surname').val().trim()) {
        $('#surname').addClass('is-invalid');
        isValid = false;
    } else {
        $('#surname').removeClass('is-invalid');
    }

    if (!$('#email').val().trim()) {
        $('#email').addClass('is-invalid');
        isValid = false;
    } else {
        $('#email').removeClass('is-invalid');
    }

    if (!$('#birthday').val()) {
        $('#birthday').addClass('is-invalid');
        isValid = false;
    } else {
        $('#birthday').removeClass('is-invalid');
    }

    const isEdit = $('#userId').val() !== '';
    if (!isEdit && !$('#password').val()) {
        $('#password').addClass('is-invalid');
        isValid = false;
    } else {
        $('#password').removeClass('is-invalid');
    }

    return isValid;
}

// Toast notifications
function showToast(message, type = 'success') {
    const bgColor = type === 'success' ? 'bg-success' : 'bg-danger';
    const icon = type === 'success' ? '✓' : '✗';

    const toastHtml = `
        <div class="toast align-items-center text-white ${bgColor} border-0" role="alert">
            <div class="d-flex">
                <div class="toast-body">
                    <strong>${icon}</strong> ${escapeHtml(message)}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>
    `;

    // Remove existing toasts
    $('.toast').remove();

    // Add new toast
    $('body').append(`
        <div class="toast-container">
            ${toastHtml}
        </div>
    `);

    // Show toast
    const toast = new bootstrap.Toast($('.toast').last()[0]);
    toast.show();

    // Auto remove after 3 seconds
    setTimeout(() => {
        $('.toast').remove();
    }, 3000);
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}