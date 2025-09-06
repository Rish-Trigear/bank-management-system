package com.bank.management.service;

import com.bank.management.model.Customer;
import com.bank.management.model.Transaction;
import com.bank.management.repository.CustomerRepository;
import com.bank.management.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * Service layer for Transaction business logic
 * 
 * This service handles all transaction-related operations including:
 * - Transaction creation with customer association and timestamp
 * - Auto-generation of unique transaction IDs (TXN + 10 digits)
 * - CRUD operations for transaction management
 * - Customer transaction history retrieval
 * - Transaction processing and updates
 * 
 * Integrates with TransactionRepository and CustomerRepository for data access.
 * Automatically sets transaction timestamp and validates customer existence.
 */
@Service
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    /**
     * Create a new financial transaction for a customer
     * Validates customer exists, generates transaction ID, and sets timestamp
     */
    public Transaction createTransaction(Transaction transaction, String customerSsnId) {
        Customer customer = customerRepository.findBySsnId(customerSsnId)
                .orElseThrow(() -> new RuntimeException("Customer not found with SSN: " + customerSsnId));
        
        transaction.setCustomer(customer);
        transaction.setDate(LocalDateTime.now()); // Set current timestamp
        
        // Generate transaction ID if not provided
        if (transaction.getTransactionId() == null || transaction.getTransactionId().isEmpty()) {
            transaction.setTransactionId(generateTransactionId());
        }
        
        return transactionRepository.save(transaction);
    }
    
    public Transaction getTransactionById(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));
    }
    
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    public List<Transaction> getTransactionsByCustomer(String customerSsnId) {
        return transactionRepository.findAllByCustomer_SsnId(customerSsnId);
    }
    
    public Transaction updateTransaction(String transactionId, Transaction transactionDetails) {
        Transaction transaction = getTransactionById(transactionId);
        
        transaction.setAccountId(transactionDetails.getAccountId());
        transaction.setModeOfTransaction(transactionDetails.getModeOfTransaction());
        transaction.setAmount(transactionDetails.getAmount());
        transaction.setType(transactionDetails.getType());
        
        return transactionRepository.save(transaction);
    }
    
    public void deleteTransaction(String transactionId) {
        Transaction transaction = getTransactionById(transactionId);
        transactionRepository.delete(transaction);
    }
    
    /**
     * Generate a unique transaction ID with format "TXN" + 10 digits
     * Ensures uniqueness by checking against existing transaction IDs
     */
    private String generateTransactionId() {
        Random random = new Random();
        String transactionId;
        do {
            transactionId = "TXN" + String.format("%010d", random.nextInt(1000000000));
        } while (transactionRepository.findByTransactionId(transactionId).isPresent());
        
        return transactionId;
    }
}