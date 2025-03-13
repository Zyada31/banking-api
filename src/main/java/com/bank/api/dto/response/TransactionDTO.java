package com.bank.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bank.api.entity.Transaction;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionDTO
{
    private Long id;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String failureReason;
    private String status;
    private LocalDateTime timestamp;
    private String transactionId; // Optional - Will be generated if missing

    public TransactionDTO(Transaction transaction)
    {
        this.id = transaction.getId();
        this.fromAccount = transaction.getFromAccount();
        this.toAccount = transaction.getToAccount();
        this.amount = transaction.getAmount();
        this.failureReason = transaction.getFailureReason();
        this.status = transaction.getStatus().name();
        this.timestamp = transaction.getTimestamp();
        this.transactionId = transaction.getTransactionId();
    }
}
