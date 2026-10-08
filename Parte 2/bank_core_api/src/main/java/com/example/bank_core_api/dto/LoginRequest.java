package com.example.bank_core_api.dto;


public record LoginRequest(
        String username,
        String password
) {
}
