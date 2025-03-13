package com.bank.api.repository;

import com.bank.api.entity.PendingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PendingTransactionRepository extends JpaRepository<PendingTransaction, String> {
    Optional<PendingTransaction> findByTransactionId(String transactionId);
}
