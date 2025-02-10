package com.bank.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class BankResponse<T>
{
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public BankResponse(boolean success, String message, T data)
    {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
}
