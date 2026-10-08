package com.example.atm_bff.dto;

import java.math.BigDecimal;

public record AtmWithdrawResponse(
        Long numeroCuenta,
        BigDecimal montoRetirado,
        BigDecimal saldoDisponible,
        String estado
) {
}
