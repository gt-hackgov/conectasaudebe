package br.com.conectasaude.repository;

import br.com.conectasaude.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, String> {

    List<Agendamento> findByUsuarioId(UUID usuarioId);

    Optional<Agendamento> findByIdAndUsuarioId(String id, UUID usuarioId);
}
