const API_BASE_URL = 'http://localhost:8080/api';
const token = localStorage.getItem('token');
const userRole = localStorage.getItem('userRole');

if (!token || userRole !== 'ADMIN') {
  window.location.href = 'login.html';
}

document.getElementById('adminName').textContent = localStorage.getItem('userName') || 'Admin';

async function fetchAllUsers() {
  try {
    const response = await fetch(`${API_BASE_URL}/auth/internal/users`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
        logout();
      }
      throw new Error('Failed to fetch users');
    }

    const users = await response.json();
    const total = users.length;
    const frozen = users.filter(u => u.frozen).length;
    const active = users.filter(u => u.active && !u.frozen).length;

    document.getElementById('totalUsers').textContent = total;
    document.getElementById('activeUsers').textContent = active;
    document.getElementById('frozenUsers').textContent = frozen;

    const tbody = document.getElementById('usersBody');
    tbody.innerHTML = users.map(user => `
          <tr>
            <td>${user.name}</td>
            <td>${user.email}</td>
            <td>${user.phoneNumber || '--'}</td>
            <td><span style="background:#e0e7ff; padding:2px 10px; border-radius:20px;">${user.role}</span></td>
            <td>${user.frozen ? '<span style="color:#f97316;">❄️ Frozen</span>' : '<span style="color:#10b981;">✓ Active</span>'}</td>
            <td>
              <button class="${user.frozen ? 'unfreeze-btn' : 'freeze-btn'}" onclick="toggleFreeze('${user.id}', ${!user.frozen})">
                ${user.frozen ? 'Unfreeze' : 'Freeze'}
              </button>
            </td>
          </tr>
        `).join('');
  } catch (error) {
    console.error('Error:', error);
    document.getElementById('usersBody').innerHTML = '<tr><td colspan="6">Error loading users</td></tr>';
  }
}

async function toggleFreeze(userId, shouldFreeze) {
  try {
    const response = await fetch(`${API_BASE_URL}/auth/internal/users/${userId}/freeze?freeze=${shouldFreeze}`, {
      method: 'PUT',
      headers: { 'Authorization': `Bearer ${token}` }
    });

    if (response.ok) {
      await fetchAllUsers();
    } else {
      throw new Error('Unable to update status');
    }
  } catch (error) {
    alert('Error: ' + error.message);
  }
}

function logout() {
  localStorage.clear();
  window.location.href = 'login.html';
}

fetchAllUsers();
