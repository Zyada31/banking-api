package com.bank.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pending_transactions")
public class PendingTransaction
{
    @Id
    private String transactionId;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
