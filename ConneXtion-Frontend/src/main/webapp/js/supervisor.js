// js/supervisor.js
const API_URL = 'http://localhost:8081/api/issues';

async function loadPendingIssues() {
    try {
        const [issuesRes, supportersRes] = await Promise.all([
            fetch(`${API_URL}/pending`),
            fetch('http://localhost:8081/api/supporters', {credentials: 'include'})
        ]);

        const issues = await issuesRes.json();
        const supporters = await supportersRes.json();

        const tbody = document.querySelector('#issuesTable tbody');
        tbody.innerHTML = '';

        if (issues.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">No hay tiquetes pendientes</td></tr>';
            return;
        }

        const options = supporters.map(s =>
                `<option value="${s.id}">${s.name}</option>`
        ).join('');

        issues.forEach(issue => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#${issue.id}</td>
                <td>${issue.description || 'Sin descripción'}</td>
                <td><strong>${issue.classification || 'Calculando..'}</strong></td>
                <td>
                    <select id="supporter-${issue.id}">
                        <option value="">Seleccione soportista</option>
                        ${options}
                    </select>
                </td>
                <td>
                    <button onclick="assignIssue(${issue.id})">Asignar Tiquete</button>
                </td>
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

window.onload = loadPendingIssues;

