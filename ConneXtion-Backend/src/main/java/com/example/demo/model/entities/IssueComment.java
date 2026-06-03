package com.example.demo.model.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
        
@Entity
@Table(name = "Issue_Comment")
public class IssueComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_comment_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client clientAuthor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supporter_id")
    private Supporter supporterAuthor;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "comment_timestamp", nullable = false)
    private LocalDateTime commentTimestamp;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public Client getClientAuthor() {
        return clientAuthor;
    }

    public void setClientAuthor(Client clientAuthor) {
        this.clientAuthor = clientAuthor;
    }

    public Supporter getSupporterAuthor() {
        return supporterAuthor;
    }

    public void setSupporterAuthor(Supporter supporterAuthor) {
        this.supporterAuthor = supporterAuthor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCommentTimestamp() {
        return commentTimestamp;
    }

    public void setCommentTimestamp(LocalDateTime commentTimestamp) {
        this.commentTimestamp = commentTimestamp;
    }

    
     
}
