document.addEventListener("DOMContentLoaded", async () => {
    const session = requireClientSession();
    if (!session) return;

    renderHeader(
        "Detalle de solicitud",
        `Cliente: ${session.clientName || session.clientEmail}`
    );

    const issueId = getIssueIdFromQueryString();
    const detailContainer = document.getElementById("issueDetail");
    const commentsList = document.getElementById("commentsList");
    const commentForm = document.getElementById("commentForm");
    const messageContainer = document.getElementById("messageContainer");

    if (!issueId) {
        detailContainer.innerHTML = `
            <div class="alert alert-error">No se encontró el identificador de la solicitud.</div>
        `;
        return;
    }

    await loadIssueDetail();

    commentForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearMessage();

        const description = document.getElementById("commentDescription").value.trim();

        if (!description) {
            showError("Debes escribir un comentario.");
            return;
        }

        const payload = {
            clientId: session.clientId,
            description: description
        };

        try {
            await apiRequest(`/client/issues/${issueId}/comments`, "POST", payload);
            document.getElementById("commentDescription").value = "";
            showSuccess("Comentario agregado correctamente.");
            await loadIssueDetail();
        } catch (error) {
            showError(error.message);
        }
    });

    async function loadIssueDetail() {
        try {
            const issue = await apiRequest(`/client/issues/${issueId}`);
            renderIssue(issue);
            renderComments(issue.comments || []);
        } catch (error) {
            detailContainer.innerHTML = `
                <div class="alert alert-error">${error.message}</div>
            `;
        }
    }

    function renderIssue(issue) {
        detailContainer.innerHTML = `
            <div class="detail-grid">
                <div class="detail-item">
                    <strong>Número de solicitud</strong>
                    <div>${issue.requestNumber}</div>
                </div>

                <div class="detail-item">
                    <strong>Estado</strong>
                    <div>
                        <span class="${getStatusBadgeClass(issue.status)}">${translateStatus(issue.status)}</span>
                    </div>
                </div>

                <div class="detail-item">
                    <strong>Servicio</strong>
                    <div>${issue.service}</div>
                </div>

                <div class="detail-item">
                    <strong>Fecha y hora de registro</strong>
                    <div>${formatDateTime(issue.registeredAt)}</div>
                </div>

                <div class="detail-item detail-item-full">
                    <strong>Descripción</strong>
                    <div>${issue.description}</div>
                </div>
            </div>
        `;
    }

    function renderComments(comments) {
        commentsList.innerHTML = "";

        if (!comments.length) {
            commentsList.innerHTML = `
                <li>No hay comentarios registrados para esta solicitud.</li>
            `;
            return;
        }

        comments.forEach(comment => {
            const item = document.createElement("li");
            item.innerHTML = `
                <div class="comment-meta">
                    ${formatDateTime(comment.commentTimestamp)} - ${comment.authorEmail}
                </div>
                <div>${comment.description}</div>
            `;
            commentsList.appendChild(item);
        });
    }

    function getIssueIdFromQueryString() {
        const params = new URLSearchParams(window.location.search);
        return params.get("id");
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