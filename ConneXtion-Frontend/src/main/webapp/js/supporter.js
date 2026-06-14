const API = 'http://localhost:8081/api';

async function loadMyIssues() {
    const supporterId = sessionStorage.getItem('userId');

    if (!supporterId) {
        alert('Sesión no válida. Inicia sesión nuevamente.');
        window.location.href = 'support-login.html';
        return;
    }

    try {
        const response = await fetch(`${API}/issues/supporter/${supporterId}`);
        const issues = await response.json();
        const tbody = document.querySelector('#supporterTable tbody');
        tbody.innerHTML = '';

        if (issues.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">No tienes solicitudes asignadas</td></tr>';
            return;
        }

        issues.forEach(issue => {
            const tr = document.createElement('tr');
            const id = issue.issueId;

            const actionCell = issue.status === 'ASIGNADO' ? `
                <td><button onclick="startIssue(${id})">Iniciar Proceso</button></td>
            ` : issue.status === 'EN_PROGRESO' ? `
                <td><button onclick="goToResolve(${id})">Resolver</button></td>
            ` : `
                <td style="color: gray; font-style: italic;">Resuelto</td>
            `;

            tr.innerHTML = `
                <td>${id}</td>
                <td>${issue.requestNumber}</td>
                <td>${issue.description || 'Sin descripción'}</td>
                <td><strong>${issue.classification}</strong></td>
                <td>${issue.status}</td>
                ${actionCell}
            `;
            tbody.appendChild(tr);
        });

    } catch (error) {
        console.error('Error:', error);
        alert('Error conectando con el servidor.');
    }
}

async function startIssue(issueId) {
    try {
        const response = await fetch(`${API}/issues/${issueId}/start`, {
            method: 'POST'
        });
        if (response.ok) {
            alert('Solicitud iniciada correctamente.');
            loadMyIssues();
        } else {
            alert('Error al iniciar el proceso.');
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

function goToResolve(issueId) {
    window.location.href = `resolution.html?issueId=${issueId}`;
}

window.onload = loadMyIssues;