/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.services;
import com.example.demo.models.entities.Issue;
import org.springframework.stereotype.Component;

@Component("softwareResolver")
public class SoftwareResolver implements IssueResolver {
    @Override
    public void resolve(Issue issue, String comment) {
        issue.setResolutionComment("[Solución de Software] - " + comment);
        issue.setStatus("Resuelto");
    }
}
