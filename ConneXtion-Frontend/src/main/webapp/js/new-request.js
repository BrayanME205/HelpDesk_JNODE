document.addEventListener("DOMContentLoaded", initNewRequestPage);

async function initNewRequestPage() {
    const session = await checkSession();
    if (!session) return;

    const role = sessionStorage.getItem("role");
    const clientId = sessionStorage.getItem("clientId");

    if (role !== "CLIENT" || !clientId) {
        window.location.href = "index.html";
        return;
    }

    const email = sessionStorage.getItem("email");
    if (email) {
        document.getElementById("contactEmail").value = email;
    }

    await loadClientServices();
}

async function loadClientServices() {
    const select = document.getElementById("serviceId");
    const servicesJson = sessionStorage.getItem("clientServices");

    if (servicesJson) {
        try {
            const services = JSON.parse(servicesJson);

            select.innerHTML = `<option value="">Seleccione un servicio</option>`;

            services.forEach(service => {
                const option = document.createElement("option");
                option.value = service.serviceId || service.id;
                option.textContent = service.name;
                select.appendChild(option);
            });

            return;
        } catch (error) {
            console.error("Error parseando clientServices:", error);
        }
    }

    try {
        const res = await fetch(`${API}/auth/services`, {
            credentials: 'include'
        });

        const services = await parseResponse(res);

        if (!res.ok || !Array.isArray(services)) {
            showAlert("No se pudieron cargar los servicios.", "error");
            return;
        }

        sessionStorage.setItem("clientServices", JSON.stringify(services));

        select.innerHTML = `<option value="">Seleccione un servicio</option>`;

        services.forEach(service => {
            const option = document.createElement("option");
            option.value = service.serviceId || service.id;
            option.textContent = service.name;
            select.appendChild(option);
        });

        showAlert("No se encontraron servicios del cliente en sesión. Se cargaron los servicios generales temporalmente.", "success");

    } catch (error) {
        console.error("Error cargando servicios:", error);
        showAlert("Error de conexión con el servidor al cargar servicios.", "error");
    }
}

async function createIssue() {
    const clientId = Number(sessionStorage.getItem("clientId"));

    const payload = {
        clientId: clientId,
        serviceId: Number(document.getElementById("serviceId").value),
        description: document.getElementById("description").value.trim(),
        contactPhone: document.getElementById("contactPhone").value.trim(),
        contactEmail: document.getElementById("contactEmail").value.trim(),
        referenceAddress: document.getElementById("referenceAddress").value.trim()
    };

    if (!payload.description) {
        showAlert("La descripción es obligatoria.", "error");
        return;
    }

    if (!payload.serviceId) {
        showAlert("Debes seleccionar un servicio.", "error");
        return;
    }

    try {
        const res = await fetch(`${API}/client/issues`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(payload)
        });

        const data = await parseResponse(res);

        if (!res.ok) {
            showAlert(data.message || "No se pudo registrar la solicitud.", "error");
            return;
        }

        showAlert(`Solicitud registrada correctamente. Número: ${data.requestNumber}`, "success");

        setTimeout(() => {
            window.location.href = "my-requests.html";
        }, 1200);

    } catch (error) {
        console.error("Error en createIssue:", error);
        showAlert("Error de conexión con el servidor.", "error");
    }
}

async function parseResponse(res) {
    const contentType = res.headers.get("content-type");

    if (contentType && contentType.includes("application/json")) {
        return await res.json();
    }

    return { message: await res.text() };
}
