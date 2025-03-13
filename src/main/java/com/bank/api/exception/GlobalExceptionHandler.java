package com.bank.api.exception;

import com.bank.api.dto.response.BankResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler(OtpRequiredException.class)
    public ResponseEntity<BankResponse<Map<String, String>>> handleOtpRequiredException(OtpRequiredException ex)
    {
        return ResponseEntity.status(HttpStatus.ACCEPTED) // Use 202 status code for OTP-required transactions
                .body(new BankResponse<>(true, ex.getMessage(),
                        Map.of("transactionId", ex.getTransactionId())));
    }

    @ExceptionHandler(TransactionExceptions.class)
    public ResponseEntity<BankResponse<String>> handleTransactionExceptions(TransactionExceptions ex)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new BankResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<BankResponse<String>> handleNotFound(ResourceNotFoundException ex)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new BankResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<BankResponse<String>> handleInsufficientFunds(InsufficientFundsException ex)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BankResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<BankResponse<String>> handleBadRequest(BadRequestException ex)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BankResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BankResponse<String>> handleRuntimeException(RuntimeException ex)
    {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BankResponse<>(false, "Internal server error: " + ex.getMessage(), null));
    }

}
