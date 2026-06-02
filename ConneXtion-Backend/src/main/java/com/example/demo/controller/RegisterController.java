package com.example.demo.controller;

import com.example.demo.model.entity.Client;
import com.example.demo.model.entity.Supporter;
import com.example.demo.model.entity.Service;
import com.example.demo.model.data.ClientRepository;
import com.example.demo.model.data.SupporterRepository;
import com.example.demo.model.data.ServiceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class RegisterController {

    private final ClientRepository clientRepository;
    private final SupporterRepository supporterRepository;
    private final ServiceRepository serviceRepository;

    public RegisterController(ClientRepository clientRepository,
            SupporterRepository supporterRepository,
            ServiceRepository serviceRepository) {
        this.clientRepository = clientRepository;
        this.supporterRepository = supporterRepository;
        this.serviceRepository = serviceRepository;
    }

    // CU1 — Registrar cliente
    @PostMapping("/register")
    public ResponseEntity<?> registerClient(@RequestBody Map<String, Object> body) {

        String email = (String) body.get("email");

        if (clientRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "El correo ya está registrado."));
        }

        List<Integer> serviceIds = (List<Integer>) body.get("serviceIds");
        if (serviceIds == null || serviceIds.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Debe seleccionar al menos un servicio."));
        }

        List<Service> services = serviceRepository.findByServiceIdIn(serviceIds);
        if (services.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Servicios no válidos."));
        }

        Client client = new Client();
        client.setName((String) body.get("name"));
        client.setFirstSurname((String) body.get("firstSurname"));
        client.setSecondSurname((String) body.get("secondSurname"));
        client.setEmail(email);
        client.setPasswordHash((String) body.get("password"));
        client.setAddress((String) body.get("address"));
        client.setPhone((String) body.get("phone"));
        client.setSecondContact((String) body.get("secondContact"));
        client.setServices(new HashSet<>(services));
        client.setIsActive(true);

        clientRepository.save(client);
        return ResponseEntity.ok(Map.of("message", "Cliente registrado exitosamente."));
    }

    // CU7 — Registrar soportista / supervisor
    @PostMapping("/register/support")
    public ResponseEntity<?> registerSupporter(@RequestBody Map<String, Object> body) {

        String email = (String) body.get("email");

        if (supporterRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "El correo ya está registrado."));
        }

        List<Integer> serviceIds = (List<Integer>) body.get("serviceIds");
        if (serviceIds == null || serviceIds.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Debe asignar al menos un servicio."));
        }

        List<Service> services = serviceRepository.findByServiceIdIn(serviceIds);

        Boolean isSupervisor = body.get("isSupervisor") != null
                && (Boolean) body.get("isSupervisor");

        Supporter supporter = new Supporter();
        supporter.setName((String) body.get("name"));
        supporter.setFirstSurname((String) body.get("firstSurname"));
        supporter.setSecondSurname((String) body.get("secondSurname"));
        supporter.setEmail(email);
        supporter.setPasswordHash((String) body.get("password"));
        supporter.setIsSupervisor(isSupervisor);
        supporter.setServices(new HashSet<>(services));
        supporter.setIsActive(true);

        supporterRepository.save(supporter);
        return ResponseEntity.ok(Map.of("message", "Usuario de soporte registrado exitosamente."));
    }

    // Listar servicios (para los formularios)
    @GetMapping("/services")
    public ResponseEntity<?> getServices() {
        return ResponseEntity.ok(serviceRepository.findAll());
    }
}
