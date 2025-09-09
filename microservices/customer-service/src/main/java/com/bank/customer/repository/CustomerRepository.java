package com.bank.customer.repository;

import com.bank.customer.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findBySsnId(String ssnId);
    
    boolean existsBySsnId(String ssnId);
    
    boolean existsByAccountNumber(String accountNumber);
}