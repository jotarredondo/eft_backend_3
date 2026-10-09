package com.duoc.movimiento_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "movimiento_anual")
public class MovimientoAnual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cuenta_id")
    private Long accountId;

    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipo;

    private BigDecimal monto;

    private String descripcion;
}
