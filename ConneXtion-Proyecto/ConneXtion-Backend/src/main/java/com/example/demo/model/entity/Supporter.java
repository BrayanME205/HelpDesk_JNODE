package com.example.demo.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Supporter")
public class Supporter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supporter_id")
    private Integer supporterId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "first_surname", nullable = false, length = 80)
    private String firstSurname;

    @Column(name = "second_surname", nullable = false, length = 80)
    private String secondSurname;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 256)
    private String passwordHash;

    @Column(name = "is_supervisor", nullable = false)
    private Boolean isSupervisor = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "Supporter_Service",
        joinColumns = @JoinColumn(name = "supporter_id"),
        inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private Set<Service> services = new HashSet<>();

    public Supporter() {}

    // Getters y Setters
    public Integer getSupporterId()              { return supporterId; }
    public void setSupporterId(Integer v)        { this.supporterId = v; }

    public String getName()                      { return name; }
    public void setName(String name)             { this.name = name; }

    public String getFirstSurname()              { return firstSurname; }
    public void setFirstSurname(String v)        { this.firstSurname = v; }

    public String getSecondSurname()             { return secondSurname; }
    public void setSecondSurname(String v)       { this.secondSurname = v; }

    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }

    public String getPasswordHash()              { return passwordHash; }
    public void setPasswordHash(String v)        { this.passwordHash = v; }

    public Boolean getIsSupervisor()             { return isSupervisor; }
    public void setIsSupervisor(Boolean v)       { this.isSupervisor = v; }

    public Boolean getIsActive()                 { return isActive; }
    public void setIsActive(Boolean isActive)    { this.isActive = isActive; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime v)    { this.createdAt = v; }

    public Set<Service> getServices()            { return services; }
    public void setServices(Set<Service> s)      { this.services = s; }
}