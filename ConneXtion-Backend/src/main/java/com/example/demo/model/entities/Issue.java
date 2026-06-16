package com.example.demo.model.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Issue")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_id")
    private Integer id;

    @Column(name = "request_number", nullable = false, unique = true, length = 50)
    private String requestNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Column(name = "reference_address", length = 200)
    private String referenceAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IssueStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "classification", nullable = false, length = 10)
    private IssueClassification classification;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_supporter_id")
    private Supporter assignedSupporter;

    @Column(name = "resolution_comment", length = 1000)
    private String resolutionComment;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Issue() {
        this.registeredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber(String requestNumber) {
        this.requestNumber = requestNumber;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getReferenceAddress() {
        return referenceAddress;
    }

    public void setReferenceAddress(String referenceAddress) {
        this.referenceAddress = referenceAddress;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public IssueClassification getClassification() {
        return classification;
    }

    public void setClassification(IssueClassification classification) {
        this.classification = classification;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Supporter getAssignedSupporter() {
        return assignedSupporter;
    }

    public void setAssignedSupporter(Supporter assignedSupporter) {
        this.assignedSupporter = assignedSupporter;
    }

    public String getResolutionComment() {
        return resolutionComment;
    }

    public void setResolutionComment(String resolutionComment) {
        this.resolutionComment = resolutionComment;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}