package com.bank.gateway.controller;

import com.bank.gateway.service.GatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class GatewayController {
    
    private static final Logger logger = LoggerFactory.getLogger(GatewayController.class);
    
    @Autowired
    private GatewayService gatewayService;
    
    @RequestMapping(value = "/api/**", method = {RequestMethod.GET, RequestMethod.POST, 
                   RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}, 
                   produces = "application/json")
    public ResponseEntity<String> gateway(@RequestBody(required = false) String body,
                                        HttpServletRequest request) {
        
        String path = request.getRequestURI();
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        logger.info("Gateway received {} request to {}", method, path);
        
        HttpEntity<String> requestEntity = new HttpEntity<>(body);
        
        ResponseEntity<String> response = gatewayService.forwardRequest(path, method, requestEntity, request);
        
        // Ensure proper content type header
        return ResponseEntity.status(response.getStatusCode())
                .header("Content-Type", "application/json")
                .body(response.getBody());
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("API Gateway is running");
    }
}