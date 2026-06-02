// js/supervisor.js
const API_URL = 'http://localhost:8081/api/issues';

// Cargar tiquetes "Ingresados"
async function loadPendingIssues() {
    try {
        const response = await fetch(`${API_URL}/pending`);
        const issues = await response.json();

        const tbody = document.querySelector('#issuesTable tbody');
        tbody.innerHTML = '';

        if (issues.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">No hay tiquetes pendientes</td></tr>';
            return;
        }

        issues.forEach(issue => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#${issue.reportNumber}</td>
                <td>${issue.description || 'Sin descripción'}</td>
                <td><strong>${issue.classification || 'Calculando...'}</strong></td>
                <td>
                    <select id="supporter-${issue.reportNumber}">
                        <option value="1">Mauricio</option>
                        <option value="2">Ericka</option>
                        <option value="3">Darío</option>
                    </select>
                </td>
                <td>
                    <button onclick="assignIssue(${issue.reportNumber})">Asignar Tiquete</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (error) {
        console.error('Error conectando con el backend:', error);
    }
}

// Enviar la orden al Backend
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

// Iniciar al cargar la página
window.onload = loadPendingIssues;

