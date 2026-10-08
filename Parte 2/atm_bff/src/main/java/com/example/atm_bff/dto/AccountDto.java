package com.example.atm_bff.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AccountDto {

    private Long id;
    private Long accountId;
    private String holderName;
    private String type;
    private BigDecimal balance;
}
