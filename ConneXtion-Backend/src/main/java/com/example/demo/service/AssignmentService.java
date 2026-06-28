package com.example.demo.service;

import com.example.demo.model.entities.Issue;
import com.example.demo.model.entities.IssueStatus;
import com.example.demo.model.entities.IssueClassification;
import com.example.demo.repository.IssueRepository;
import com.example.demo.model.data.SupporterRepository;
import com.example.demo.model.entities.Supporter;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.websockets.ChatHandler;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssignmentService {

    private final IssueRepository issueRepository;
    private final SupporterRepository supporterRepository;
    private final ChatHandler chatHandler;

    public AssignmentService(IssueRepository issueRepository, SupporterRepository supporterRepository, ChatHandler chatHandler) {
        this.issueRepository = issueRepository;
        this.supporterRepository = supporterRepository;
        this.chatHandler = chatHandler;
    }

    @Transactional
    public void assignIssue(Integer issueId, Integer supporterId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        Supporter supporter = supporterRepository.findById(supporterId)
                .orElseThrow(() -> new RuntimeException("Soportista no encontrado"));

        issue.setAssignedSupporter(supporter);
        issue.setStatus(IssueStatus.ASIGNADO);
        issue.setUpdatedAt(LocalDateTime.now());

        issueRepository.save(issue);
        System.out.println("Ticket " + issueId + " asignado manualmente al soportista " + supporterId);
    }

    @Transactional
    public void resolveIssue(Integer issueId, String comment, com.example.demo.service.IssueResolver resolver) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        if (issue.getStatus() != IssueStatus.EN_PROGRESO) {
            throw new IllegalStateException("El tiquete debe estar En Progreso para poder resolverse");
        }

        issue.setResolutionComment(comment);
        issue.setStatus(IssueStatus.RESUELTO);
        issue.setUpdatedAt(LocalDateTime.now());

        issueRepository.save(issue);

        resolver.resolve(issue);

        chatHandler.sendStateNotification(issueId, "El tiquete ha sido RESUELTO por el departamento técnico.");
    }

    @PostConstruct
    public void startIssueMonitorThread() {
        Thread monitorThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(15000);

                    List<Issue> pendingIssues = issueRepository.findByStatus(IssueStatus.INGRESADO);

                    for (Issue issue : pendingIssues) {
                        if (issue.getDescription() != null && issue.getDescription().toLowerCase().contains("urgente")) {
                            issue.setClassification(IssueClassification.ALTA);
                        } else {
                            issue.setClassification(IssueClassification.MEDIA);
                        }

                        issue.setUpdatedAt(LocalDateTime.now());
                        issueRepository.save(issue);

                        System.out.println("Se clasificó el ticket " + issue.getId() + " con prioridad " + issue.getClassification());
                    }

                } catch (InterruptedException e) {
                    System.err.println("monitoreo interrumpido: " + e.getMessage());
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error de monitoreo: " + e.getMessage());
                }
            }
        });
        monitorThread.setName("ConneXtion-Monitor-Thread");
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    public List<Issue> getPendingIssues() {
        return issueRepository.findByStatus(IssueStatus.INGRESADO);
    }

    @Transactional
    public void startIssue(Integer issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        if (issue.getStatus() != IssueStatus.INGRESADO
                && issue.getStatus() != IssueStatus.ASIGNADO) {
            throw new IllegalStateException(
                    "El tiquete debe estar Ingresado o Asignado para iniciar proceso");
        }

        issue.setStatus(IssueStatus.EN_PROGRESO);
        issue.setUpdatedAt(LocalDateTime.now());
        issueRepository.save(issue);

        chatHandler.sendStateNotification(issueId, "El soportista ha iniciado la atención de tu solicitud (Estado: EN PROGRESO).");
    }

    @Transactional
    public void updateClassification(Integer issueId, IssueClassification newClassification) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        issue.setClassification(newClassification);
        issue.setUpdatedAt(LocalDateTime.now());
        issueRepository.save(issue);

        System.out.println("Supervisor cambió manualmente la prioridad del ticket " + issueId + " a " + newClassification);
    }

}
