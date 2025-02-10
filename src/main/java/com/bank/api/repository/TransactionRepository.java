package com.bank.api.repository;

import com.bank.api.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long>
{
    List<Transaction> findByFromAccountOrToAccount(String fromAccount, String toAccount);
}
