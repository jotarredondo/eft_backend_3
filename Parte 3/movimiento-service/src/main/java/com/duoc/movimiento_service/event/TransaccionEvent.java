package com.duoc.movimiento_service.event;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransaccionEvent {

    private Long cuentaId;
    private Long cuentaDestinoId;
    private String tipo;
    private BigDecimal monto;
    private String descripcion;
    private LocalDateTime fecha;
}
