package com.example.demo.services;
import com.example.demo.models.entities.Issue;

public interface IssueResolver {
    void resolve(Issue issue, String comment);
}
