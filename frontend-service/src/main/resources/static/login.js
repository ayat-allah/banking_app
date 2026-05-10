const API_BASE_URL = 'http://localhost:8080/api';

async function login() {
  const email = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value;
  const msg = document.getElementById('msg');
  const btn = document.querySelector('button');

  if (!email || !password) {
    msg.style.color = 'red';
    msg.textContent = 'Please fill all fields';
    return;
  }

  btn.textContent = 'Logging in...';
  btn.classList.add('loading');
  msg.textContent = '';

  try {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || 'Login failed');
    }

    localStorage.setItem('token', data.token);
    localStorage.setItem('userId', data.userId);
    localStorage.setItem('userName', data.name);
    localStorage.setItem('userRole', data.role);
    localStorage.setItem('userEmail', data.email);

    msg.style.color = 'green';
    msg.textContent = '✓ Login successful! Redirecting...';

    setTimeout(() => {
      if (data.role === 'ADMIN') {
        window.location.href = 'admin_dashboard.html';
      } else {
        window.location.href = 'user_dashboard.html';
      }
    }, 800);
  } catch (error) {
    msg.style.color = 'red';
    msg.textContent = error.message || 'Connection error. Please try again.';
  } finally {
    btn.textContent = 'Login';
    btn.classList.remove('loading');
  }
}

(function redirectIfLoggedIn() {
  const token = localStorage.getItem('token');
  if (!token) return;

  const role = localStorage.getItem('userRole');
  if (role === 'ADMIN') {
    window.location.href = 'admin_dashboard.html';
  } else if (role === 'CUSTOMER') {
    window.location.href = 'user_dashboard.html';
  }
})();
