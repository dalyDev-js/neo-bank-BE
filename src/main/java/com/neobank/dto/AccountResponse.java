package com.neobank.dto;

import com.neobank.entity.Account;
import com.neobank.enums.AccountStatus;
import com.neobank.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String accountNumber,
        AccountType type,
        AccountStatus status,
        BigDecimal balance,
        String currency,
        BigDecimal dailyTransferLimit,
        String ownerName,
        LocalDateTime createdAt) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getType(),
                account.getStatus(),
                account.getBalance(),
                account.getCurrency(),
                account.getDailyTransferLimit(),
                account.getUser().getFirstName() + " " + account.getUser().getLastName(),
                account.getCreatedAt());
    }
}