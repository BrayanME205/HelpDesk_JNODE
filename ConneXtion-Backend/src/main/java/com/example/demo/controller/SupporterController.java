package com.example.demo.controller;

import com.example.demo.model.data.SupporterRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/supporters")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class SupporterController {

    private final SupporterRepository supporterRepository;

    public SupporterController(SupporterRepository supporterRepository) {
        this.supporterRepository = supporterRepository;
    }

    @GetMapping
    public ResponseEntity<?> getSupporters(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("role") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Sesión requerida."));
        }

        List<Map<String, Object>> supporters = supporterRepository.findAll()
                .stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .filter(s -> !Boolean.TRUE.equals(s.getIsSupervisor()))
                .map(s -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", s.getSupporterId());
                    dto.put("name", s.getName() + " " + s.getFirstSurname());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(supporters);
    }
}