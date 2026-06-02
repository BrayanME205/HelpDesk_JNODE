/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.models.repositories;

import com.example.demo.models.entities.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Integer> {

    // Spring Boot es tan inteligente que con solo nombrar el método así, 
    // él hace un "SELECT * FROM Issue WHERE status = ?" por debajo.
    List<Issue> findByStatus(String status);
}
