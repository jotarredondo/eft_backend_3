package com.example.atm_bff.dto;

import java.math.BigDecimal;

public record AtmWithdrawRequest(
        Long accountId,
        BigDecimal amount
) {
}
