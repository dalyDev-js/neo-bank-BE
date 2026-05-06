package com.neobank.service;

import com.neobank.entity.Account;
import com.neobank.entity.Transaction;
import com.neobank.enums.AccountStatus;
import com.neobank.enums.TransactionStatus;
import com.neobank.enums.TransactionType;
import com.neobank.exception.AccessDeniedException;
import com.neobank.exception.BankingException;
import com.neobank.exception.ResourceNotFoundException;
import com.neobank.repository.AccountRepository;
import com.neobank.repository.TransactionRepository;
import com.neobank.util.AccountNumberGenerator;
import com.neobank.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AuditLogService auditLogService;

    @Transactional
    public Transaction transfer(UUID sourceAccountId, String destinationAccountNumber,
            BigDecimal amount, String description) {

        Account source = accountRepository.findById(sourceAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", sourceAccountId));

        if (!SecurityUtils.isOwner(source.getUser())) {
            throw new AccessDeniedException();
        }

        Account destination = accountRepository.findByAccountNumber(destinationAccountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", destinationAccountNumber));

        validateTransfer(source, destination, amount);

        Transaction transaction = Transaction.builder()
                .referenceNumber(accountNumberGenerator.generateReferenceNumber())
                .sourceAccount(source)
                .destinationAccount(destination)
                .type(TransactionType.TRANSFER)
                .amount(amount)
                .description(description)
                .build();

        transaction = transactionRepository.save(transaction);

        source.setBalance(source.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));

        transaction.setBalanceAfterSource(source.getBalance());
        transaction.setBalanceAfterDestination(destination.getBalance());
        transaction.setStatus(TransactionStatus.COMPLETED);

        accountRepository.save(source);
        accountRepository.save(destination);
        auditLogService.log(
                SecurityUtils.getCurrentUser().getId(),
                "TRANSFER",
                "Account",
                source.getId(),
                "Transferred " + amount + " " + source.getCurrency() +
                        " to " + destination.getAccountNumber());
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction deposit(UUID accountId, BigDecimal amount, String description) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        if (!SecurityUtils.isOwner(account.getUser())) {
            throw new AccessDeniedException();
        }

        validateAmount(amount);

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BankingException("Account is not active");
        }

        account.setBalance(account.getBalance().add(amount));

        Transaction transaction = Transaction.builder()
                .referenceNumber(accountNumberGenerator.generateReferenceNumber())
                .destinationAccount(account)
                .type(TransactionType.DEPOSIT)
                .amount(amount)
                .balanceAfterDestination(account.getBalance())
                .description(description)
                .status(TransactionStatus.COMPLETED)
                .build();
        auditLogService.log(
                SecurityUtils.getCurrentUser().getId(),
                "DEPOSIT",
                "Account",
                account.getId(),
                "Deposited " + amount + " " + account.getCurrency());
        accountRepository.save(account);

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction withdraw(UUID accountId, BigDecimal amount, String description) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        if (!SecurityUtils.isOwner(account.getUser())) {
            throw new AccessDeniedException();
        }

        validateAmount(amount);

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BankingException("Account is not active");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new BankingException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));

        Transaction transaction = Transaction.builder()
                .referenceNumber(accountNumberGenerator.generateReferenceNumber())
                .sourceAccount(account)
                .type(TransactionType.WITHDRAWAL)
                .amount(amount)
                .balanceAfterSource(account.getBalance())
                .description(description)
                .status(TransactionStatus.COMPLETED)
                .build();

        accountRepository.save(account);
        auditLogService.log(
                SecurityUtils.getCurrentUser().getId(),
                "WITHDRAWAL",
                "Account",
                account.getId(),
                "Withdrew " + amount + " " + account.getCurrency());

        return transactionRepository.save(transaction);
    }

    public Page<Transaction> getTransactionHistory(UUID accountId, Pageable pageable) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        if (!SecurityUtils.isOwner(account.getUser())) {
            throw new AccessDeniedException();
        }

        return transactionRepository.findByAccountId(accountId, pageable);
    }

    private void validateTransfer(Account source, Account destination, BigDecimal amount) {
        if (source.getStatus() != AccountStatus.ACTIVE) {
            throw new BankingException("Source account is not active");
        }
        if (destination.getStatus() != AccountStatus.ACTIVE) {
            throw new BankingException("Destination account is not active");
        }
        if (source.getId().equals(destination.getId())) {
            throw new BankingException("Cannot transfer to the same account");
        }
        if (source.getBalance().compareTo(amount) < 0) {
            throw new BankingException("Insufficient balance");
        }
        validateAmount(amount);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Amount must be greater than zero");
        }
    }
}