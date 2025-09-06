package com.bank.management.controller;

import com.bank.management.model.Transaction;
import com.bank.management.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Transaction operations
 * 
 * This controller provides HTTP endpoints for transaction management:
 * - POST /api/transactions/customer/{customerSsnId} - Create new transaction
 * - GET /api/transactions - Get all transactions
 * - GET /api/transactions/{transactionId} - Get specific transaction
 * - GET /api/transactions/customer/{customerSsnId} - Get customer's transactions
 * - PUT /api/transactions/{transactionId} - Update transaction
 * - DELETE /api/transactions/{transactionId} - Delete transaction
 * 
 * Handles financial transactions with proper validation and customer association.
 */
@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction Management", description = "Transaction CRUD operations")
public class TransactionController {
    
    @Autowired
    private TransactionService transactionService;
    
    @PostMapping("/customer/{customerSsnId}")
    @Operation(summary = "Create a new transaction for a customer")
    public ResponseEntity<Transaction> createTransaction(
            @PathVariable String customerSsnId, 
            @RequestBody Transaction transaction) {
        Transaction createdTransaction = transactionService.createTransaction(transaction, customerSsnId);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }
    
    @GetMapping
    @Operation(summary = "Get all transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/{transactionId}")
    @Operation(summary = "Get transaction by Transaction ID")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable String transactionId) {
        Transaction transaction = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(transaction);
    }
    
    @GetMapping("/customer/{customerSsnId}")
    @Operation(summary = "Get all transactions for a specific customer")
    public ResponseEntity<List<Transaction>> getTransactionsByCustomer(@PathVariable String customerSsnId) {
        List<Transaction> transactions = transactionService.getTransactionsByCustomer(customerSsnId);
        return ResponseEntity.ok(transactions);
    }
    
    @PutMapping("/{transactionId}")
    @Operation(summary = "Update transaction")
    public ResponseEntity<Transaction> updateTransaction(
            @PathVariable String transactionId, 
            @RequestBody Transaction transaction) {
        Transaction updatedTransaction = transactionService.updateTransaction(transactionId, transaction);
        return ResponseEntity.ok(updatedTransaction);
    }
    
    @DeleteMapping("/{transactionId}")
    @Operation(summary = "Delete transaction")
    public ResponseEntity<String> deleteTransaction(@PathVariable String transactionId) {
        transactionService.deleteTransaction(transactionId);
        return ResponseEntity.ok("Transaction deleted successfully");
    }
}