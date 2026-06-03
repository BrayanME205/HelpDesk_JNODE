/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.service;
import com.example.demo.service.IssueResolver;
import com.example.demo.model.entities.Issue;
import com.example.demo.model.entities.IssueStatus;
import org.springframework.stereotype.Component;

@Component("softwareResolver")
public class SoftwareResolver implements IssueResolver {
    @Override
    public void resolve(Issue issue) {
        String currentComment = issue.getResolutionComment();
        
        issue.setResolutionComment("[Resolución de Software / Configuración] - " + (currentComment != null ? currentComment : ""));
        
        issue.setStatus(IssueStatus.RESUELTO);
        
        System.out.println("Solicitud " + issue.getId() + " resuelta mediante Soporte de Software.");
    }
}
