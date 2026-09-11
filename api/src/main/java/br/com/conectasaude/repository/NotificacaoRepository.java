package br.com.conectasaude.repository;

import br.com.conectasaude.model.Notificacao;
import br.com.conectasaude.model.TipoNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, String> {

    List<Notificacao> findByTipoOrUsuarioId(
            TipoNotificacao tipo,
            UUID usuarioId
    );
}
