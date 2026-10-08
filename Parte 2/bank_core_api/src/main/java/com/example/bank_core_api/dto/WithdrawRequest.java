package com.example.bank_core_api.dto;


import java.math.BigDecimal;

public record WithdrawRequest(
        Long accountId,
        BigDecimal amount
) {
}
