package com.example.demo.controller;

import com.example.demo.model.entities.ChatMessage;
import com.example.demo.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class ChatApiController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @GetMapping("/history/{issueId}")
    public List<ChatMessage> getChatHistory(@PathVariable Integer issueId) {
        return chatMessageRepository.findByIssueIdOrderBySentAtAsc(issueId);
    }
}
