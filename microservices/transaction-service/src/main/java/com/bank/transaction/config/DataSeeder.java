package com.bank.transaction.config;

import com.bank.transaction.model.Transaction;
import com.bank.transaction.model.TransactionType;
import com.bank.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class DataSeeder implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (transactionRepository.count() == 0) {
            seedTransactions();
        }
    }
    
    private void seedTransactions() {
        logger.info("Seeding sample transactions...");
        
        List<Transaction> transactions = Arrays.asList(
            createTransaction("123456789", "ACC001", new BigDecimal("1000.00"), TransactionType.CREDIT, "DEPOSIT", LocalDateTime.now().minusDays(5)),
            createTransaction("123456789", "ACC001", new BigDecimal("500.00"), TransactionType.DEBIT, "WITHDRAWAL", LocalDateTime.now().minusDays(4)),
            createTransaction("987654321", "ACC002", new BigDecimal("2000.00"), TransactionType.CREDIT, "TRANSFER", LocalDateTime.now().minusDays(3)),
            createTransaction("987654321", "ACC002", new BigDecimal("750.00"), TransactionType.DEBIT, "ATM_WITHDRAWAL", LocalDateTime.now().minusDays(2)),
            createTransaction("555666777", "ACC003", new BigDecimal("1500.00"), TransactionType.CREDIT, "SALARY", LocalDateTime.now().minusDays(1)),
            createTransaction("555666777", "ACC003", new BigDecimal("200.00"), TransactionType.DEBIT, "ONLINE_PAYMENT", LocalDateTime.now())
        );
        
        transactionRepository.saveAll(transactions);
        logger.info("Successfully seeded {} transactions", transactions.size());
    }
    
    private Transaction createTransaction(String customerSsnId, String accountId, BigDecimal amount, 
                                       TransactionType type, String mode, LocalDateTime date) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setCustomerSsnId(customerSsnId);
        transaction.setAccountId(accountId);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setModeOfTransaction(mode);
        transaction.setDate(date);
        return transaction;
    }
}