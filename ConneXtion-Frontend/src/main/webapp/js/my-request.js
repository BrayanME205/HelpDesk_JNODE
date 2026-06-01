document.addEventListener("DOMContentLoaded", async () => {
    const session = requireClientSession();
    if (!session) return;

    renderHeader(
        "Mis solicitudes",
        `Cliente: ${session.clientName || session.clientEmail}`
    );

    const tableBody = document.getElementById("issuesTableBody");
    const emptyState = document.getElementById("emptyState");

    try {
        const issues = await apiRequest(`/client/issues/client/${session.clientId}`);

        if (!issues || issues.length === 0) {
            emptyState.innerHTML = `
                <div class="card empty-state">
                    Aún no has registrado solicitudes.
                </div>
            `;
            return;
        }

        tableBody.innerHTML = "";

        issues.forEach(issue => {
            const row = document.createElement("tr");

            row.innerHTML = `
                <td>${issue.requestNumber}</td>
                <td>${issue.service}</td>
                <td>${formatDateTime(issue.registeredAt)}</td>
                <td><span class="${getStatusBadgeClass(issue.status)}">${translateStatus(issue.status)}</span></td>
                <td>
                    <button class="btnDetail" data-id="${issue.issueId}">
                        Ver detalle
                    </button>
                </td>
            `;

            tableBody.appendChild(row);
        });

        bindDetailButtons();

    } catch (error) {
        emptyState.innerHTML = `
            <div class="card">
                <div class="alert alert-error">${error.message}</div>
            </div>
        `;
    }

    function bindDetailButtons() {
        const buttons = document.querySelectorAll(".btnDetail");

        buttons.forEach(button => {
            button.addEventListener("click", () => {
                const issueId = button.dataset.id;
                window.location.href = `request-detail.html?id=${issueId}`;
            });
        });
    }
});