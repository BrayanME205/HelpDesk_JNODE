const API = 'http://localhost:8081/api';
async function resolveIssue(event) {
    event.preventDefault();
    const issueId = document.getElementById('issueId').value;
    const comment = document.getElementById('resolutionComment').value;
    try {
        const response = await fetch(`${API}/issues/${issueId}/resolve`, {
            method: 'POST',
            headers: {
                'Content-Type': 'text/plain'
            },
            body: comment
        });
        if (response.ok) {
            const message = await response.text();
            alert("¡Éxito! " + message);
            document.getElementById('resolveForm').reset();
        } else {
            alert('Error: Verifica que el tiquete exista y esté "En Progreso".');
        }
    } catch (error) {
        console.error('Error de conexión:', error);
        alert('Error conectando con el servidor.');
    }
}
window.onload = () => {
    const params = new URLSearchParams(window.location.search);
    const issueId = params.get('issueId');
    if (issueId) {
        document.getElementById('issueId').value = issueId;
    }
    document.getElementById('resolveForm').addEventListener('submit', resolveIssue);
};