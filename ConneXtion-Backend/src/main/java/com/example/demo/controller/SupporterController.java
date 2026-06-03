/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller;

import com.example.demo.model.data.SupporterRepository;
import com.example.demo.model.entities.Supporter;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/supporters")
public class SupporterController {

    private final SupporterRepository supporterRepository;

    public SupporterController(SupporterRepository supporterRepository) {
        this.supporterRepository = supporterRepository;
    }

    @GetMapping
    public List<Map<String, Object>> getSupporters() {
        return supporterRepository.findAll().stream()
            .filter(s -> !s.getIsSupervisor() && s.getIsActive())
            .map(s -> Map.<String, Object>of(
                "id", s.getSupporterId(),
                "name", s.getName() + " " + s.getFirstSurname()
            ))
            .collect(Collectors.toList());
    }
}