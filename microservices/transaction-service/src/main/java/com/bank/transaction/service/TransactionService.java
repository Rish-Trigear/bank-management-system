package com.bank.transaction.service;

import com.bank.transaction.model.Transaction;
import com.bank.transaction.model.TransactionType;
import com.bank.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private CustomerValidationService customerValidationService;
    
    public Transaction createTransaction(Transaction transaction, String customerSsnId) {
        logger.info("Creating transaction for customer: {}", customerSsnId);
        
        // Validate customer exists
        if (!customerValidationService.customerExists(customerSsnId)) {
            logger.error("Customer not found: {}", customerSsnId);
            throw new RuntimeException("Customer not found with SSN ID: " + customerSsnId);
        }
        
        // Set customer SSN ID
        transaction.setCustomerSsnId(customerSsnId);
        
        // Generate unique transaction ID if not provided
        if (transaction.getTransactionId() == null) {
            transaction.setTransactionId(UUID.randomUUID().toString());
        }
        
        // Set current date if not provided
        if (transaction.getDate() == null) {
            transaction.setDate(LocalDateTime.now());
        }
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        logger.info("Transaction created successfully: {}", savedTransaction.getTransactionId());
        
        return savedTransaction;
    }
    
    public List<Transaction> getAllTransactions() {
        logger.info("Fetching all transactions");
        return transactionRepository.findAll();
    }
    
    public Transaction getTransactionById(String transactionId) {
        logger.info("Fetching transaction: {}", transactionId);
        return transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> {
                    logger.error("Transaction not found: {}", transactionId);
                    return new RuntimeException("Transaction not found with ID: " + transactionId);
                });
    }
    
    public List<Transaction> getTransactionsByCustomer(String customerSsnId) {
        logger.info("Fetching transactions for customer: {}", customerSsnId);
        
        // Validate customer exists
        if (!customerValidationService.customerExists(customerSsnId)) {
            logger.error("Customer not found: {}", customerSsnId);
            throw new RuntimeException("Customer not found with SSN ID: " + customerSsnId);
        }
        
        return transactionRepository.findByCustomerSsnIdOrderByDateDesc(customerSsnId);
    }
    
    public Transaction updateTransaction(String transactionId, Transaction transactionDetails) {
        logger.info("Updating transaction: {}", transactionId);
        
        Transaction existingTransaction = getTransactionById(transactionId);
        
        // Update fields (preserve customer SSN ID and transaction ID)
        if (transactionDetails.getAccountId() != null) {
            existingTransaction.setAccountId(transactionDetails.getAccountId());
        }
        if (transactionDetails.getModeOfTransaction() != null) {
            existingTransaction.setModeOfTransaction(transactionDetails.getModeOfTransaction());
        }
        if (transactionDetails.getAmount() != null) {
            existingTransaction.setAmount(transactionDetails.getAmount());
        }
        if (transactionDetails.getType() != null) {
            existingTransaction.setType(transactionDetails.getType());
        }
        
        Transaction updatedTransaction = transactionRepository.save(existingTransaction);
        logger.info("Transaction updated successfully: {}", transactionId);
        
        return updatedTransaction;
    }
    
    public void deleteTransaction(String transactionId) {
        logger.info("Deleting transaction: {}", transactionId);
        
        Transaction transaction = getTransactionById(transactionId);
        transactionRepository.delete(transaction);
        
        logger.info("Transaction deleted successfully: {}", transactionId);
    }
    
    public long getTransactionCount() {
        return transactionRepository.count();
    }
    
    public double getTotalBankBalance() {
        logger.info("Calculating total bank balance");
        List<Transaction> allTransactions = transactionRepository.findAll();
        
        BigDecimal totalBalance = BigDecimal.ZERO;
        
        for (Transaction transaction : allTransactions) {
            if (transaction.getType() == TransactionType.CREDIT) {
                totalBalance = totalBalance.add(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.DEBIT) {
                totalBalance = totalBalance.subtract(transaction.getAmount());
            }
        }
        
        logger.info("Total bank balance calculated: {}", totalBalance);
        return totalBalance.doubleValue();
    }
}