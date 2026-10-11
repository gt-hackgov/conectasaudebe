package br.com.conectasaude.repository;

import br.com.conectasaude.model.AtendimentoHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtendimentoHistoricoRepository extends JpaRepository<AtendimentoHistorico, Long> {
}
