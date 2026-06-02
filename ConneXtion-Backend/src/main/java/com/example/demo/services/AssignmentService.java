/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.services;

import com.example.demo.models.entities.Issue;
import com.example.demo.models.repositories.IssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
public class AssignmentService {

    @Autowired
    private IssueRepository issueRepository;

    // Asignación automática (Solo recibe el ID del ticket)
    public void assignIssue(Integer issueId) {
        System.out.println("Lógica futura: Asignando automáticamente el ticket " + issueId + " al soportista con menos carga.");
    }

    // Asignación manual (Recibe el ID del ticket Y el ID del soportista que eligió el supervisor)
    public void assignIssue(Integer issueId, Integer supporterId) {
        Issue issue = issueRepository.findById(issueId).orElse(null);
        if (issue != null) {
            issue.setSupportUserAssignedId(supporterId);
            issue.setStatus("En Progreso"); // Cambia el estado como pide el CU
            issueRepository.save(issue);
            System.out.println("Ticket " + issueId + " asignado manualmente al soportista " + supporterId);
        }
    }

    // El @PostConstruct hace que este hilo arranque automáticamente cuando inicia Spring Boot
    @PostConstruct
    public void startIssueMonitorThread() {
        Thread monitorThread = new Thread(() -> {
            while (true) {
                try {
                    // El hilo se duerme por 15 segundos para no saturar la CPU
                    Thread.sleep(15000); 
                    
                    // Va a la BD y busca todos los tickets recién creados
                    List<Issue> pendingIssues = issueRepository.findByStatus("Ingresado");
                    
                    for (Issue issue : pendingIssues) {
                        // Clasifica automáticamente la prioridad según palabras clave
                        if (issue.getDescription() != null && issue.getDescription().toLowerCase().contains("urgente")) {
                            issue.setClassification("Alta");
                        } else {
                            issue.setClassification("Media");
                        }
                        issueRepository.save(issue);
                        System.out.println("[Thread Monitor] Se clasificó el ticket " + issue.getReportNumber() + " con prioridad " + issue.getClassification());
                    }
                    
                } catch (InterruptedException e) {
                    System.out.println("Error en el hilo de monitoreo.");
                }
            }
        });
        
        monitorThread.setDaemon(true); // Esto evita que el hilo mantenga la app viva si la queremos apagar
        monitorThread.start();
        System.out.println("¡Hilo concurrente de asignaciones iniciado correctamente!");
    }
  
    public void resolveIssue(Integer issueId, String comment, IssueResolver resolver) {
        Issue issue = issueRepository.findById(issueId).orElse(null);
        
        if (issue != null && "En Progreso".equals(issue.getStatus())) {
            
            resolver.resolve(issue, comment);
            
            issueRepository.save(issue);
            System.out.println("El tiquete " + issueId + " fue resuelto exitosamente.");
        } else {
            System.out.println("Error: El tiquete no existe o no está 'En Progreso'.");
        }
    }
}
