package com.example.mobile_bff.controller;


import com.example.mobile_bff.client.AccountClient;
import com.example.mobile_bff.dto.MobileAccountResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/mobile/accounts")
public class MobileAccountController {

    private final AccountClient accountClient;

    public MobileAccountController(AccountClient accountClient) {
        this.accountClient = accountClient;
    }

    @GetMapping
    public List<MobileAccountResponse> getAccounts() {
        return accountClient.getAccounts()
                .stream()
                .map(account -> new MobileAccountResponse(
                        account.getAccountId(),
                        account.getType(),
                        account.getBalance()
                ))
                .toList();
    }
}
