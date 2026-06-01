function getClientSession() {
    const clientId = sessionStorage.getItem("clientId");
    const clientName = sessionStorage.getItem("clientName");
    const clientEmail = sessionStorage.getItem("clientEmail");
    const rawServices = sessionStorage.getItem("clientServices");

    let services = [];
    if (rawServices) {
        try {
            services = JSON.parse(rawServices);
        } catch (error) {
            console.error("No se pudieron parsear los servicios del cliente", error);
        }
    }

    return {
        clientId: clientId ? Number(clientId) : null,
        clientName: clientName || "",
        clientEmail: clientEmail || "",
        services: services
    };
}

function requireClientSession() {
    const session = getClientSession();

    if (!session.clientId) {
        alert("Tu sesión no está disponible. Debes iniciar sesión nuevamente.");
        window.location.href = "../index.html";
        return null;
    }

    return session;
}

function renderHeader(title, subtitle = "") {
    const session = getClientSession();
    const headerContainer = document.getElementById("pageHeader");

    if (!headerContainer) return;

    headerContainer.innerHTML = `
        <div class="header">
            <h1>${title}</h1>
            <p>${subtitle}</p>
            <div class="top-menu">
                <a class="btn" href="new-request.html">Nueva solicitud</a>
                <a class="btn" href="my-requests.html">Mis solicitudes</a>
                <button class="btn btn-secondary" id="logoutButton">Cerrar sesión</button>
            </div>
        </div>
    `;

    const logoutButton = document.getElementById("logoutButton");
    if (logoutButton) {
        logoutButton.addEventListener("click", logoutClient);
    }
}

function logoutClient() {
    sessionStorage.removeItem("clientId");
    sessionStorage.removeItem("clientName");
    sessionStorage.removeItem("clientEmail");
    sessionStorage.removeItem("clientServices");
    window.location.href = "../index.html";
}
