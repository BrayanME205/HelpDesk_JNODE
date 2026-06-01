document.addEventListener("DOMContentLoaded", () => {
    const session = requireClientSession();
    if (!session) return;

    renderHeader(
        "Ingresar solicitud",
        `Cliente: ${session.clientName || session.clientEmail}`
    );

    const serviceSelect = document.getElementById("serviceId");
    const form = document.getElementById("issueForm");
    const messageContainer = document.getElementById("messageContainer");

    loadClientServices(session.services, serviceSelect);

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearMessage();

        const payload = {
            clientId: session.clientId,
            serviceId: Number(serviceSelect.value),
            description: document.getElementById("description").value.trim(),
            contactPhone: document.getElementById("contactPhone").value.trim(),
            contactEmail: document.getElementById("contactEmail").value.trim(),
            referenceAddress: document.getElementById("referenceAddress").value.trim()
        };

        if (!payload.description) {
            showError("La descripción es obligatoria.");
            return;
        }

        if (!payload.serviceId) {
            showError("Debes seleccionar un servicio.");
            return;
        }

        try {
            const result = await apiRequest("/client/issues", "POST", payload);

            showSuccess(
                `Solicitud registrada correctamente. Número: ${result.requestNumber}`
            );

            form.reset();
            if (serviceSelect.options.length > 0) {
                serviceSelect.selectedIndex = 0;
            }
        } catch (error) {
            showError(error.message);
        }
    });

    function loadClientServices(services, selectElement) {
        selectElement.innerHTML = "";

        if (!services || services.length === 0) {
            const option = document.createElement("option");
            option.value = "";
            option.textContent = "No hay servicios disponibles";
            selectElement.appendChild(option);
            selectElement.disabled = true;
            showError("Tu sesión no trae servicios asociados. Debes volver a iniciar sesión.");
            return;
        }

        const defaultOption = document.createElement("option");
        defaultOption.value = "";
        defaultOption.textContent = "Seleccione un servicio";
        selectElement.appendChild(defaultOption);

        services.forEach(service => {
            const option = document.createElement("option");
            option.value = service.id;
            option.textContent = service.name;
            selectElement.appendChild(option);
        });
    }

    function showSuccess(message) {
        messageContainer.innerHTML = `<div class="alert alert-success">${message}</div>`;
    }

    function showError(message) {
        messageContainer.innerHTML = `<div class="alert alert-error">${message}</div>`;
    }

    function clearMessage() {
        messageContainer.innerHTML = "";
    }
});