package com.bank.loan.service;

import com.bank.loan.dto.CustomerDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CustomerValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerValidationService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private JwtService jwtService;
    
    @Value("${services.customer.url}")
    private String customerServiceUrl;
    
    public boolean customerExists(String ssnId) {
        try {
            String url = customerServiceUrl + "/customers/" + ssnId;
            logger.info("Validating customer existence: {}", ssnId);
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<CustomerDto> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, CustomerDto.class);
                
            CustomerDto customer = response.getBody();
            boolean exists = customer != null && customer.getSsnId() != null;
            
            logger.info("Customer validation result for {}: {}", ssnId, exists);
            return exists;
            
        } catch (Exception e) {
            logger.warn("Customer validation failed for {}: {}", ssnId, e.getMessage());
            return false;
        }
    }
    
    public CustomerDto getCustomer(String ssnId) {
        try {
            String url = customerServiceUrl + "/customers/" + ssnId;
            logger.info("Fetching customer details: {}", ssnId);
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<CustomerDto> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, CustomerDto.class);
                
            return response.getBody();
            
        } catch (Exception e) {
            logger.warn("Failed to fetch customer {}: {}", ssnId, e.getMessage());
            return null;
        }
    }
    
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            String serviceToken = jwtService.generateToken(auth.getName());
            headers.set("Authorization", "Bearer " + serviceToken);
        }
        
        return headers;
    }
}