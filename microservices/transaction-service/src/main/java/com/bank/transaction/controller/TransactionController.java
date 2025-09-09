package com.bank.transaction.controller;

import com.bank.transaction.model.Transaction;
import com.bank.transaction.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    
    @Autowired
    private TransactionService transactionService;
    
    @PostMapping("/customer/{customerSsnId}")
    public ResponseEntity<Transaction> createTransaction(
            @PathVariable String customerSsnId, 
            @RequestBody Transaction transaction) {
        try {
            logger.info("Creating transaction for customer: {}", customerSsnId);
            Transaction createdTransaction = transactionService.createTransaction(transaction, customerSsnId);
            return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating transaction for customer {}: {}", customerSsnId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        try {
            logger.info("Fetching all transactions");
            List<Transaction> transactions = transactionService.getAllTransactions();
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Error fetching all transactions: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable String transactionId) {
        try {
            logger.info("Fetching transaction: {}", transactionId);
            Transaction transaction = transactionService.getTransactionById(transactionId);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            logger.error("Error fetching transaction {}: {}", transactionId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/customer/{customerSsnId}")
    public ResponseEntity<List<Transaction>> getTransactionsByCustomer(@PathVariable String customerSsnId) {
        try {
            logger.info("Fetching transactions for customer: {}", customerSsnId);
            List<Transaction> transactions = transactionService.getTransactionsByCustomer(customerSsnId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Error fetching transactions for customer {}: {}", customerSsnId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    @PutMapping("/{transactionId}")
    public ResponseEntity<Transaction> updateTransaction(
            @PathVariable String transactionId,
            @RequestBody Transaction transactionDetails) {
        try {
            logger.info("Updating transaction: {}", transactionId);
            Transaction updatedTransaction = transactionService.updateTransaction(transactionId, transactionDetails);
            return ResponseEntity.ok(updatedTransaction);
        } catch (Exception e) {
            logger.error("Error updating transaction {}: {}", transactionId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String transactionId) {
        try {
            logger.info("Deleting transaction: {}", transactionId);
            transactionService.deleteTransaction(transactionId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting transaction {}: {}", transactionId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getTransactionCount() {
        try {
            logger.info("Fetching transaction count");
            long count = transactionService.getTransactionCount();
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            logger.error("Error fetching transaction count: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/total-balance")
    public ResponseEntity<Double> getTotalBankBalance() {
        try {
            logger.info("Fetching total bank balance");
            double totalBalance = transactionService.getTotalBankBalance();
            return ResponseEntity.ok(totalBalance);
        } catch (Exception e) {
            logger.error("Error fetching total bank balance: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}