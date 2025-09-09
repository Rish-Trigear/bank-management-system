package com.bank.customer.config;

import com.bank.customer.model.Customer;
import com.bank.customer.model.Gender;
import com.bank.customer.model.MaritalStatus;
import com.bank.customer.model.Role;
import com.bank.customer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        if (customerRepository.count() == 0) {
            seedCustomers();
        }
    }
    
    private void seedCustomers() {
        Customer customer1 = new Customer();
        customer1.setSsnId("1001001");
        customer1.setFirstName("John");
        customer1.setLastName("Doe");
        customer1.setEmail("john.doe@example.com");
        customer1.setPasswordHash(passwordEncoder.encode("password123"));
        customer1.setAddress("123 Main Street, Springfield, IL 62701");
        customer1.setContactNumber("555-0101");
        customer1.setAadharNumber("123456789012");
        customer1.setPanNumber("ABCDE1234F");
        customer1.setAccountNumber("ACC1001001001");
        customer1.setDateOfBirth(LocalDate.of(1985, 6, 15));
        customer1.setGender(Gender.MALE);
        customer1.setMaritalStatus(MaritalStatus.MARRIED);
        customer1.setRole(Role.CUSTOMER);
        
        Customer customer2 = new Customer();
        customer2.setSsnId("1001002");
        customer2.setFirstName("Jane");
        customer2.setLastName("Smith");
        customer2.setEmail("jane.smith@example.com");
        customer2.setPasswordHash(passwordEncoder.encode("password123"));
        customer2.setAddress("456 Oak Avenue, Chicago, IL 60601");
        customer2.setContactNumber("555-0102");
        customer2.setAadharNumber("234567890123");
        customer2.setPanNumber("FGHIJ5678K");
        customer2.setAccountNumber("ACC1001002002");
        customer2.setDateOfBirth(LocalDate.of(1990, 3, 22));
        customer2.setGender(Gender.FEMALE);
        customer2.setMaritalStatus(MaritalStatus.SINGLE);
        customer2.setRole(Role.CUSTOMER);
        
        Customer customer3 = new Customer();
        customer3.setSsnId("1001003");
        customer3.setFirstName("Michael");
        customer3.setLastName("Johnson");
        customer3.setEmail("michael.johnson@example.com");
        customer3.setPasswordHash(passwordEncoder.encode("password123"));
        customer3.setAddress("789 Pine Street, New York, NY 10001");
        customer3.setContactNumber("555-0103");
        customer3.setAadharNumber("345678901234");
        customer3.setPanNumber("KLMNO9012P");
        customer3.setAccountNumber("ACC1001003003");
        customer3.setDateOfBirth(LocalDate.of(1978, 11, 8));
        customer3.setGender(Gender.MALE);
        customer3.setMaritalStatus(MaritalStatus.DIVORCED);
        customer3.setRole(Role.CUSTOMER);
        
        Customer customer4 = new Customer();
        customer4.setSsnId("1001004");
        customer4.setFirstName("Emily");
        customer4.setLastName("Davis");
        customer4.setEmail("emily.davis@example.com");
        customer4.setPasswordHash(passwordEncoder.encode("password123"));
        customer4.setAddress("321 Elm Drive, Los Angeles, CA 90210");
        customer4.setContactNumber("555-0104");
        customer4.setAadharNumber("456789012345");
        customer4.setPanNumber("PQRST3456U");
        customer4.setAccountNumber("ACC1001004004");
        customer4.setDateOfBirth(LocalDate.of(1992, 9, 14));
        customer4.setGender(Gender.FEMALE);
        customer4.setMaritalStatus(MaritalStatus.SINGLE);
        customer4.setRole(Role.CUSTOMER);
        
        customerRepository.save(customer1);
        customerRepository.save(customer2);
        customerRepository.save(customer3);
        customerRepository.save(customer4);
        
        System.out.println("Customer data seeded successfully!");
        System.out.println("Sample customers:");
        System.out.println("1. SSN ID: 1001001, Email: john.doe@example.com, Name: John Doe");
        System.out.println("2. SSN ID: 1001002, Email: jane.smith@example.com, Name: Jane Smith");
        System.out.println("3. SSN ID: 1001003, Email: michael.johnson@example.com, Name: Michael Johnson");
        System.out.println("4. SSN ID: 1001004, Email: emily.davis@example.com, Name: Emily Davis");
        System.out.println("Default password for all: password123");
    }
}