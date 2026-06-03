package com.example.demo.service;

import com.example.demo.model.data.ClientRepository;
import com.example.demo.model.data.SupporterRepository;
import com.example.demo.model.entities.Client;
import com.example.demo.model.entities.Supporter;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    private final ClientRepository    clientRepository;
    private final SupporterRepository supporterRepository;

    public AuthService(ClientRepository clientRepository,
                       SupporterRepository supporterRepository) {
        this.clientRepository    = clientRepository;
        this.supporterRepository = supporterRepository;
    }

    public Object login(String email, String password) {

        // Buscar en clientes
        Optional<Client> clientOpt = clientRepository.findByEmail(email);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            if (client.getIsActive() && client.getPasswordHash().equals(password)) {
                return client;
            }
        }

        // Buscar en soporte
        Optional<Supporter> supporterOpt = supporterRepository.findByEmail(email);
        if (supporterOpt.isPresent()) {
            Supporter supporter = supporterOpt.get();
            if (supporter.getIsActive() && supporter.getPasswordHash().equals(password)) {
                return supporter;
            }
        }

        return null;
    }
    
    public Client findClientById(Integer clientId) {
    return clientRepository.findById(clientId).orElse(null);
}
}