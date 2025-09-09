package com.bank.gateway.controller;

import com.bank.gateway.dto.DashboardResponse;
import com.bank.gateway.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;
    
    @GetMapping("/summary")
    public ResponseEntity<DashboardResponse> getDashboardSummary() {
        DashboardResponse response = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(response);
    }
}