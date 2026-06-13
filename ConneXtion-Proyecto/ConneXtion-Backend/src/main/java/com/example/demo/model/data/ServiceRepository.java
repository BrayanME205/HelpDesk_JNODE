package com.example.demo.model.data;

import com.example.demo.model.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Integer> {

    List<Service> findByServiceIdIn(List<Integer> ids);
}
