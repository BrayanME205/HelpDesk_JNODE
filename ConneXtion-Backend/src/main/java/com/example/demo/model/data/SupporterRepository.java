package com.example.demo.model.data;

import com.example.demo.model.entity.Supporter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SupporterRepository extends JpaRepository<Supporter, Integer> {

    Optional<Supporter> findByEmail(String email);

    boolean existsByEmail(String email);
}
