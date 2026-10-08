package com.example.atm_bff.dto;

import java.math.BigDecimal;

public record BackendWithdrawRequest(
        Long accountId,
        BigDecimal amount
) {}
