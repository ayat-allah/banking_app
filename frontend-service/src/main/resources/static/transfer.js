const API_BASE_URL = 'http://localhost:8080/api';
const token = localStorage.getItem('token');
const userId = localStorage.getItem('userId');
const userRole = localStorage.getItem('userRole');

if (!token || userRole !== 'CUSTOMER') {
  window.location.href = 'login.html';
}

function goToDashboard() {
  window.location.href = 'user_dashboard.html';
}

function logout() {
  localStorage.clear();
  window.location.href = 'login.html';
}

async function transferMoney() {
  const receiver = document.getElementById('username').value.trim();
  const amount = parseFloat(document.getElementById('amount').value);
  const msg = document.getElementById('msg');
  const btn = document.querySelector('button.transfer-btn');

  if (!receiver || isNaN(amount) || amount <= 0) {
    msg.style.color = 'red';
    msg.textContent = 'Please enter a valid recipient and amount.';
    return;
  }

  btn.textContent = 'Sending...';
  btn.disabled = true;
  msg.textContent = '';

  try {
    const response = await fetch(`${API_BASE_URL}/transactions/transfer`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-User-Id': userId
      },
      body: JSON.stringify({
        receiverIdentifier: receiver,
        amount,
        description: 'Customer transfer'
      })
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || 'Transfer failed');
    }

    msg.style.color = 'green';
    msg.textContent = '✅ Transfer successful! Redirecting to dashboard...';

    setTimeout(() => {
      window.location.href = 'user_dashboard.html';
    }, 1200);
  } catch (error) {
    msg.style.color = 'red';
    msg.textContent = error.message || 'Transfer failed. Please try again.';
  } finally {
    btn.textContent = 'Send';
    btn.disabled = false;
  }
}
