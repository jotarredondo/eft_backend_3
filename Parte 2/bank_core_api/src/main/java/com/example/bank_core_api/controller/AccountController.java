package com.example.bank_core_api.controller;

import com.example.bank_core_api.dto.WithdrawRequest;
import com.example.bank_core_api.dto.WithdrawResponse;
import com.example.bank_core_api.model.Account;
import com.example.bank_core_api.repository.AccountRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @PostMapping("/withdraw")
    public WithdrawResponse withdraw(@RequestBody WithdrawRequest request) {

        Account account = accountRepository
                .findByAccountId(request.accountId())
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new RuntimeException("Monto inválido");
        }
        if (account.getBalance().compareTo(request.amount()) < 0) {
            throw new RuntimeException("Saldo insuficiente");
        }
        BigDecimal newBalance = account.getBalance().subtract(request.amount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        return new WithdrawResponse(
                request.accountId(),
                request.amount(),
                newBalance,
                "APROBADO");
    }
}
