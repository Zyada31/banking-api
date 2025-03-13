package com.bank.api.dto.rerquest;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest
{
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String transactionId; // Optional - Will be generated if missing
}
