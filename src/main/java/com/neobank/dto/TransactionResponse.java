package com.neobank.dto;

import com.neobank.entity.Transaction;
import com.neobank.enums.TransactionStatus;
import com.neobank.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String referenceNumber,
        TransactionType type,
        TransactionStatus status,
        BigDecimal amount,
        String sourceAccountNumber,
        String destinationAccountNumber,
        BigDecimal balanceAfterSource,
        BigDecimal balanceAfterDestination,
        String description,
        LocalDateTime createdAt) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getReferenceNumber(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getAmount(),
                transaction.getSourceAccount() != null
                        ? transaction.getSourceAccount().getAccountNumber()
                        : null,
                transaction.getDestinationAccount() != null
                        ? transaction.getDestinationAccount().getAccountNumber()
                        : null,
                transaction.getBalanceAfterSource(),
                transaction.getBalanceAfterDestination(),
                transaction.getDescription(),
                transaction.getCreatedAt());
    }
}