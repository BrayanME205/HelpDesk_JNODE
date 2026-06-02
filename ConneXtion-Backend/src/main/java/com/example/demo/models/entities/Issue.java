/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.models.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Issue")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_number")
    private Integer reportNumber;

    @Column(nullable = false)
    private String description;

    @Column(name = "register_timestamp")
    private LocalDateTime registerTimestamp;

    private String address;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    // Estado por defecto: Ingresado
    private String status = "Ingresado";

    // Prioridad: Alta, Media, Baja
    private String classification;

    @Column(name = "resolution_comment")
    private String resolutionComment;

    @Column(name = "client_id")
    private Integer clientId;

    @Column(name = "service_id")
    private Integer serviceId;

    @Column(name = "support_user_assigned_id")
    private Integer supportUserAssignedId;

    // Constructor vacío obligatorio para Spring Boot
    public Issue() {
        this.registerTimestamp = LocalDateTime.now();
    }

    // --- GETTERS Y SETTERS ---
    // (Puedes generarlos todos en NetBeans haciendo clic derecho -> Insert Code -> Getter and Setter -> Seleccionar todo)
    
    public Integer getReportNumber() { return reportNumber; }
    public void setReportNumber(Integer reportNumber) { this.reportNumber = reportNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public Integer getSupportUserAssignedId() { return supportUserAssignedId; }
    public void setSupportUserAssignedId(Integer supportUserAssignedId) { this.supportUserAssignedId = supportUserAssignedId; }

    public String getResolutionComment() { return resolutionComment; }
    public void setResolutionComment(String resolutionComment) { this.resolutionComment = resolutionComment; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getRegisterTimestamp() {
        return registerTimestamp;
    }

    public void setRegisterTimestamp(LocalDateTime registerTimestamp) {
        this.registerTimestamp = registerTimestamp;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

}