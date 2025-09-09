package com.bank.gateway.service;

import com.bank.gateway.dto.DashboardResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DashboardService {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${services.customer.url}")
    private String customerServiceUrl;
    
    @Value("${services.employee.url}")
    private String employeeServiceUrl;
    
    @Value("${services.transaction.url}")
    private String transactionServiceUrl;
    
    @Value("${services.loan.url}")
    private String loanServiceUrl;
    
    public DashboardResponse getDashboardSummary() {
        DashboardResponse dashboard = new DashboardResponse();
        
        try {
            long totalCustomers = getCustomerCount();
            dashboard.setTotalCustomers(totalCustomers);
        } catch (Exception e) {
            logger.error("Error fetching customer count: {}", e.getMessage());
            dashboard.setTotalCustomers(0);
        }
        
        try {
            long totalEmployees = getEmployeeCount();
            dashboard.setTotalEmployees(totalEmployees);
        } catch (Exception e) {
            logger.error("Error fetching employee count: {}", e.getMessage());
            dashboard.setTotalEmployees(0);
        }
        
        try {
            long totalLoans = getLoanCount();
            dashboard.setTotalLoanRequests(totalLoans);
        } catch (Exception e) {
            logger.error("Error fetching loan count: {}", e.getMessage());
            dashboard.setTotalLoanRequests(0);
        }
        
        try {
            double totalBalance = getTotalBankBalance();
            dashboard.setTotalBankBalance(totalBalance);
        } catch (Exception e) {
            logger.error("Error fetching total balance: {}", e.getMessage());
            dashboard.setTotalBankBalance(0.0);
        }
        
        return dashboard;
    }
    
    private long getCustomerCount() {
        String url = customerServiceUrl + "/customers/count";
        logger.info("Fetching customer count from: {}", url);
        
        ResponseEntity<Long> response = restTemplate.exchange(
            url, HttpMethod.GET, null, Long.class);
        
        return response.getBody() != null ? response.getBody() : 0;
    }
    
    private long getEmployeeCount() {
        String url = employeeServiceUrl + "/employees/count";
        logger.info("Fetching employee count from: {}", url);
        
        ResponseEntity<Long> response = restTemplate.exchange(
            url, HttpMethod.GET, null, Long.class);
        
        return response.getBody() != null ? response.getBody() : 0;
    }
    
    private long getLoanCount() {
        String url = loanServiceUrl + "/loans/count";
        logger.info("Fetching loan count from: {}", url);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, null, String.class);
            
            if (response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return jsonNode.get("count").asLong(0);
            }
        } catch (Exception e) {
            logger.error("Error parsing loan count response: {}", e.getMessage());
        }
        
        return 0;
    }
    
    private double getTotalBankBalance() {
        String url = transactionServiceUrl + "/transactions/total-balance";
        logger.info("Fetching total balance from: {}", url);
        
        ResponseEntity<Double> response = restTemplate.exchange(
            url, HttpMethod.GET, null, Double.class);
        
        return response.getBody() != null ? response.getBody() : 0.0;
    }
}