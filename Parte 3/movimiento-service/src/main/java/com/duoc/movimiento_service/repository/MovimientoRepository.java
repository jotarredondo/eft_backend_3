package com.duoc.movimiento_service.repository;

import com.duoc.movimiento_service.model.MovimientoAnual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoRepository
        extends JpaRepository<MovimientoAnual, Long> {

    List<MovimientoAnual> findByAccountId(Long accountId);
}
