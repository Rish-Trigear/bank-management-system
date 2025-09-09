package com.bank.employee.config;

import com.bank.employee.model.Employee;
import com.bank.employee.model.Role;
import com.bank.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        if (employeeRepository.count() == 0) {
            seedEmployees();
        }
    }
    
    private void seedEmployees() {
        Employee emp1 = new Employee();
        emp1.setEmployeeId("1000001");
        emp1.setFirstName("John");
        emp1.setLastName("Smith");
        emp1.setEmail("john.smith@bank.com");
        emp1.setPasswordHash(passwordEncoder.encode("password123"));
        emp1.setAddress("123 Main St, City, State");
        emp1.setContactNumber("555-0101");
        emp1.setRole(Role.EMPLOYEE);
        
        Employee emp2 = new Employee();
        emp2.setEmployeeId("1000002");
        emp2.setFirstName("Sarah");
        emp2.setLastName("Johnson");
        emp2.setEmail("sarah.johnson@bank.com");
        emp2.setPasswordHash(passwordEncoder.encode("password123"));
        emp2.setAddress("456 Oak Ave, City, State");
        emp2.setContactNumber("555-0102");
        emp2.setRole(Role.MANAGER);
        
        Employee emp3 = new Employee();
        emp3.setEmployeeId("1000003");
        emp3.setFirstName("Mike");
        emp3.setLastName("Wilson");
        emp3.setEmail("mike.wilson@bank.com");
        emp3.setPasswordHash(passwordEncoder.encode("password123"));
        emp3.setAddress("789 Pine St, City, State");
        emp3.setContactNumber("555-0103");
        emp3.setRole(Role.EMPLOYEE);
        
        employeeRepository.save(emp1);
        employeeRepository.save(emp2);
        employeeRepository.save(emp3);
        
        System.out.println("Employee data seeded successfully!");
        System.out.println("Sample employees:");
        System.out.println("1. Employee ID: 1000001, Email: john.smith@bank.com, Role: EMPLOYEE");
        System.out.println("2. Employee ID: 1000002, Email: sarah.johnson@bank.com, Role: MANAGER");
        System.out.println("3. Employee ID: 1000003, Email: mike.wilson@bank.com, Role: EMPLOYEE");
        System.out.println("Default password for all: password123");
    }
}