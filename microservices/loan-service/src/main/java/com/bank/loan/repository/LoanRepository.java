package com.bank.loan.repository;

import com.bank.loan.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    Optional<Loan> findByLoanId(String loanId);
    
    List<Loan> findByCustomerSsnId(String customerSsnId);
    
    boolean existsByLoanId(String loanId);
    
    @Query("SELECT COUNT(l) FROM Loan l")
    long countAllLoans();
}