package com.example.atm_bff.dto;

import java.math.BigDecimal;

public record BackendWithdrawResponse(
        Long accountId,
        BigDecimal withdrawnAmount,
        BigDecimal remainingBalance,
        String status
) {
}
