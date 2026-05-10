const API_BASE_URL = 'http://localhost:8080/api';

async function register() {
  const name = document.getElementById('name').value.trim();
  const email = document.getElementById('email').value.trim();
  const phone = document.getElementById('phone').value.trim();
  const password = document.getElementById('password').value;
  const msg = document.getElementById('msg');
  const btn = document.querySelector('button');

  if (!name || !email || !phone || !password) {
    msg.style.color = 'red';
    msg.textContent = 'Please fill all fields';
    return;
  }

  if (password.length < 8) {
    msg.style.color = 'red';
    msg.textContent = 'Password must be at least 8 characters';
    return;
  }

  btn.textContent = 'Registering...';
  btn.classList.add('loading');
  msg.textContent = '';

  try {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name,
        email,
        phoneNumber: phone,
        password,
        role: 'CUSTOMER'
      })
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || 'Registration failed');
    }

    localStorage.setItem('token', data.token);
    localStorage.setItem('userId', data.userId);
    localStorage.setItem('userName', data.name);
    localStorage.setItem('userRole', data.role);
    localStorage.setItem('userEmail', data.email);

    msg.style.color = 'green';
    msg.textContent = '✓ Registration successful! Redirecting to dashboard...';

    setTimeout(() => {
      window.location.href = 'user_dashboard.html';
    }, 1000);
  } catch (error) {
    msg.style.color = 'red';
    msg.textContent = error.message || 'Registration failed. Please try again.';
  } finally {
    btn.textContent = 'Register';
    btn.classList.remove('loading');
  }
}

(function redirectIfLoggedIn() {
  if (localStorage.getItem('token')) {
    const role = localStorage.getItem('userRole');
    if (role === 'ADMIN') {
      window.location.href = 'admin_dashboard.html';
    } else {
      window.location.href = 'user_dashboard.html';
    }
  }
})();
