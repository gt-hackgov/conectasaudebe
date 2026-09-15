package br.com.conectasaude.service;

import br.com.conectasaude.dto.notificacao.NotificacaoResponse;
import br.com.conectasaude.model.TipoNotificacao;
import br.com.conectasaude.repository.NotificacaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    public List<NotificacaoResponse> listarParaUsuario(UUID usuarioId) {
        return notificacaoRepository
                .findByTipoOrUsuarioId(TipoNotificacao.GLOBAL, usuarioId)
                .stream()
                .map(NotificacaoResponse::fromEntity)
                .toList();
    }
}
