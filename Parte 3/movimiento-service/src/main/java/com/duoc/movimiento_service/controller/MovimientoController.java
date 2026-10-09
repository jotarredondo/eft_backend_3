package com.duoc.movimiento_service.controller;

import com.duoc.movimiento_service.model.MovimientoAnual;
import com.duoc.movimiento_service.repository.MovimientoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoRepository repository;

    public MovimientoController(MovimientoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<MovimientoAnual> getAll() {
        return repository.findAll();
    }

    @GetMapping("/cuenta/{accountId}")
    public List<MovimientoAnual> getByAccount(
            @PathVariable Long accountId) {

        return repository.findByAccountId(accountId);
    }
}
