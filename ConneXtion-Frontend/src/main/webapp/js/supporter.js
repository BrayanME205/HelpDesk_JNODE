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
            
            // Renderizado de celdas de acción estilizadas sin emojis y simétricas
            const actionCell = issue.status === 'ASIGNADO' ? `
                <td>
                    <button onclick="startIssue(${id})" 
                            style="background-color: #e67e22; color: white; padding: 8px 12px; border: none; border-radius: 4px; font-weight: bold; cursor: pointer; font-size: 13px; display: inline-flex; align-items: center; gap: 5px;">
                        <i class="fas fa-play"></i> Iniciar Proceso
                    </button>
                </td>
            ` : issue.status === 'EN_PROGRESO' ? `
                <td>
                    <div style="display: flex; gap: 6px; align-items: center; max-width: 220px;">
                        <button onclick="goToResolve(${id})" 
                                style="flex: 1; background-color: #007bff; color: white; padding: 8px 12px; border: none; border-radius: 4px; font-weight: bold; cursor: pointer; font-size: 13px; display: inline-flex; align-items: center; justify-content: center; gap: 5px;">
                            <i class="fas fa-check-circle"></i> Resolver
                        </button>
                        <button onclick="window.location.href='chat_module.html?issueId=${id}'"
                                style="flex: 1; background-color: #28a745; color: white; padding: 8px 12px; border: none; border-radius: 4px; font-weight: bold; cursor: pointer; font-size: 13px; display: inline-flex; align-items: center; justify-content: center; gap: 5px;">
                            <i class="fas fa-comments"></i> Chat
                        </button>
                    </div>
                </td>
            ` : `
                <td style="color: gray; font-style: italic; font-size: 13px;">
                    <i class="fas fa-archive"></i> Resuelto
                </td>
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

// Polling de notificaciones con Toast integrado
setInterval(async () => {
    const role = sessionStorage.getItem("role");
    const userId = sessionStorage.getItem("userId");
    if (!userId) return;

    try {
        const res = await fetch(`http://localhost:8081/api/issues/unread-alerts/${userId}?role=${role}`);
        if (res.ok) {
            const alertas = await res.json();
            alertas.forEach(alerta => {
                showPushToast(`Mensaje nuevo en tiquete #${alerta.requestNumber}`);
            });
        }
    } catch (e) {
        console.error("Error en polling de notificaciones", e);
    }
}, 10000);

function showPushToast(text) {
    const toast = document.createElement("div");
    toast.style.position = "fixed";
    toast.style.bottom = "20px";
    toast.style.right = "20px";
    toast.style.background = "#0056b3";
    toast.style.color = "white";
    toast.style.padding = "15px";
    toast.style.borderRadius = "8px";
    toast.style.boxShadow = "0 4px 12px rgba(0,0,0,0.2)";
    toast.style.zIndex = "1000";
    toast.style.fontFamily = "sans-serif";
    toast.style.fontSize = "14px";
    toast.innerText = text;

    document.body.appendChild(toast);
    setTimeout(() => {
        toast.remove();
    }, 4000);
}