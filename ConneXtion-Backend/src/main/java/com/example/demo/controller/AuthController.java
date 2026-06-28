package com.example.demo.controller;

import com.example.demo.model.entities.Client;
import com.example.demo.model.entities.Service;
import com.example.demo.model.entities.Supporter;
import com.example.demo.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    private List<Map<String, Object>> mapServices(Set<Service> services) {
        return services.stream()
                .map(service -> Map.<String, Object>of(
                "serviceId", service.getServiceId(),
                "name", service.getName()
        ))
                .collect(Collectors.toList());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Correo y contraseña son obligatorios."));
        }

        Object user = authService.login(email, password);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Correo o contraseña incorrectos."));
        }

        HttpSession session = request.getSession(true);

        if (user instanceof Client c) {
            session.setAttribute("userId", c.getClientId());
            session.setAttribute("userName", c.getName());
            session.setAttribute("email", c.getEmail());
            session.setAttribute("role", "CLIENT");

            return ResponseEntity.ok(Map.of(
                    "id", c.getClientId(),
                    "clientId", c.getClientId(),
                    "name", c.getName(),
                    "role", "CLIENT",
                    "email", c.getEmail(),
                    "services", mapServices(c.getServices())
            ));
        }

        if (user instanceof Supporter s) {
            String role = s.getIsSupervisor() ? "SUPERVISOR" : "SUPPORTER";

            session.setAttribute("userId", s.getSupporterId());
            session.setAttribute("userName", s.getName());
            session.setAttribute("email", s.getEmail());
            session.setAttribute("role", role);

            return ResponseEntity.ok(Map.of(
                    "id", s.getSupporterId(),
                    "name", s.getName(),
                    "role", role,
                    "email", s.getEmail()
            ));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "No se pudo completar el login."));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok(Map.of("message", "Sesión cerrada exitosamente."));
    }

    @GetMapping("/session")
    public ResponseEntity<?> getSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("role") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No hay sesión activa."));
        }

        String role = (String) session.getAttribute("role");
        Integer userId = (Integer) session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");
        String email = (String) session.getAttribute("email");

        if ("CLIENT".equals(role)) {
            Object user = authService.findClientById(userId);

            if (user instanceof Client c) {
                return ResponseEntity.ok(Map.of(
                        "userId", c.getClientId(),
                        "clientId", c.getClientId(),
                        "userName", c.getName(),
                        "email", c.getEmail(),
                        "role", "CLIENT",
                        "services", mapServices(c.getServices())
                ));
            }
        }

        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "userName", userName,
                "email", email,
                "role", role
        ));
    }
}
