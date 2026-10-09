package com.duoc.transaccion_service.producer;

import com.duoc.transaccion_service.event.TransaccionEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransaccionProducer {

    private static final String TOPIC = "transacciones-bancarias";

    private final KafkaTemplate<String, TransaccionEvent> kafkaTemplate;

    public TransaccionProducer(
            KafkaTemplate<String, TransaccionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void enviarTransaccion(TransaccionEvent event) {

        kafkaTemplate.send(
                TOPIC,
                String.valueOf(event.getCuentaId()),
                event
        ).whenComplete((result, ex) -> {

            if (ex == null) {
                System.out.println(
                        "Mensaje enviado a Kafka. Topic: "
                                + result.getRecordMetadata().topic()
                                + " | Particion: "
                                + result.getRecordMetadata().partition()
                                + " | Offset: "
                                + result.getRecordMetadata().offset()
                );
            } else {
                System.err.println(
                        "Error enviando mensaje a Kafka: "
                                + ex.getMessage()
                );
            }
        });
    }
}
