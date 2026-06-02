package com.example.demo.controllers;

import com.example.demo.models.entities.Issue;
import com.example.demo.models.repositories.IssueRepository;
import com.example.demo.services.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "*") 
public class IssueController {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private AssignmentService assignmentService;

    @GetMapping("/pending")
    public List<Issue> getPendingIssues() {
        return issueRepository.findByStatus("Ingresado");
    }

    @PostMapping("/{issueId}/assign/{supporterId}")
    public String assignIssue(@PathVariable Integer issueId, @PathVariable Integer supporterId) {
        assignmentService.assignIssue(issueId, supporterId);
        return "Tiquete " + issueId + " asignado correctamente al soportista " + supporterId;
    }
    
    @PostMapping("/{issueId}/resolve")
    public String resolveIssue(@PathVariable Integer issueId, @RequestBody String comment) {
        // Por defecto usaremos el SoftwareResolver para el ejemplo de polimorfismo
        // (En un caso real, el frontend podría enviar qué tipo de problema fue)
        assignmentService.resolveIssue(issueId, comment, new com.example.demo.services.SoftwareResolver());
        return "Tiquete resuelto con éxito";
    }
}
