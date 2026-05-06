package com.neobank.controller;

import com.neobank.dto.TransactionResponse;
import com.neobank.service.TransactionService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

        private final TransactionService transactionService;

        @PostMapping("/transfer")
        public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
                return ResponseEntity.ok(TransactionResponse.from(
                                transactionService.transfer(
                                                request.sourceAccountId(),
                                                request.destinationAccountNumber(),
                                                request.amount(),
                                                request.description())));
        }

        @PostMapping("/deposit")
        public ResponseEntity<TransactionResponse> deposit(@RequestBody DepositRequest request) {
                return ResponseEntity.ok(TransactionResponse.from(
                                transactionService.deposit(
                                                request.accountId(),
                                                request.amount(),
                                                request.description())));
        }

        @PostMapping("/withdraw")
        public ResponseEntity<TransactionResponse> withdraw(@RequestBody WithdrawRequest request) {
                return ResponseEntity.ok(TransactionResponse.from(
                                transactionService.withdraw(
                                                request.accountId(),
                                                request.amount(),
                                                request.description())));
        }

        @GetMapping("/history/{accountId}")
        public ResponseEntity<Page<TransactionResponse>> getHistory(
                        @PathVariable UUID accountId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

                return ResponseEntity.ok(
                                transactionService.getTransactionHistory(accountId, pageable)
                                                .map(TransactionResponse::from));
        }

        public record TransferRequest(
                        @NotNull(message = "Source account is required") UUID sourceAccountId,

                        @NotBlank(message = "Destination account number is required") String destinationAccountNumber,

                        @NotNull(message = "Amount is required") @DecimalMin(value = "0.01", message = "Amount must be greater than zero") BigDecimal amount,

                        String description) {
        }

        public record DepositRequest(
                        @NotNull(message = "Account is required") UUID accountId,

                        @NotNull(message = "Amount is required") @DecimalMin(value = "0.01", message = "Amount must be greater than zero") BigDecimal amount,

                        String description) {
        }

        public record WithdrawRequest(
                        @NotNull(message = "Account is required") UUID accountId,

                        @NotNull(message = "Amount is required") @DecimalMin(value = "0.01", message = "Amount must be greater than zero") BigDecimal amount,

                        String description) {
        }
}