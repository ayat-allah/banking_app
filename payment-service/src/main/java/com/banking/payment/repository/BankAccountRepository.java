package com.banking.payment.repository;

import com.banking.payment.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
    List<BankAccount> findByUserIdAndActiveTrue(String userId);
    boolean existsByUserIdAndCardNumber(String userId, String cardNumber);
}
