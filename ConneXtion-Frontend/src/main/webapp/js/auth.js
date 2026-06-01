const API = 'http://localhost:8080/api';

// ── CU2 / CU8 — Login ────────────────────────────────────────────────────
async function loginUser() {
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;

    if (!email || !password) {
        showAlert('Correo y contraseña son obligatorios.', 'error');
        return;
    }

    try {
        const res = await fetch(`${API}/auth/login`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            credentials: 'include', // ← envía/recibe la cookie de sesión
            body: JSON.stringify({email, password})
        });

        if (!res.ok) {
            showAlert('Correo o contraseña incorrectos.', 'error');
            return;
        }

        const data = await res.json();

        // Guardamos solo lo necesario para la UI — la sesión real está en el servidor
        sessionStorage.setItem('userName', data.name);
        sessionStorage.setItem('role', data.role);
        sessionStorage.setItem('userId', data.id);

        window.location.href = 'dashboard.html';

    } catch (e) {
        showAlert('Error de conexión con el servidor.', 'error');
    }
}

// ── CU1 — Registro cliente ───────────────────────────────────────────────
async function registerClient() {
    const name = document.getElementById('name').value.trim();
    const firstSurname = document.getElementById('firstSurname').value.trim();
    const secondSurname = document.getElementById('secondSurname').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const address = document.getElementById('address').value.trim();
    const phone = document.getElementById('phone').value.trim();
    const secondContact = document.getElementById('secondContact').value.trim();
    const serviceIds = getCheckedServices();

    if (!name || !firstSurname || !secondSurname || !email || !password) {
        showAlert('Los campos con * son obligatorios.', 'error');
        return;
    }
    if (password.length < 6) {
        showAlert('La contraseña debe tener al menos 6 caracteres.', 'error');
        return;
    }
    if (serviceIds.length === 0) {
        showAlert('Debe seleccionar al menos un servicio.', 'error');
        return;
    }

    try {
        const res = await fetch(`${API}/auth/register`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            credentials: 'include',
            body: JSON.stringify({
                name, firstSurname, secondSurname,
                email, password, address, phone,
                secondContact, serviceIds
            })
        });

        const data = await res.json();
        if (res.ok) {
            showAlert('¡Cuenta creada exitosamente! Redirigiendo...', 'success');
            setTimeout(() => window.location.href = 'index.html', 2000);
        } else {
            showAlert(data.message || 'Error al registrar.', 'error');
        }
    } catch (e) {
        showAlert('Error de conexión con el servidor.', 'error');
    }
}

// ── CU7 — Registro soportista ────────────────────────────────────────────
async function registerSupporter() {
    const name = document.getElementById('name').value.trim();
    const firstSurname = document.getElementById('firstSurname').value.trim();
    const secondSurname = document.getElementById('secondSurname').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const isSupervisor = document.getElementById('isSupervisor').checked;
    const serviceIds = getCheckedServices();

    if (!name || !firstSurname || !secondSurname || !email || !password) {
        showAlert('Los campos con * son obligatorios.', 'error');
        return;
    }
    if (serviceIds.length === 0) {
        showAlert('Debe asignar al menos un servicio.', 'error');
        return;
    }

    try {
        const res = await fetch(`${API}/auth/register/support`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            credentials: 'include',
            body: JSON.stringify({
                name, firstSurname, secondSurname,
                email, password, isSupervisor, serviceIds
            })
        });

        const data = await res.json();
        if (res.ok) {
            showAlert('Usuario registrado exitosamente.', 'success');
            setTimeout(() => window.location.href = 'dashboard.html', 2000);
        } else {
            showAlert(data.message || 'Error al registrar.', 'error');
        }
    } catch (e) {
        showAlert('Error de conexión con el servidor.', 'error');
    }
}

// ── CU3 / CU9 — Logout ───────────────────────────────────────────────────
async function logout() {
    try {
        await fetch(`${API}/auth/logout`, {
            method: 'POST',
            credentials: 'include'   // ← envía la cookie para que el servidor invalide la sesión
        });
    } finally {
        const role = sessionStorage.getItem('role');
        sessionStorage.clear();
        window.location.href =
                (role === 'SUPPORTER' || role === 'SUPERVISOR')
                ? 'support-login.html'
                : 'index.html';
    }
}

// ── Verificar sesión al cargar páginas protegidas ─────────────────────────
async function checkSession() {
    try {
        const res = await fetch(`${API}/auth/session`, {
            credentials: 'include'
        });

        if (!res.ok) {
            window.location.href = 'index.html';
            return null;
        }

        const data = await res.json();
        // Sincronizar sessionStorage con la sesión del servidor
        sessionStorage.setItem('userName', data.userName);
        sessionStorage.setItem('role', data.role);
        sessionStorage.setItem('userId', data.userId);
        return data;

    } catch (e) {
        window.location.href = 'index.html';
        return null;
    }
}

// ── Cargar servicios ──────────────────────────────────────────────────────
async function loadServices() {
    try {
        const res = await fetch(`${API}/auth/services`, {
            credentials: 'include'
        });
        const services = await res.json();
        const grid = document.getElementById('servicesGrid');
        grid.innerHTML = '';
        services.forEach(s => {
            grid.innerHTML += `
                <label class="service-option">
                    <input type="checkbox" value="${s.serviceId}"> ${s.name}
                </label>`;
        });
    } catch (e) {
        document.getElementById('servicesGrid').innerHTML =
                '<p style="color:red">Error cargando servicios.</p>';
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────
function getCheckedServices() {
    return Array.from(
            document.querySelectorAll('#servicesGrid input:checked')
            ).map(cb => parseInt(cb.value));
}

function showAlert(message, type) {
    const el = document.getElementById('alertMsg');
    if (!el)
        return;
    el.textContent = message;
    el.className = `alert alert-${type}`;
    el.style.display = 'block';
}