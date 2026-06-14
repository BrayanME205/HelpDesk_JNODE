package com.example.demo.repository;

import com.example.demo.model.entities.Issue;
import com.example.demo.model.entities.IssueStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueRepository extends JpaRepository<Issue, Integer> {

    List<Issue> findByClientClientIdOrderByRegisteredAtDesc(Integer clientId);
   List<Issue> findByStatus(IssueStatus status);
   List<Issue> findByAssignedSupporter_SupporterId(Integer supporterId);
}
