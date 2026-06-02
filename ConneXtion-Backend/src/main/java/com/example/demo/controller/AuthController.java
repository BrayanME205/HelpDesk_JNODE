package com.example.demo.controller;

import com.example.demo.model.entity.Client;
import com.example.demo.model.entity.Supporter;
import com.example.demo.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // CU2 / CU8 — Login unificado con sesión HTTP
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

        // Crear sesión HTTP en el servidor
        HttpSession session = request.getSession(true);

        if (user instanceof Client c) {
            session.setAttribute("userId", c.getClientId());
            session.setAttribute("userName", c.getName());
            session.setAttribute("email", c.getEmail());
            session.setAttribute("role", "CLIENT");

            return ResponseEntity.ok(Map.of(
                    "id", c.getClientId(),
                    "name", c.getName(),
                    "role", "CLIENT",
                    "email", c.getEmail()
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

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    // CU3 / CU9 — Logout — invalida la sesión en el servidor
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada exitosamente."));
    }

    // Verificar sesión activa (para proteger páginas)
    @GetMapping("/session")
    public ResponseEntity<?> getSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("role") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No hay sesión activa."));
        }
        return ResponseEntity.ok(Map.of(
                "userId", session.getAttribute("userId"),
                "userName", session.getAttribute("userName"),
                "email", session.getAttribute("email"),
                "role", session.getAttribute("role")
        ));
    }
}
