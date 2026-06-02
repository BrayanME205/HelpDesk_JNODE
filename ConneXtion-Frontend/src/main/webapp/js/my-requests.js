document.addEventListener("DOMContentLoaded", loadMyRequests);

async function loadMyRequests() {
    const session = await checkSession();
    if (!session) return;

    const role = sessionStorage.getItem("role");
    const clientId = sessionStorage.getItem("clientId");

    if (role !== "CLIENT" || !clientId) {
        window.location.href = "index.html";
        return;
    }

    try {
        const res = await fetch(`${API}/client/issues/client/${clientId}`, {
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
        container.innerHTML = "<p>No tienes solicitudes registradas todavía.</p>";
        return;
    }

    let html = `
        <table style="width:100%; border-collapse:collapse; background:white;">
            <thead>
                <tr style="background:#0056b3; color:white;">
                    <th style="padding:10px;">Número</th>
                    <th style="padding:10px;">Servicio</th>
                    <th style="padding:10px;">Fecha</th>
                    <th style="padding:10px;">Estado</th>
                    <th style="padding:10px;">Detalle</th>
                </tr>
            </thead>
            <tbody>
    `;

    requests.forEach(issue => {
        html += `
            <tr>
                <td style="padding:10px; border-bottom:1px solid #ddd;">${issue.requestNumber}</td>
                <td style="padding:10px; border-bottom:1px solid #ddd;">${issue.service}</td>
                <td style="padding:10px; border-bottom:1px solid #ddd;">${formatDateTime(issue.registeredAt)}</td>
                <td style="padding:10px; border-bottom:1px solid #ddd;">${translateStatus(issue.status)}</td>
                <td style="padding:10px; border-bottom:1px solid #ddd;">
                    <button class="btn" onclick="goToDetail(${issue.issueId})">Ver detalle</button>
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

function goToDetail(issueId) {
    window.location.href = `request-detail.html?id=${issueId}`;
}

function formatDateTime(dateString) {
    if (!dateString) return "";
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

    return { message: await res.text() };
}