package br.com.conectasaude.dto.notificacao;

import br.com.conectasaude.model.Notificacao;

public record NotificacaoResponse(
        String id,
        String title,
        String message,
        String time
) {

    public static NotificacaoResponse fromEntity(Notificacao notificacao) {
        return new NotificacaoResponse(
                notificacao.getId(),
                notificacao.getTitle(),
                notificacao.getMessage(),
                notificacao.getTime()
        );
    }
}
