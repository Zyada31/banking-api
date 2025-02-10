package com.bank.api.repository;

import com.bank.api.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long>
{
    List<BankAccount> findByCustomerId(Long customerId);

    Optional<BankAccount> findByAccountNumber(String accountNumber);
}
