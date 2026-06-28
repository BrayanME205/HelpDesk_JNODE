const API_BASE_URL = "https://helpdesk-jnode.onrender.com/api";

async function apiRequest(endpoint, method = "GET", body = null) {
    const config = {
        method,
        headers: {"Content-Type": "application/json"},
        credentials: "include"
    };
    if (body) {
        config.body = JSON.stringify(body);
    }
    const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
    let data;
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
        data = await response.json();
    } else {
        data = await response.text();
    }
    if (!response.ok) {
        const message = typeof data === "string" ? data : data.message || "Error en la solicitud";
        throw new Error(message);
    }
    return data;
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
