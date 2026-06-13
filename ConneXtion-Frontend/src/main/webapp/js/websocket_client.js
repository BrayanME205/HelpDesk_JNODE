// CONFIGURATION
const WS_URL = "ws://localhost:8081/chat";
let webSocket;

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
        updateStatus("Connected", "status-connected");
        appendMessage("System: Connected to Help Desk Server.", "message-server");
    };

    webSocket.onmessage = function (event) {
        appendMessage("Server: " + event.data, "message-server");
    };

    webSocket.onerror = function (error) {
        updateStatus("Error connecting to server", "status-disconnected");
        appendMessage("System Error: Could not reach the backend.", "message-server");
    };

    webSocket.onclose = function (event) {
        updateStatus("Disconnected", "status-disconnected");
        appendMessage("System: Connection closed.", "message-server");
    };
}

// UI HANDLING FUNCTIONS
function sendMessage() {
    const message = messageInput.value.trim();

    if (message !== "" && webSocket.readyState === WebSocket.OPEN) {
        // Show message in UI
        appendMessage("You: " + message, "message-client");

        // Send to backend
        webSocket.send(message);

        // Clear input
        messageInput.value = "";
    } else if (webSocket.readyState !== WebSocket.OPEN) {
        alert("Cannot send message. Disconnected from server.");
    }
}

function appendMessage(text, cssClass) {
    const listItem = document.createElement("li");
    listItem.textContent = text;
    listItem.classList.add("message-item", cssClass);

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

// EVENT LISTENERS & INITIALIZATION
function setupEventListeners() {
    sendButton.addEventListener("click", sendMessage);

    messageInput.addEventListener("keypress", function (event) {
        if (event.key === "Enter") {
            sendMessage();
        }
    });
}

function init() {
    setupEventListeners();
    connectWebSocket();
}

// Start application
init();