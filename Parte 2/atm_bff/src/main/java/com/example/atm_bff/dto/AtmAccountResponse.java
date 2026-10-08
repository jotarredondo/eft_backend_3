package com.example.atm_bff.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AtmAccountResponse {

    private Long numeroCuenta;
    private BigDecimal saldoDisponible;
}