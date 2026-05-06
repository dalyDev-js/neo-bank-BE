package com.neobank.controller;

import com.neobank.dto.AccountResponse;
import com.neobank.enums.AccountType;
import com.neobank.service.AccountService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = AccountResponse.from(
                accountService.createAccount(request.type()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getMyAccounts() {
        List<AccountResponse> accounts = accountService.getMyAccounts()
                .stream()
                .map(AccountResponse::from)
                .toList();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(AccountResponse.from(accountService.getAccount(id)));
    }

    @PatchMapping("/{id}/freeze")
    public ResponseEntity<AccountResponse> freezeAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(AccountResponse.from(accountService.freezeAccount(id)));
    }

    @PatchMapping("/{id}/unfreeze")
    public ResponseEntity<AccountResponse> unfreezeAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(AccountResponse.from(accountService.unfreezeAccount(id)));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<AccountResponse> closeAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(AccountResponse.from(accountService.closeAccount(id)));
    }

    public record CreateAccountRequest(
            @NotNull(message = "Account type is required") AccountType type) {
    }
}