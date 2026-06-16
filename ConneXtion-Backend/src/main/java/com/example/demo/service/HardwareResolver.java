package com.example.demo.service;

import com.example.demo.model.entities.Issue;
import com.example.demo.model.entities.IssueStatus;
import org.springframework.stereotype.Component;

@Component("hardwareResolver")
public class HardwareResolver implements IssueResolver {

    @Override
    public void resolve(Issue issue) {
        String currentComment = issue.getResolutionComment();

        issue.setResolutionComment("[Reparación de Hardware] - " + (currentComment != null ? currentComment : ""));

        issue.setStatus(IssueStatus.RESUELTO);

        System.out.println("Solicitud " + issue.getId() + " resuelta mediante Soporte de Hardware.");
    }
}
