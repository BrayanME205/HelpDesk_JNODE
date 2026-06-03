package com.example.demo.controller;

import com.example.demo.model.data.AddCommentRequest;
import com.example.demo.model.data.CreateIssueRequest;
import com.example.demo.model.data.IssueService;
import com.example.demo.model.entities.Issue;
import com.example.demo.model.entities.IssueComment;
import com.example.demo.model.entities.IssueStatus;
import com.example.demo.service.AssignmentService;
import com.example.demo.repository.IssueRepository; 
import com.example.demo.service.SoftwareResolver;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueService issueService;
    private final AssignmentService assignmentService;
    private final IssueRepository issueRepository;
    private final SoftwareResolver softwareResolver;

    public IssueController(IssueService issueService, 
                           AssignmentService assignmentService, 
                           IssueRepository issueRepository,SoftwareResolver softwareResolver) {
        this.issueService = issueService;
        this.assignmentService = assignmentService;
        this.issueRepository = issueRepository;
        this.softwareResolver = softwareResolver;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createIssue(@Valid @RequestBody CreateIssueRequest request) {
        Issue issue = issueService.createIssue(request);

        Map<String, Object> response = new HashMap<>();
        response.put("issueId", issue.getId());
        response.put("requestNumber", issue.getRequestNumber());
        response.put("status", issue.getStatus());
        response.put("registeredAt", issue.getRegisteredAt());
        response.put("message", "Issue created successfully");
        return response;
    }

    @GetMapping("/client/{clientId}")
    public List<Map<String, Object>> listClientIssues(@PathVariable Integer clientId) {
        return issueService.findIssuesByClient(clientId).stream().map(issue -> {
            Map<String, Object> item = new HashMap<>();
            item.put("issueId", issue.getId());
            item.put("requestNumber", issue.getRequestNumber());
            item.put("service", issue.getService().getName());
            item.put("registeredAt", issue.getRegisteredAt());
            item.put("status", issue.getStatus());
            return item;
        }).toList();
    }

    @GetMapping("/{issueId}")
    public Map<String, Object> getIssueDetail(@PathVariable Integer issueId) {
        Issue issue = issueService.findIssueById(issueId);
        List<IssueComment> comments = issueService.findCommentsByIssue(issueId);

        Map<String, Object> response = new HashMap<>();
        response.put("issueId", issue.getId());
        response.put("requestNumber", issue.getRequestNumber());
        response.put("description", issue.getDescription());
        response.put("registeredAt", issue.getRegisteredAt());
        response.put("status", issue.getStatus());
        response.put("service", issue.getService().getName());
        response.put("comments", comments.stream().map(comment -> {
            Map<String, Object> c = new HashMap<>();
            c.put("commentId", comment.getId());
            c.put("description", comment.getDescription());
            c.put("commentTimestamp", comment.getCommentTimestamp());
            c.put("authorEmail",
                    comment.getClientAuthor() != null
                            ? comment.getClientAuthor().getEmail()
                            : comment.getSupporterAuthor().getEmail());
            return c;
        }).toList());

        return response;
    }

    @PostMapping("/{issueId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> addComment(
            @PathVariable Integer issueId,
            @Valid @RequestBody AddCommentRequest request) {

        IssueComment comment = issueService.addClientComment(issueId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("commentId", comment.getId());
        response.put("description", comment.getDescription());
        response.put("commentTimestamp", comment.getCommentTimestamp());
        response.put("message", "Comment added successfully");
        return response;
    }

    //soporte/asignación

    @GetMapping("/pending")
    public List<Issue> getPendingIssues() {
       return assignmentService.getPendingIssues();
    }

    @PostMapping("/{issueId}/assign/{supporterId}")
    public String assignIssue(@PathVariable Integer issueId, @PathVariable Integer supporterId) {
        assignmentService.assignIssue(issueId, supporterId); // CU-13: Asignación
        return "Tiquete " + issueId + " asignado correctamente al soportista " + supporterId;
    }
    
    @PostMapping("/{issueId}/resolve")
    public String resolveIssue(@PathVariable Integer issueId, @RequestBody String comment) {
        assignmentService.resolveIssue(issueId, comment, softwareResolver); // CU-15: Resolución
        return "Tiquete resuelto con éxito";
    }
}