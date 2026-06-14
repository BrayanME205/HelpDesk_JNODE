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
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">No hay tiquetes pendientes</td></tr>';
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
                    <select id="supporter-${id}">
                        <option value="">Seleccione soportista</option>
                        ${options}
                    </select>
                </td>
                <td><button onclick="assignIssue(${id})">Asignar Tiquete</button></td>
            ` : `<td colspan="2" style="color: gray; font-style: italic;">Ya asignado</td>`;

            const startCell = issue.status === 'ASIGNADO' ? `
                <td><button onclick="startIssue(${id})">Iniciar Proceso</button></td>
            ` : issue.status === 'EN_PROGRESO' ? `
                <td style="color: #e67e22; font-style: italic;">En progreso</td>
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