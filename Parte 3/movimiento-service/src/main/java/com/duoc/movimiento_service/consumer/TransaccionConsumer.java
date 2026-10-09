package com.duoc.movimiento_service.consumer;


import com.duoc.movimiento_service.event.TransaccionEvent;
import com.duoc.movimiento_service.model.MovimientoAnual;
import com.duoc.movimiento_service.model.TipoMovimiento;
import com.duoc.movimiento_service.repository.MovimientoRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TransaccionConsumer {

    private final MovimientoRepository repository;

    public TransaccionConsumer(MovimientoRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(
            topics = "transacciones-bancarias",
            groupId = "movimiento-group")
    public void consumir(TransaccionEvent event) {

        System.out.println(
                "Evento recibido desde Kafka -> "
                        + "Cuenta: " + event.getCuentaId()
                        + " | Cuenta destino: " + event.getCuentaDestinoId()
                        + " | Tipo: " + event.getTipo()
                        + " | Monto: " + event.getMonto()
                        + " | Descripcion: " + event.getDescripcion()
                        + " | Fecha: " + event.getFecha());

        MovimientoAnual movimiento = new MovimientoAnual();

        movimiento.setAccountId(event.getCuentaId());
        movimiento.setFecha(event.getFecha().toLocalDate());
        movimiento.setTipo(
                TipoMovimiento.valueOf(event.getTipo().toUpperCase()));
        movimiento.setMonto(event.getMonto());
        movimiento.setDescripcion(event.getDescripcion());
        repository.save(movimiento);
        System.out.println(
                "Movimiento persistido en MySQL con id: "
                        + movimiento.getId());
    }
}
