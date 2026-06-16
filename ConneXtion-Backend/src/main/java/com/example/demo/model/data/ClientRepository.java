package com.example.demo.model.data;

import com.example.demo.model.entities.Client;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    Optional<Client> findByEmail(String email);
    boolean existsByEmail(String email);
}