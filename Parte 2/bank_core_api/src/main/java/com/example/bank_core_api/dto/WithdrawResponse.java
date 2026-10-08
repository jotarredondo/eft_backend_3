package com.example.bank_core_api.dto;

import java.math.BigDecimal;

public record WithdrawResponse(
        Long accountId,
        BigDecimal withdrawnAmount,
        BigDecimal remainingBalance,
        String status
) {
}
