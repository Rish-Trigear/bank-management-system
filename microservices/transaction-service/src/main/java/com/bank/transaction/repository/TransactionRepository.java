package com.bank.transaction.repository;

import com.bank.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findByTransactionId(String transactionId);
    
    List<Transaction> findByCustomerSsnId(String customerSsnId);
    
    List<Transaction> findByCustomerSsnIdOrderByDateDesc(String customerSsnId);
    
    boolean existsByTransactionId(String transactionId);
    
    long countByCustomerSsnId(String customerSsnId);
}