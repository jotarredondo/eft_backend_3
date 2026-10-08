package com.example.web_bff.controller;

import com.example.web_bff.client.AccountClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.example.web_bff.dto.WebAccountResponse;


@RestController
@RequestMapping("/web/accounts")
public class WebAccountController {

    private final AccountClient accountClient;

    public WebAccountController(AccountClient accountClient) {
        this.accountClient = accountClient;
    }

    @GetMapping
    public List<WebAccountResponse> getAccounts() {
        return accountClient.getAccounts()
                .stream()
                .map(account -> new WebAccountResponse(
                        account.getId(),
                        account.getAccountId(),
                        account.getHolderName(),
                        account.getType(),
                        account.getBalance()
                ))
                .toList();
    }
}
