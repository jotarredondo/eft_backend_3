package com.duoc.cuenta_service.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import com.duoc.cuenta_service.dto.MovimientoFallbackResponse;
import java.util.Arrays;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Component
public class MovimientoClient {

    private final RestTemplate restTemplate;

    public MovimientoClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retry(name = "movimientoService")
    @CircuitBreaker(
            name = "movimientoService",
            fallbackMethod = "movimientoFallback"
    )
    public Object getMovimientos(Long accountId) {

        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String token = authentication.getToken().getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Object[]> response = restTemplate.exchange(
                "http://MOVIMIENTO-SERVICE/api/movimientos/cuenta/" + accountId,
                HttpMethod.GET,
                request,
                Object[].class
        );

        Object[] movimientos = response.getBody();

        return movimientos != null
                ? Arrays.asList(movimientos)
                : List.of();
    }

    public Object movimientoFallback(Long accountId, Throwable ex) {

        return new MovimientoFallbackResponse(
                "Movimiento Service no disponible",
                List.of()
        );
    }
}