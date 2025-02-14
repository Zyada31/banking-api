package com.bank.api.service.transaction;

import com.bank.api.entity.Transaction;
import java.util.List;

public interface TransactionService
{
    Transaction transferMoney(Transaction transferRequest);

    List<Transaction> getTransactionsByAccount(String accountNumber);
}
