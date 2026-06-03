package com.example.demo.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "Service")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Integer serviceId;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    public Service() {}

    public Integer getServiceId()           { return serviceId; }
    public void setServiceId(Integer v)     { this.serviceId = v; }

    public String getName()                 { return name; }
    public void setName(String name)        { this.name = name; }
}