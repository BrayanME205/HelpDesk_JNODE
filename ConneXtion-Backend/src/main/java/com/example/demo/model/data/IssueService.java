package com.example.demo.model.data;

import com.example.demo.model.entities.Issue;
import com.example.demo.model.entities.IssueClassification;
import com.example.demo.model.entities.IssueComment;
import com.example.demo.model.entities.IssueStatus;
import com.example.demo.repository.IssueCommentRepository;
import com.example.demo.repository.IssueRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.example.demo.model.entities.Client;
import com.example.demo.model.entities.Supporter;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final IssueCommentRepository issueCommentRepository;
    private final ClientRepository clientRepository;
    private final ServiceRepository serviceRepository;
    private final SupporterRepository supporterRepository;

    public IssueService(
            IssueRepository issueRepository,
            IssueCommentRepository issueCommentRepository,
            ClientRepository clientRepository,
            ServiceRepository serviceRepository, SupporterRepository supporterRepository) {
        this.issueRepository = issueRepository;
        this.issueCommentRepository = issueCommentRepository;
        this.clientRepository = clientRepository;
        this.serviceRepository = serviceRepository;
        this.supporterRepository = supporterRepository;
    }

    @Transactional
    public Issue createIssue(CreateIssueRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        com.example.demo.model.entities.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        boolean clientOwnsService = client.getServices()
                .stream()
                .anyMatch(s -> s.getServiceId().equals(service.getServiceId()));

        if (!clientOwnsService) {
            throw new IllegalArgumentException("The selected service does not belong to the client");
        }

        Issue issue = new Issue();
        issue.setRequestNumber(generateRequestNumber());
        issue.setClient(client);
        issue.setService(service);
        issue.setDescription(request.getDescription());
        issue.setContactPhone(request.getContactPhone());
        issue.setContactEmail(request.getContactEmail());
        issue.setReferenceAddress(request.getReferenceAddress());
        issue.setStatus(IssueStatus.INGRESADO);
        issue.setClassification(IssueClassification.MEDIA);
        issue.setRegisteredAt(LocalDateTime.now());
        issue.setUpdatedAt(LocalDateTime.now());

        return issueRepository.save(issue);
    }

    public List<Issue> findIssuesByClient(Integer clientId) {

        return issueRepository.findByClientClientIdOrderByRegisteredAtDesc(clientId);
    }

    public Issue findIssueById(Integer issueId) {
        return issueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));
    }

    public List<IssueComment> findCommentsByIssue(Integer issueId) {
        return issueCommentRepository.findByIssue_IdOrderByCommentTimestampAsc(issueId);
    }

    @Transactional
    public IssueComment addClientComment(Integer issueId, AddCommentRequest request) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        if (!issue.getClient().getClientId().equals(client.getClientId())) {
            throw new IllegalArgumentException("The client cannot comment on another client's issue");
        }

        IssueComment comment = new IssueComment();
        comment.setIssue(issue);
        comment.setClientAuthor(client);
        comment.setDescription(request.getDescription());
        comment.setCommentTimestamp(LocalDateTime.now());

        return issueCommentRepository.save(comment);
    }

    private String generateRequestNumber() {
        return "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Transactional
    public void updateIssueStatus(Integer issueId, String newStatus) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Tiquete no encontrado"));

        // Convertimos el string a Enum. Si el estado viene en mayúsculas desde JS, funciona directo.
        issue.setStatus(IssueStatus.valueOf(newStatus.toUpperCase()));
        issue.setUpdatedAt(LocalDateTime.now());

        issueRepository.save(issue);
    }

    @Transactional
    public void addTechnicalNote(Integer issueId, Integer supporterId, String content) {
        // Buscamos las entidades
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Tiquete no encontrado"));

        Supporter supporter = supporterRepository.findById(supporterId)
                .orElseThrow(() -> new RuntimeException("Soportista no encontrado"));

        // Creamos el comentario (nota técnica)
        IssueComment note = new IssueComment();
        note.setIssue(issue);
        note.setSupporterAuthor(supporter);
        note.setClientAuthor(null);
        note.setDescription(content);
        note.setCommentTimestamp(LocalDateTime.now());

        issueCommentRepository.save(note);

        issue.setUpdatedAt(LocalDateTime.now());
        issueRepository.save(issue);
    }

    @Transactional
    public void addSupportComment(Integer issueId, Integer supporterId, String content) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Tiquete no encontrado"));
        Supporter supporter = supporterRepository.findById(supporterId)
                .orElseThrow(() -> new RuntimeException("Soportista no encontrado"));

        IssueComment comment = new IssueComment();
        comment.setIssue(issue);
        comment.setSupporterAuthor(supporter); // El soporte es el autor
        comment.setClientAuthor(null);         // No es el cliente
        comment.setDescription(content);
        comment.setCommentTimestamp(LocalDateTime.now());

        issueCommentRepository.save(comment);

        // Opcional: Actualizar fecha de actualización del tiquete
        issue.setUpdatedAt(LocalDateTime.now());
        issueRepository.save(issue);
    }

    public List<Issue> findActiveIssues() {
        return issueRepository.findByStatusIn(
                List.of(IssueStatus.INGRESADO, IssueStatus.ASIGNADO, IssueStatus.EN_PROGRESO)
        );
    }

}
