package br.com.conectasaude.service;

import br.com.conectasaude.dto.agendamento.AgendamentoRequest;
import br.com.conectasaude.dto.agendamento.AgendamentoResponse;
import br.com.conectasaude.model.Agendamento;
import br.com.conectasaude.model.Usuario;
import br.com.conectasaude.repository.AgendamentoRepository;
import br.com.conectasaude.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AgendamentoService {

    private static final DateTimeFormatter FORMATO_HORARIO =
            DateTimeFormatter.ofPattern("HH:mm");

    private final AgendamentoRepository agendamentoRepository;
    private final UsuarioRepository usuarioRepository;

    public AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.agendamentoRepository = agendamentoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<AgendamentoResponse> listarPorUsuario(UUID usuarioId) {
        return agendamentoRepository.findByUsuarioId(usuarioId).stream()
                .map(AgendamentoResponse::fromEntity)
                .toList();
    }

    public Optional<Agendamento> buscarPorId(String id) {
        return agendamentoRepository.findById(id);
    }

    public AgendamentoResponse criar(
            AgendamentoRequest request,
            UUID usuarioId
    ) {
        validarDataHora(request.date(), request.time());

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Usuário não encontrado")
                );

        Agendamento agendamento = new Agendamento();
        agendamento.setId("apt-" + UUID.randomUUID().toString());
        agendamento.setDate(request.date());
        agendamento.setTime(request.time());
        agendamento.setLocation(request.location());
        agendamento.setSpecialty(request.specialty());
        agendamento.setNotes(request.notes());
        agendamento.setCreatedAt(Instant.now().toString());
        agendamento.setUsuario(usuario);

        Agendamento saved = agendamentoRepository.save(agendamento);
        return AgendamentoResponse.fromEntity(saved);
    }

    public Optional<AgendamentoResponse> atualizar(
            String id,
            AgendamentoRequest request,
            UUID usuarioId
    ) {
        Optional<Agendamento> existente =
                agendamentoRepository.findByIdAndUsuarioId(id, usuarioId);

        if (existente.isEmpty()) {
            return Optional.empty();
        }

        validarDataHora(request.date(), request.time());

        Agendamento agendamento = existente.get();
        agendamento.setDate(request.date());
        agendamento.setTime(request.time());
        agendamento.setLocation(request.location());
        agendamento.setSpecialty(request.specialty());
        agendamento.setNotes(request.notes());

        Agendamento saved = agendamentoRepository.save(agendamento);
        return Optional.of(AgendamentoResponse.fromEntity(saved));
    }

    public boolean excluir(String id, UUID usuarioId) {
        Optional<Agendamento> existente =
                agendamentoRepository.findByIdAndUsuarioId(id, usuarioId);

        if (existente.isEmpty()) {
            return false;
        }

        agendamentoRepository.delete(existente.get());
        return true;
    }

    private void validarDataHora(String date, String time) {
        LocalDate data;
        try {
            data = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Data do agendamento inválida.");
        }

        LocalTime horario;
        try {
            horario = LocalTime.parse(time, FORMATO_HORARIO);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "Horário do agendamento inválido."
            );
        }

        LocalDateTime momentoAgendamento = LocalDateTime.of(data, horario);

        if (momentoAgendamento.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Não é possível agendar para uma data ou horário anterior ao atual."
            );
        }
    }
}
