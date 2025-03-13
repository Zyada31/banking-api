package com.bank.api.service.transaction;

import com.bank.api.dto.rerquest.TransactionRequest;
import com.bank.api.dto.response.BankResponse;
import com.bank.api.dto.response.TransactionDTO;
import com.bank.api.entity.Transaction;

import java.util.List;

public interface TransactionService
{
    BankResponse<?> transferMoney(TransactionRequest transferRequest);

    List<Transaction> getTransactionsByAccount(String accountNumber);

    Transaction processTransaction(String transactionId);
}
