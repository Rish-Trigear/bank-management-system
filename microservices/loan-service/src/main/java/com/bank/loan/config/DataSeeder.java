package com.bank.loan.config;

import com.bank.loan.model.Loan;
import com.bank.loan.model.MaritalStatus;
import com.bank.loan.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class DataSeeder implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (loanRepository.count() == 0) {
            seedLoanData();
        }
    }
    
    private void seedLoanData() {
        logger.info("Seeding loan data...");
        
        Loan loan1 = new Loan();
        loan1.setLoanId("LOAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        loan1.setCustomerSsnId("1001001");
        loan1.setOccupation("Software Engineer");
        loan1.setEmployerName("Tech Corp");
        loan1.setEmployerAddress("123 Tech Street, Silicon Valley, CA");
        loan1.setEmail("john.doe@example.com");
        loan1.setAddress("456 Oak Street, San Francisco, CA");
        loan1.setMaritalStatus(MaritalStatus.MARRIED);
        loan1.setContactNumber("1234567890");
        loan1.setLoanAmount(new BigDecimal("250000.00"));
        loan1.setDurationMonths(240);
        
        Loan loan2 = new Loan();
        loan2.setLoanId("LOAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        loan2.setCustomerSsnId("1001002");
        loan2.setOccupation("Marketing Manager");
        loan2.setEmployerName("Marketing Inc");
        loan2.setEmployerAddress("789 Marketing Blvd, Los Angeles, CA");
        loan2.setEmail("jane.smith@example.com");
        loan2.setAddress("321 Pine Street, Los Angeles, CA");
        loan2.setMaritalStatus(MaritalStatus.SINGLE);
        loan2.setContactNumber("0987654321");
        loan2.setLoanAmount(new BigDecimal("150000.00"));
        loan2.setDurationMonths(180);
        
        loanRepository.save(loan1);
        loanRepository.save(loan2);
        
        logger.info("Loan data seeded successfully. Total loans: {}", loanRepository.count());
    }
}