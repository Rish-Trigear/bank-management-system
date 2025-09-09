package com.bank.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;

@Service
public class GatewayService {
    
    private static final Logger logger = LoggerFactory.getLogger(GatewayService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${services.customer.url}")
    private String customerServiceUrl;
    
    @Value("${services.employee.url}")
    private String employeeServiceUrl;
    
    @Value("${services.transaction.url}")
    private String transactionServiceUrl;
    
    @Value("${services.loan.url}")
    private String loanServiceUrl;
    
    public ResponseEntity<String> forwardRequest(String path, HttpMethod method, 
                                               HttpEntity<String> requestEntity, 
                                               HttpServletRequest request) {
        try {
            String targetUrl = determineTargetUrl(path);
            if (targetUrl == null) {
                logger.error("Unable to route request to path: {}", path);
                return new ResponseEntity<>("Service not found", HttpStatus.NOT_FOUND);
            }
            
            // Remove /api prefix: /api/customers/login -> /customers/login
            String servicePath = path.substring(4); // Remove "/api"
            String fullUrl = targetUrl + servicePath;
            logger.info("Forwarding {} {} to {}", method, path, fullUrl);
            
            HttpHeaders headers = createForwardingHeaders(request);
            if (requestEntity.getBody() != null) {
                headers.setContentType(MediaType.APPLICATION_JSON);
            }
            
            HttpEntity<String> forwardEntity = new HttpEntity<>(requestEntity.getBody(), headers);
            
            ResponseEntity<String> response = restTemplate.exchange(fullUrl, method, forwardEntity, String.class);
            logger.info("Successfully forwarded request. Response status: {}", response.getStatusCode());
            return response;
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("Client error forwarding request to {}: {} - {}", path, e.getStatusCode(), e.getResponseBodyAsString());
            return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            logger.error("Server error forwarding request to {}: {} - {}", path, e.getStatusCode(), e.getResponseBodyAsString());
            return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (Exception e) {
            logger.error("Error forwarding request to {}: {}", path, e.getMessage(), e);
            return new ResponseEntity<>("Gateway error: " + e.getMessage(), 
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    private String determineTargetUrl(String path) {
        if (path.startsWith("/api/customers")) {
            return customerServiceUrl;
        } else if (path.startsWith("/api/employees")) {
            return employeeServiceUrl;
        } else if (path.startsWith("/api/transactions")) {
            return transactionServiceUrl;
        } else if (path.startsWith("/api/loans")) {
            return loanServiceUrl;
        } else if (path.startsWith("/api/dashboard")) {
            return customerServiceUrl;
        }
        return null;
    }
    
    private HttpHeaders createForwardingHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            for (String headerName : Collections.list(headerNames)) {
                String headerValue = request.getHeader(headerName);
                if (headerValue != null && !headerName.equalsIgnoreCase("host")) {
                    headers.set(headerName, headerValue);
                }
            }
        }
        
        return headers;
    }
}