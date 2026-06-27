const API = 'http://localhost:8081/api';

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
            credentials: 'include',
            body: JSON.stringify({email, password})
        });

        if (!res.ok) {
            showAlert('Correo o contraseña incorrectos.', 'error');
            return;
        }

        const data = await res.json();
        console.log("LOGIN DATA:", data);

        sessionStorage.setItem('userName', data.name);
        sessionStorage.setItem('role', data.role);
        sessionStorage.setItem('userId', data.id);
        sessionStorage.setItem('email', data.email || email);

        if (data.role === 'CLIENT') {
            sessionStorage.setItem('clientId', data.clientId || data.id);

            if (data.services) {
                sessionStorage.setItem('clientServices', JSON.stringify(data.services));
            }
        }


        window.location.href = 'dashboard.html';

    } catch (e) {
        showAlert('Error de conexión con el servidor.', 'error');
    }
}

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

async function logout() {
    try {
        await fetch(`${API}/auth/logout`, {
            method: 'POST',
            credentials: 'include'
        });
    } finally {
        sessionStorage.clear();
        window.location.href = 'index.html';
    }
}

async function checkSession() {
    const role = sessionStorage.getItem('role');
    const userId = sessionStorage.getItem('userId');

    if (!role || !userId) {
        window.location.href = 'index.html';
        return null;
    }

    try {
        const res = await fetch(`${API}/auth/session`, {
            credentials: 'include'
        });

        if (!res.ok) {
            const userName = sessionStorage.getItem('userName');
            const email = sessionStorage.getItem('email');
            const clientId = sessionStorage.getItem('clientId');
            return {role, userId, userName, email, clientId};
        }

        const data = await res.json();
        console.log("SESSION DATA:", data);

        sessionStorage.setItem('userName', data.userName || data.name || '');
        sessionStorage.setItem('role', data.role || '');
        sessionStorage.setItem('userId', data.userId || data.id || '');
        sessionStorage.setItem('email', data.email || '');

        if (data.role === 'CLIENT') {
            sessionStorage.setItem('clientId', data.clientId || data.userId || '');
            if (data.services) {
                sessionStorage.setItem('clientServices', JSON.stringify(data.services));
            }
        }

        return data;

    } catch (e) {
        const userName = sessionStorage.getItem('userName');
        const email = sessionStorage.getItem('email');
        const clientId = sessionStorage.getItem('clientId');
        return {role, userId, userName, email, clientId};
    }
}

async function loadServices() {
    const grid = document.getElementById('servicesGrid');
    if (!grid)
        return;

    try {
        const res = await fetch(`${API}/auth/services`, {
            credentials: 'include'
        });

        const data = await res.json();
        console.log("SERVICES DATA:", data);

        if (!res.ok || !Array.isArray(data)) {
            grid.innerHTML = '<p style="color:red">No se pudieron cargar los servicios.</p>';
            return;
        }

        grid.innerHTML = '';

        data.forEach(s => {
            grid.innerHTML += `
                <label class="service-option">
                    <input type="checkbox" value="${s.serviceId || s.id}"> ${s.name}
                </label>`;
        });

        if (data.length === 0) {
            grid.innerHTML = '<p style="color:red">No hay servicios disponibles.</p>';
        }

    } catch (e) {
        console.error("Error en loadServices:", e);
        grid.innerHTML = '<p style="color:red">Error cargando servicios.</p>';
    }
}

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
