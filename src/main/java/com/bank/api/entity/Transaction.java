package com.bank.api.entity;

import com.bank.api.dto.Status;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transaction")
public class Transaction
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String transactionId;

    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String failureReason;
    private Status status;
    private LocalDateTime timestamp;

    @Version
    private Long version;  // 🔹 Optimistic Locking Version

    public Transaction(String fromAccount, String toAccount, BigDecimal amount, Status status,
                       String failureReason, String transactionId)
    {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.status = status;
        this.failureReason = failureReason;
        this.timestamp = LocalDateTime.now();
        this.transactionId = transactionId;
    }
}
