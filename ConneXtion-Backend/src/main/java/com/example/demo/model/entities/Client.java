package com.example.demo.model.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    private Integer clientId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "first_surname", nullable = false, length = 80)
    private String firstSurname;

    @Column(name = "second_surname", nullable = false, length = 80)
    private String secondSurname;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "second_contact", length = 80)
    private String secondContact;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 256)
    private String passwordHash;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "Client_Service",
        joinColumns = @JoinColumn(name = "client_id"),
        inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private Set<Service> services = new HashSet<>();

    // Constructor vacío obligatorio para JPA
    public Client() {}

    // Getters y Setters
    public Integer getClientId()               { return clientId; }
    public void setClientId(Integer clientId)  { this.clientId = clientId; }

    public String getName()                    { return name; }
    public void setName(String name)           { this.name = name; }

    public String getFirstSurname()            { return firstSurname; }
    public void setFirstSurname(String v)      { this.firstSurname = v; }

    public String getSecondSurname()           { return secondSurname; }
    public void setSecondSurname(String v)     { this.secondSurname = v; }

    public String getAddress()                 { return address; }
    public void setAddress(String address)     { this.address = address; }

    public String getPhone()                   { return phone; }
    public void setPhone(String phone)         { this.phone = phone; }

    public String getSecondContact()           { return secondContact; }
    public void setSecondContact(String v)     { this.secondContact = v; }

    public String getEmail()                   { return email; }
    public void setEmail(String email)         { this.email = email; }

    public String getPasswordHash()            { return passwordHash; }
    public void setPasswordHash(String v)      { this.passwordHash = v; }

    public Boolean getIsActive()               { return isActive; }
    public void setIsActive(Boolean isActive)  { this.isActive = isActive; }

    public LocalDateTime getCreatedAt()        { return createdAt; }
    public void setCreatedAt(LocalDateTime v)  { this.createdAt = v; }

    public Set<Service> getServices()          { return services; }
    public void setServices(Set<Service> s)    { this.services = s; }
}