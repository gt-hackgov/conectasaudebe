package br.com.conectasaude.dto.fila;

import br.com.conectasaude.model.ClassificacaoRisco;

public record EntradaFilaResponse(
        String id,
        int senha,
        int posicao,
        String iniciais,
        ClassificacaoRisco classificacao,
        String descricaoRisco,
        String queixa,
        String chegada,
        long minutosEsperando,
        int tempoAlvoMinutos,
        boolean acimaDoTempoAlvo
) {
}
