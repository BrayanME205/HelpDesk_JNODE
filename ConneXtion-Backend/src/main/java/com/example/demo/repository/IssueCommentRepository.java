package com.example.demo.repository;


import com.example.demo.model.entities.IssueComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueCommentRepository extends JpaRepository<IssueComment, Integer> {

    List<IssueComment> findByIssueIdOrderByCommentTimestampAsc(Integer issueId);
}

