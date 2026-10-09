package com.duoc.cliente_service.controller;

import com.duoc.cliente_service.model.Cliente;
import com.duoc.cliente_service.repository.ClienteRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteRepository clienteRepository;

    public ClienteController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @GetMapping
    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtener(@PathVariable Long id) {
        return clienteRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Cliente crear(@RequestBody Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizar(
            @PathVariable Long id,
            @RequestBody Cliente datos) {

        return clienteRepository.findById(id)
                .map(cliente -> {
                    cliente.setNombre(datos.getNombre());
                    cliente.setApellido(datos.getApellido());
                    cliente.setEmail(datos.getEmail());
                    cliente.setTelefono(datos.getTelefono());
                    cliente.setPerfil(datos.getPerfil());

                    return ResponseEntity.ok(
                            clienteRepository.save(cliente)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }
}