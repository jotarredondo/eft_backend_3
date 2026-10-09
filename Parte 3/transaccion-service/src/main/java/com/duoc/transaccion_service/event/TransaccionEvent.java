package com.duoc.transaccion_service.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionEvent {

    private Long cuentaId;
    private Long cuentaDestinoId;
    private String tipo;
    private BigDecimal monto;
    private String descripcion;
    private LocalDateTime fecha;
}