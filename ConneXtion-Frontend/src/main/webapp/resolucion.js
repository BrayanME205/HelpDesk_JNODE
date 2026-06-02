// js/resolucion.js
const API_URL = 'http://localhost:8081/api/issues';

async function resolveIssue(event) {
    event.preventDefault(); // Evita que la página se recargue

    const issueId = document.getElementById('issueId').value;
    const comment = document.getElementById('resolutionComment').value;

    try {
        const response = await fetch(`${API_URL}/${issueId}/resolve`, {
            method: 'POST',
            headers: {
                'Content-Type': 'text/plain' // Enviamos el comentario como texto plano
            },
            body: comment
        });

        if (response.ok) {
            const message = await response.text();
            alert("¡Éxito! " + message);
            document.getElementById('resolveForm').reset(); // Limpia el formulario
        } else {
            alert('Error: Verifica que el tiquete exista y esté "En Progreso".');
        }
    } catch (error) {
        console.error('Error de conexión:', error);
        alert('Error conectando con el servidor.');
    }
}

// Asignar el evento al formulario cuando cargue la página
window.onload = () => {
    document.getElementById('resolveForm').addEventListener('submit', resolveIssue);
};

