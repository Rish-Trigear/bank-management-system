package com.bank.management.repository;

import com.bank.management.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Transaction entity
 * 
 * This repository provides CRUD operations for financial transactions.
 * Custom methods include:
 * - Finding transactions by unique transaction ID
 * - Retrieving all transactions for a specific customer (by SSN ID)
 * 
 * Supports transaction history queries and individual transaction lookups.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    /**
     * Find a transaction by its unique transaction ID
     * Used for transaction verification and details lookup
     */
    Optional<Transaction> findByTransactionId(String transactionId);
    
    /**
     * Find all transactions for a specific customer by their SSN ID
     * Used for generating customer transaction history
     */
    List<Transaction> findAllByCustomer_SsnId(String ssnId);
}