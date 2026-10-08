package com.example.mobile_bff.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class MobileAccountResponse {

    private Long numeroCuenta;
    private String tipoCuenta;
    private BigDecimal saldo;
}