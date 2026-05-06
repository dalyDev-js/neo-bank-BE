package com.neobank.service;

import com.neobank.entity.Account;
import com.neobank.enums.AccountStatus;
import com.neobank.enums.AccountType;
import com.neobank.exception.AccessDeniedException;
import com.neobank.exception.BankingException;
import com.neobank.exception.ResourceNotFoundException;
import com.neobank.repository.AccountRepository;
import com.neobank.util.AccountNumberGenerator;
import com.neobank.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    @Transactional
    public Account createAccount(AccountType type) {

        if (accountRepository.countByUserId(SecurityUtils.getCurrentUser().getId()) >= 3) {
            throw new BankingException("Maximum of 3 accounts allowed per user");
        }

        String accountNumber;
        do {
            accountNumber = accountNumberGenerator.generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .user(SecurityUtils.getCurrentUser())
                .type(type)
                .build();

        return accountRepository.save(account);
    }

    public List<Account> getMyAccounts() {
        return accountRepository.findByUserId(SecurityUtils.getCurrentUser().getId());
    }

    public Account getAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        if (!SecurityUtils.isOwner(account.getUser())) {
            throw new AccessDeniedException();
        }

        return account;
    }

    @Transactional
    public Account freezeAccount(UUID accountId) {
        Account account = getAccount(accountId);

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BankingException("Cannot freeze a closed account");
        }

        account.setStatus(AccountStatus.FROZEN);
        return accountRepository.save(account);
    }

    @Transactional
    public Account unfreezeAccount(UUID accountId) {
        Account account = getAccount(accountId);
        account.setStatus(AccountStatus.ACTIVE);
        return accountRepository.save(account);
    }

    @Transactional
    public Account closeAccount(UUID accountId) {
        Account account = getAccount(accountId);

        if (account.getBalance().compareTo(java.math.BigDecimal.ZERO) > 0) {
            throw new BankingException("Cannot close account with remaining balance");
        }

        account.setStatus(AccountStatus.CLOSED);
        return accountRepository.save(account);
    }
}