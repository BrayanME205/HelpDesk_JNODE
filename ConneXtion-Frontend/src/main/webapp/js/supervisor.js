const API_URL = 'http://localhost:8081/api/issues';

async function loadPendingIssues() {
    try {
        const [issuesRes, supportersRes] = await Promise.all([
            fetch(`${API_URL}/active`),
            fetch('http://localhost:8081/api/supporters', {credentials: 'include'})
        ]);
        const issues = await issuesRes.json();
        const supporters = await supportersRes.json();
        const tbody = document.querySelector('#issuesTable tbody');
        tbody.innerHTML = '';

        if (issues.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" style="padding:0;">
                <div class="empty-state">
                    <div class="empty-icon">🗂️</div>
                    <strong>No hay tiquetes pendientes</strong>
                    <span>Las nuevas solicitudes aparecerán aquí.</span>
                </div>
            </td></tr>`;
            return;
        }

        const options = supporters.map(s =>
                `<option value="${s.id}">${s.name}</option>`
        ).join('');

        issues.forEach(issue => {
            const tr = document.createElement('tr');
            const id = issue.issueId;

            const assignCell = issue.status === 'INGRESADO' ? `
                <td>
                    <select id="supporter-${id}" style="padding: 6px; border-radius: 4px; border: 1px solid #ccc; font-size: 13px;">
                        <option value="">Seleccione soportista</option>
                        ${options}
                    </select>
                </td>
                <td>
                    <button onclick="assignIssue(${id})" 
                            style="background-color: #007bff; color: white; padding: 6px 12px; border: none; border-radius: 4px; font-weight: bold; cursor: pointer; font-size: 13px; display: inline-flex; align-items: center; gap: 5px;">
                        <i class="fas fa-user-plus"></i> Asignar Tiquete
                    </button>
                </td>
            ` : `
                <td colspan="2" style="color: gray; font-style: italic; font-size: 13px;">
                    <i class="fas fa-user-check"></i> Ya asignado
                </td>
            `;

            const startCell = issue.status === 'ASIGNADO' ? `
                <td>
                    <button onclick="startIssue(${id})"
                            style="background-color: #e67e22; color: white; padding: 6px 12px; border: none; border-radius: 4px; font-weight: bold; cursor: pointer; font-size: 13px; display: inline-flex; align-items: center; gap: 5px;">
                        <i class="fas fa-play"></i> Iniciar Proceso
                    </button>
                </td>
            ` : issue.status === 'EN_PROGRESO' ? `
                <td>
                    <div style="display: flex; align-items: center; justify-content: space-between; gap: 10px; width: 100%; max-width: 200px;">
                        <span style="color: #e67e22; font-weight: bold; font-style: italic; font-size: 13px;">En progreso</span>
                        <button onclick="window.location.href='chat_module.html?issueId=${id}'"
                                style="background-color: #17a2b8; color: white; border: none; border-radius: 4px; padding: 6px 12px; font-size: 12px; font-weight: bold; cursor: pointer; display: inline-flex; align-items: center; gap: 5px;">
                            <i class="fas fa-eye"></i> Auditar
                        </button>
                    </div>
                </td>
            ` : `<td></td>`;

            tr.innerHTML = `
                <td>${issue.issueId}</td>
                <td>${issue.requestNumber}</td>
                <td>${issue.description || 'Sin descripción'}</td>
                <td><strong>${issue.classification || 'Calculando...'}</strong></td>
                ${assignCell}
                ${startCell}
            `;
            tbody.appendChild(tr);
        });

    } catch (error) {
        console.error('Error conectando con el backend:', error);
    }
}

async function assignIssue(issueId) {
    const selectElement = document.getElementById(`supporter-${issueId}`);
    const supporterId = selectElement.value;
    if (!supporterId) {
        alert('Por favor selecciona un soportista.');
        return;
    }
    try {
        const response = await fetch(`${API_URL}/${issueId}/assign/${supporterId}`, {
            method: 'POST'
        });
        if (response.ok) {
            const message = await response.text();
            alert(message);
            loadPendingIssues();
        } else {
            alert('Error al asignar el tiquete en la base de datos.');
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

async function startIssue(issueId) {
    try {
        const response = await fetch(`${API_URL}/${issueId}/start`, {
            method: 'POST'
        });
        if (response.ok) {
            alert('Solicitud iniciada correctamente.');
            loadPendingIssues();
        } else {
            alert('Error: El tiquete debe estar Ingresado o Asignado.');
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

window.onload = loadPendingIssues;

setInterval(async () => {
    const role = sessionStorage.getItem("role");
    const userId = sessionStorage.getItem("userId");
    if (!userId)
        return;

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
