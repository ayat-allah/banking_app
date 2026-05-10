const API_BASE_URL = 'http://localhost:8080/api';
const token = localStorage.getItem('token');
const userId = localStorage.getItem('userId');
const userRole = localStorage.getItem('userRole');

if (!token || userRole !== 'CUSTOMER') {
  window.location.href = 'login.html';
}

const userName = localStorage.getItem('userName') || 'Customer';
const userEmail = localStorage.getItem('userEmail') || '';

document.getElementById('userName').textContent = userName;
document.getElementById('userEmail').textContent = userEmail;
document.getElementById('userAvatar').textContent = userName.charAt(0).toUpperCase();

document.getElementById('transferButton').addEventListener('click', () => {
  window.location.href = 'transfer.html';
});

async function fetchDashboardData() {
  try {
    const [userResponse, walletResponse, historyResponse] = await Promise.all([
      fetch(`${API_BASE_URL}/auth/users/${userId}`),
      fetch(`${API_BASE_URL}/wallet/balance`, {
        headers: { 'X-User-Id': userId }
      }),
      fetch(`${API_BASE_URL}/transactions/history`, {
        headers: { 'X-User-Id': userId }
      })
    ]);

    if (!userResponse.ok || !walletResponse.ok || !historyResponse.ok) {
      throw new Error('Failed to fetch dashboard data');
    }

    const userData = await userResponse.json();
    const walletData = await walletResponse.json();
    const historyData = await historyResponse.json();

    document.getElementById('totalBalance').textContent = `$ ${Number(walletData.balance || 0).toLocaleString()}`;
    document.getElementById('savings').textContent = `$ ${Number(walletData.balance || 0).toLocaleString()}`;
    document.getElementById('monthlySpend').textContent = `$ ${Math.max(0, Number(walletData.balance || 0) * 0.12).toFixed(2)}`;
    document.getElementById('accountStatus').textContent = userData.frozen ? '🔒 Frozen' : '✓ Active';
    document.getElementById('accountStatus').style.color = userData.frozen ? '#f97316' : '#10b981';
    document.getElementById('accountNumber').textContent = `ID: ${userId}`;

    const rows = historyData.length ? historyData.map(tx => `
        <tr>
          <td>${new Date(tx.createdAt || tx.timestamp || Date.now()).toLocaleDateString()}</td>
          <td>${tx.description || tx.type || 'Transaction'}</td>
          <td style="color:${tx.amount >= 0 ? 'green' : 'red'};">${tx.amount >= 0 ? '+' : '-'}$${Math.abs(tx.amount || 0).toLocaleString()}</td>
          <td>${tx.status || 'Completed'}</td>
        </tr>
      `).join('') : '<tr><td colspan="4" class="loading">No recent transactions found</td></tr>';

    document.getElementById('transactionsBody').innerHTML = rows;
  } catch (error) {
    console.error('Dashboard error:', error);
    document.getElementById('transactionsBody').innerHTML = '<tr><td colspan="4">Error loading data</td></tr>';
  }
}

function logout() {
  localStorage.clear();
  window.location.href = 'login.html';
}

fetchDashboardData();
