package com.example.atm_bff.client;

import com.example.atm_bff.dto.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;


@Component
public class AccountClient {

    private final RestClient restClient;

    public AccountClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8180")
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

    public AtmWithdrawResponse withdraw(AtmWithdrawRequest request) {

        BackendWithdrawResponse response = restClient
                .post()
                .uri("/api/accounts/withdraw")
                .body(new BackendWithdrawRequest(
                        request.accountId(),
                        request.amount()
                ))
                .retrieve()
                .body(BackendWithdrawResponse.class);

        assert response != null;
        return new AtmWithdrawResponse(
                response.accountId(),
                response.withdrawnAmount(),
                response.remainingBalance(),
                response.status()
        );
    }
}

