// CONFIGURATION
const urlParams = new URLSearchParams(window.location.search);
let currentIssueId = urlParams.get('issueId');

if (!currentIssueId) {
    alert("Error: No se encontró el Issue ID. Usando el #1 para pruebas locales.");
    currentIssueId = "1";
}

const WS_URL = `ws://localhost:8081/chat?issueId=${currentIssueId}`;
let webSocket;

// DECISIÓN DE DISEÑO: Extraer datos reales del inicio de sesión 
const myUserId = sessionStorage.getItem('userId') || "ID_FANTASMA";
const myUserName = sessionStorage.getItem('userName') || "Usuario ConneXtion";
const myUserRole = sessionStorage.getItem('role') || "CLIENT"; // CLIENT, SUPPORTER, o SUPERVISOR

// DOM ELEMENTS
const messageList = document.getElementById("message-list");
const messageInput = document.getElementById("message-input");
const sendButton = document.getElementById("send-button");
const chatContainer = document.getElementById("chat-container");
const statusIndicator = document.getElementById("connection-status");

// WEBSOCKET FUNCTIONS
function connectWebSocket() {
    webSocket = new WebSocket(WS_URL);

    webSocket.onopen = function (event) {
        updateStatus(`Connected (Issue #${currentIssueId})`, "status-connected");
        appendMessage("System", "SERVER", "Connected to Help Desk Server.", "message-server");
    };

    webSocket.onmessage = function (event) {
        try {
            const data = JSON.parse(event.data);

            if (data.type === "notification") {
                appendMessage("NOTIFICACIÓN", "SYSTEM", data.content, "message-server");
                showPushToast(data.content);
                return;
            }

            const isMyOwnMessage = (data.senderId === myUserId && data.senderRole === myUserRole);

            if (!isMyOwnMessage) {
                appendMessage(data.senderName, data.senderRole, data.content, "message-server");
            }
        } catch (e) {
            console.error("No se pudo parsear el JSON recibido:", e);
        }
    };

    webSocket.onerror = function (error) {
        updateStatus("Error connecting to server", "status-disconnected");
        appendMessage("System Error", "SERVER", "Could not reach the backend.", "message-server");
    };

    webSocket.onclose = function (event) {
        updateStatus("Disconnected", "status-disconnected");
        appendMessage("System", "SERVER", "Connection lost. Reconnecting in 5 seconds...", "message-server");
        setTimeout(connectWebSocket, 5000); // Intenta reconectar automáticamente
    };
}

// UI HANDLING FUNCTIONS
function sendMessage() {
    const message = messageInput.value.trim();

    if (message !== "" && webSocket.readyState === WebSocket.OPEN) {

        const payload = {
            senderId: myUserId,
            senderName: myUserName,
            senderRole: myUserRole,
            content: message,
            type: "chat"
        };

        appendMessage("You", myUserRole, message, "message-client");

        webSocket.send(JSON.stringify(payload));
        messageInput.value = "";
    } else if (webSocket.readyState !== WebSocket.OPEN) {
        alert("Cannot send message. Disconnected from server.");
    }
}

// Renderizado estructurado y limpio para la UI
function appendMessage(senderName, role, messageText, cssClass) {
    const listItem = document.createElement("li");
    listItem.classList.add("message-item", cssClass);
    listItem.innerHTML = `
        <div class="message-meta"><strong>${senderName}</strong> <small>(${role})</small></div>
        <div class="message-body">${messageText}</div>
    `;
    messageList.appendChild(listItem);
    autoScrollToBottom();
}

function autoScrollToBottom() {
    chatContainer.scrollTop = chatContainer.scrollHeight;
}

function updateStatus(text, cssClass) {
    statusIndicator.textContent = text;
    statusIndicator.className = cssClass;
}

// Alerta Push Visual en Pantalla sin recargar
function showPushToast(text) {
    const toast = document.createElement("div");
    toast.style.position = "fixed";
    toast.style.bottom = "20px";
    toast.style.right = "20px";
    toast.style.background = "#0056b3";
    toast.style.color = "white";
    toast.style.padding = "15px";
    toast.style.borderRadius = "8px";
    toast.style.boxShadow = "0 4px 12px rgba(0,0,0,0.2)";
    toast.style.zIndex = "1000";
    toast.innerText = text;

    document.body.appendChild(toast);
    setTimeout(() => {
        toast.remove();
    }, 4000);
}

// Consulta a SQL Server por mensajes viejos y los renderiza de forma cronológica
async function loadChatHistory() {
    try {
        console.log(`Buscando historial para Issue ID: ${currentIssueId}. Mis datos locales son - ID: ${myUserId}, Rol: ${myUserRole}`);

        const response = await fetch(`http://localhost:8081/api/chat/history/${currentIssueId}`);
        if (response.ok) {
            const messages = await response.json();
            console.log("Mensajes crudos recibidos del backend:", messages);

            // Limpiar la lista antes de renderizar para evitar duplicados
            messageList.innerHTML = "";

            messages.forEach(msg => {
                // Forzamos conversión limpia a String y eliminamos espacios
                const senderIdStr = String(msg.senderId).trim();
                const myUserIdStr = String(myUserId).trim();

                const isMyOwnMessage = (senderIdStr === myUserIdStr && msg.senderRole === myUserRole);

                console.log(`Comparando mensaje de [${msg.senderName}]: ID ${senderIdStr} === ${myUserIdStr} (${senderIdStr === myUserIdStr}). Role ${msg.senderRole} === ${myUserRole} (${msg.senderRole === myUserRole})`);

                if (isMyOwnMessage) {
                    appendMessage("You", msg.senderRole, msg.content, "message-client");
                } else {
                    appendMessage(msg.senderName, msg.senderRole, msg.content, "message-server");
                }
            });
            autoScrollToBottom();
        } else {
            console.error("El backend respondió con un código de error al pedir el historial:", response.status);
        }
    } catch (error) {
        console.error("Fallo crítico de red en la llamada del historial:", error);
    }
}

// EVENT LISTENERS & INITIALIZATION
function setupEventListeners() {
    sendButton.addEventListener("click", sendMessage);
    messageInput.addEventListener("keypress", function (event) {
        if (event.key === "Enter") {
            sendMessage();
        }
    });
}

async function init() {
    setupEventListeners();
    await loadChatHistory();
    connectWebSocket();
}

init();