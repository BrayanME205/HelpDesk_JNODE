document.addEventListener("DOMContentLoaded", loadMyRequests);
async function loadMyRequests() {
    const session = await checkSession();
    if (!session)
        return;
    const role = sessionStorage.getItem("role");
    const clientId = sessionStorage.getItem("clientId");
    if (role !== "CLIENT" || !clientId) {
        window.location.href = "index.html";
        return;
    }
    try {
        const res = await fetch(`${API}/issues/client/${clientId}`, {
            credentials: "include"
        });
        const data = await parseResponse(res);
        if (!res.ok) {
            showAlert(data.message || "No se pudieron cargar las solicitudes.", "error");
            return;
        }
        renderRequests(data);
    } catch (error) {
        showAlert("Error de conexión con el servidor.", "error");
    }
}
function renderRequests(requests) {
    const container = document.getElementById("requestsContainer");
    if (!requests || requests.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">📋</div>
                <strong>No tienes solicitudes registradas todavía</strong>
                <span>Cuando registres una solicitud aparecerá aquí.</span>
            </div>
        `;
        return;
    }
    let html = `
        <table>
            <thead>
                <tr>
                    <th>Número</th>
                    <th>Servicio</th>
                    <th>Fecha</th>
                    <th>Estado</th>
                    <th style="text-align: center;">Acciones</th>
                </tr>
            </thead>
            <tbody>
    `;
    requests.forEach(issue => {
        html += `
            <tr>
                <td data-label="Número">${issue.requestNumber}</td>
                <td data-label="Servicio">${issue.service}</td>
                <td data-label="Fecha">${formatDateTime(issue.registeredAt)}</td>
                <td data-label="Estado">${getStatusBadge(issue.status)}</td>
                <td data-label="Acciones" style="text-align: center; vertical-align: middle;">
                    <div class="actions-cell-container">
                        <button class="btn" onclick="goToDetail(${issue.issueId})">
                            Ver detalle
                        </button>
                        <!-- Aquí podés meter el segundo botón directamente y se acomodará solo -->
                    </div>
                </td>
            </tr>
        `;
    });
    html += `
            </tbody>
        </table>
    `;
    container.innerHTML = html;
}
function getStatusBadge(status) {
    const classes = {
        INGRESADO: "badge-ingresado",
        ASIGNADO: "badge-asignado",
        EN_PROGRESO: "badge-progreso",
        RESUELTO: "badge-resueltos"
    };
    const cls = classes[status] || "badge-default";
    return `<span class="badge-status ${cls}">${translateStatus(status)}</span>`;
}
function goToDetail(issueId) {
    window.location.href = `request-detail.html?id=${issueId}`;
}
function formatDateTime(dateString) {
    if (!dateString)
        return "";
    return new Date(dateString).toLocaleString("es-CR");
}
function translateStatus(status) {
    const labels = {
        INGRESADO: "Ingresado",
        ASIGNADO: "Asignado",
        EN_PROGRESO: "En progreso",
        RESUELTO: "Resuelto"
    };
    return labels[status] || status;
}
async function parseResponse(res) {
    const contentType = res.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
        return await res.json();
    }
    return {message: await res.text()};
}
