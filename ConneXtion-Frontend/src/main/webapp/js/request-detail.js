document.addEventListener("DOMContentLoaded", async () => {
    const session = await checkSession(); 
    if (!session) return;

   /* renderHeader(
        "Detalle de solicitud",
        `Cliente: ${sessionStorage.getItem("userName") || sessionStorage.getItem("email")}`
    );*/

    const issueId = getIssueIdFromQueryString();
    const detailContainer = document.getElementById("issueDetail");
    const commentsList = document.getElementById("commentsList");
    const commentForm = document.getElementById("commentForm");
    const alertMsg = document.getElementById("alertMsg");
    if (!issueId) {
        detailContainer.innerHTML = `
            <div class="alert alert-error">No se encontró el identificador de la solicitud.</div>
        `;
        return;
    }

    await loadIssueDetail();

if (commentForm) {
    commentForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearMessage();

        const description = document.getElementById("commentDescription").value.trim();
        if (!description) {
            showError("Debes escribir un comentario.");
            return;
        }

        const payload = {
            clientId: parseInt(sessionStorage.getItem("userId")), // Cambiado para asegurar que use el ID de la sesión activa
            description: description
        };

        try {
            await apiRequest(`/issues/${issueId}/comments`, "POST", payload); 
            document.getElementById("commentDescription").value = "";
            showSuccess("Comentario agregado correctamente.");
            await loadIssueDetail(); 
        } catch (error) {
            showError(error.message);
        }
    });
}

    async function loadIssueDetail() {
    try {
        const issue = await apiRequest(`/issues/${issueId}`); 
        renderIssue(issue);
        renderComments(issue.comments || []);
    } catch (error) {
        detailContainer.innerHTML = `
            <p style="color:red; font-weight:bold;">Error del servidor: ${error.message}</p>
        `;
        console.error("Error detallado:", error);
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
        const issueId = getIssueIdFromQueryString();
        console.log("ID de solicitud detectado en la URL:", issueId);
    }

 function showSuccess(message) {
    const alertMsg = document.getElementById("alertMsg");
    if (!alertMsg) return;
    alertMsg.textContent = message;
    alertMsg.className = "alert alert-success";
    alertMsg.style.display = "block";
}

function showError(message) {
    const alertMsg = document.getElementById("alertMsg");
    if (!alertMsg) return;
    alertMsg.textContent = message;
    alertMsg.className = "alert alert-error";
    alertMsg.style.display = "block";
}

function clearMessage() {
    const alertMsg = document.getElementById("alertMsg");
    if (!alertMsg) return;
    alertMsg.textContent = "";
    alertMsg.style.display = "none";
}

function getStatusBadgeClass(status) {
    const classes = {
        INGRESADO: "badge-ingresado",
        ASIGNADO: "badge-asignado",
        EN_PROGRESO: "badge-progreso",
        RESUELTO: "badge-resueltos"
    };
    return classes[status] || "badge-default";
}
});