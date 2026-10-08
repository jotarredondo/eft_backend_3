package com.example.web_bff.client;


import com.example.web_bff.dto.AccountDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;


@Component
public class AccountClient {

    private final RestClient restClient;

    public AccountClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8080")
                .build();
    }

    public List<AccountDto> getAccounts() {
        AccountDto[] accounts = restClient.get()
                .uri("/api/accounts")
                .retrieve()
                .body(AccountDto[].class);

        return accounts != null
                ? Arrays.asList(accounts)
                : List.of();
    }
}

