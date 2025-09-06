package com.bank.management.config;

import com.bank.management.model.Customer;
import com.bank.management.model.Employee;
import com.bank.management.model.Gender;
import com.bank.management.model.MaritalStatus;
import com.bank.management.model.Role;
import com.bank.management.repository.CustomerRepository;
import com.bank.management.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Random;

@Component
public class DataSeeder implements CommandLineRunner {
    
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public DataSeeder(EmployeeRepository employeeRepository, 
                     CustomerRepository customerRepository,
                     PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(String... args) throws Exception {
        seedDefaultEmployee();
        seedSampleCustomer();
    }
    
    private void seedDefaultEmployee() {
        if (employeeRepository.count() == 0) {
            Employee defaultEmployee = new Employee();
            defaultEmployee.setEmployeeId(generateEmployeeId());
            defaultEmployee.setFirstName("Default");
            defaultEmployee.setLastName("Employee");
            defaultEmployee.setEmail("employee@bank.com");
            defaultEmployee.setPasswordHash(passwordEncoder.encode("password"));
            defaultEmployee.setContactNumber("9999999999");
            defaultEmployee.setAddress("Bank Headquarters");
            defaultEmployee.setRole(Role.EMPLOYEE);
            
            employeeRepository.save(defaultEmployee);
            System.out.println("Default employee created with Employee ID: " + defaultEmployee.getEmployeeId());
            System.out.println("Login credentials - Email: employee@bank.com, Password: password");
        }
    }
    
    private void seedSampleCustomer() {
        if (customerRepository.count() == 0) {
            Customer sampleCustomer = new Customer();
            sampleCustomer.setSsnId("1234567");
            sampleCustomer.setName("John Doe");
            sampleCustomer.setEmail("john.doe@email.com");
            sampleCustomer.setAddress("123 Main Street, City");
            sampleCustomer.setContactNumber("1234567890");
            sampleCustomer.setAadharNumber("123456789012");
            sampleCustomer.setPanNumber("ABCDE1234F");
            sampleCustomer.setAccountNumber("ACC001234567890");
            sampleCustomer.setDateOfBirth(LocalDate.of(1990, 1, 15));
            sampleCustomer.setGender(Gender.MALE);
            sampleCustomer.setMaritalStatus(MaritalStatus.SINGLE);
            sampleCustomer.setPasswordHash(passwordEncoder.encode("password"));
            
            customerRepository.save(sampleCustomer);
            System.out.println("Sample customer created with SSN ID: " + sampleCustomer.getSsnId());
        }
    }
    
    private String generateEmployeeId() {
        Random random = new Random();
        String employeeId;
        do {
            employeeId = String.format("%07d", random.nextInt(10000000));
        } while (employeeRepository.findByEmployeeId(employeeId).isPresent());
        
        return employeeId;
    }
}