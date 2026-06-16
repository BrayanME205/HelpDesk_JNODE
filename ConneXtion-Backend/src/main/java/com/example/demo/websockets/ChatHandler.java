package com.example.demo.websockets;

import com.example.demo.model.entities.ChatMessage;
import com.example.demo.repository.ChatMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatHandler extends TextWebSocketHandler {

    private final ConcurrentHashMap<String, Set<WebSocketSession>> ticketRooms = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final ChatMessageRepository chatMessageRepository;

    public ChatHandler(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String issueId = getIssueIdFromSession(session);

        if (issueId != null) {
            ticketRooms.computeIfAbsent(issueId, k -> Collections.synchronizedSet(new HashSet<>())).add(session);
        } else {
            System.err.println("Conexión rechazada: Falta issueId en la URL.");
            session.close();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String clientMessage = message.getPayload();
        String issueId = getIssueIdFromSession(session);

        if (issueId != null && ticketRooms.containsKey(issueId)) {
            try {
                com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(clientMessage);

                if (jsonNode.has("type") && "chat".equals(jsonNode.get("type").asText())) {
                    ChatMessage dbMessage = new ChatMessage();
                    dbMessage.setIssueId(Integer.parseInt(issueId));
                    dbMessage.setSenderId(jsonNode.get("senderId").asText());
                    dbMessage.setSenderName(jsonNode.get("senderName").asText());
                    dbMessage.setSenderRole(jsonNode.get("senderRole").asText());
                    dbMessage.setContent(jsonNode.get("content").asText());
                    dbMessage.setSentAt(java.time.LocalDateTime.now());
                    
                    chatMessageRepository.save(dbMessage);
                }
            } catch (Exception e) {
                System.err.println("ALERTA - Error en bloque de persistencia: " + e.getMessage());
                e.printStackTrace();
            }

            // Broadcast en tiempo real a la sala
            synchronized (ticketRooms.get(issueId)) {
                for (WebSocketSession webSocketSession : ticketRooms.get(issueId)) {
                    if (webSocketSession.isOpen()) {
                        try {
                            webSocketSession.sendMessage(new TextMessage(clientMessage));
                        } catch (IOException e) {
                            System.err.println("Error enviando mensaje: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        String issueId = getIssueIdFromSession(session);
        if (issueId != null && ticketRooms.containsKey(issueId)) {
            ticketRooms.get(issueId).remove(session);
            if (ticketRooms.get(issueId).isEmpty()) {
                ticketRooms.remove(issueId);
            }
        }
    }

    private String getIssueIdFromSession(WebSocketSession session) {
        if (session.getUri() != null && session.getUri().getQuery() != null) {
            String query = session.getUri().getQuery();
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1 && "issueId".equals(pair[0])) {
                    return pair[1];
                }
            }
        }
        return null;
    }

    public void sendStateNotification(Integer issueId, String messageContent) {
        String roomId = String.valueOf(issueId);
        Set<WebSocketSession> sessions = ticketRooms.get(roomId);

        if (sessions != null) {
            String jsonNotification = String.format(
                    "{\"type\":\"notification\",\"content\":\"%s\",\"senderId\":\"SYSTEM\"}",
                    messageContent
            );
            TextMessage message = new TextMessage(jsonNotification);

            synchronized (sessions) {
                for (WebSocketSession session : sessions) {
                    if (session.isOpen()) {
                        try {
                            session.sendMessage(message);
                        } catch (IOException e) {
                            System.err.println("Error enviando notificación push: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}