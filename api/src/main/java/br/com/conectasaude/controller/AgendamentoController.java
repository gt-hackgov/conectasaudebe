package br.com.conectasaude.controller;

import br.com.conectasaude.model.Agendamento;
import br.com.conectasaude.repository.AgendamentoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AgendamentoController {

    private final AgendamentoRepository repository;

    public AgendamentoController(AgendamentoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<?> getAppointments() {
        List<Agendamento> list = repository.findAll();
        return ResponseEntity.ok(Map.of("appointments", list));
    }

    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody Agendamento agendamento) {
        if (agendamento.getDate() == null || agendamento.getTime() == null ||
            agendamento.getLocation() == null || agendamento.getSpecialty() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Os campos date, time, location e specialty são obrigatórios."));
        }

        agendamento.setId("apt-" + UUID.randomUUID().toString());
        agendamento.setCreatedAt(Instant.now().toString());

        Agendamento saved = repository.save(agendamento);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Agendamento criado com sucesso", "appointment", saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable String id) {
        Optional<Agendamento> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Consulta não encontrada."));
        }
        
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Consulta cancelada com sucesso"));
    }
}
