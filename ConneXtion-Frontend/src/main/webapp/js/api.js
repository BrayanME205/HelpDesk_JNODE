const API_BASE_URL = "http://localhost:8080/api";

async function apiRequest(endpoint, method = "GET", body = null) {
    const config = {
        method: method,
        headers: {
            "Content-Type": "application/json"
        }
    };

    if (body) {
        config.body = JSON.stringify(body);
    }

    const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

    let responseBody = null;
    const contentType = response.headers.get("content-type");

    if (contentType && contentType.includes("application/json")) {
        responseBody = await response.json();
    } else {
        responseBody = await response.text();
    }

    if (!response.ok) {
        const message = typeof responseBody === "string"
            ? responseBody
            : responseBody.message || "Ocurrió un error al consumir la API";
        throw new Error(message);
    }

    return responseBody;
}

function formatDateTime(dateString) {
    if (!dateString) return "";
    return new Date(dateString).toLocaleString("es-CR");
}

function translateStatus(status) {
    const map = {
        INGRESADO: "Ingresado",
        ASIGNADO: "Asignado",
        EN_PROGRESO: "En Progreso",
        RESUELTO: "Resuelto"
    };
    return map[status] || status;
}

function getStatusBadgeClass(status) {
    switch (status) {
        case "INGRESADO":
            return "badge badge-ingresado";
        case "ASIGNADO":
            return "badge badge-asignado";
        case "EN_PROGRESO":
            return "badge badge-enprogreso";
        case "RESUELTO":
            return "badge badge-resuelto";
        default:
            return "badge";
    }
}