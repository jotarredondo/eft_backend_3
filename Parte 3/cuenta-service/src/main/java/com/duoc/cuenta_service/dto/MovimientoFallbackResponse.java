package com.duoc.cuenta_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MovimientoFallbackResponse {

    private String mensaje;
    private List<?> movimientos;
}
