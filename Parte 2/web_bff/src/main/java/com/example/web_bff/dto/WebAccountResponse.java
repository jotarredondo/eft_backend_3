package com.example.web_bff.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class WebAccountResponse {

    private Long id;
    private Long numeroCuenta;
    private String titular;
    private String tipoCuenta;
    private BigDecimal saldo;
}