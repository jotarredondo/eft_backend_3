package com.duoc.cuenta_service.controller;

import com.duoc.cuenta_service.client.MovimientoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final MovimientoClient movimientoClient;

    public CuentaController(MovimientoClient movimientoClient) {
        this.movimientoClient = movimientoClient;
    }

    @Value("${mensaje.prueba}")
    private String mensaje;

    @GetMapping("/prueba")
    public String prueba() {
        return mensaje;
    }

    @GetMapping("/{accountId}/movimientos")
    public Object getMovimientos(@PathVariable Long accountId) {
        return movimientoClient.getMovimientos(accountId);
    }

}
