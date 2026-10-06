package com.duoc.banco_sociedad.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "annual_statement_")
@Getter
@Setter
public class AnnualStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cuentaId;

    private LocalDate fecha;

    private String transaccion;

    private BigDecimal monto;

    private String descripcion;
}
