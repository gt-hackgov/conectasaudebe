package br.com.conectasaude.service;

import br.com.conectasaude.dto.fila.EntradaFilaRequest;
import br.com.conectasaude.dto.fila.EntradaFilaResponse;
import br.com.conectasaude.estrutura.HeapDePrioridade;
import br.com.conectasaude.model.ClassificacaoRisco;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Fila de atendimento por classificação de risco.
 *
 * Regra de prioridade (Comparator):
 *   1º a cor de risco (VERMELHO antes de LARANJA, ...);
 *   2º com a mesma cor, quem chegou primeiro (senha menor) — mantém a justiça FIFO.
 *
 * Estrutura: HeapDePrioridade (árvore binária). Ver análise Big O na própria classe.
 *
 * Observação: a fila fica em memória porque representa o "agora" da unidade
 * (reinicia a cada plantão). Em produção, cada unidade teria a sua fila e um registro persistente.
 */
@Service
public class FilaAtendimentoService {

    record Entrada(
            String id,
            int senha,
            String iniciais,
            ClassificacaoRisco classificacao,
            String queixa,
            Instant chegada
    ) {
    }

    static final Comparator<Entrada> PRIORIDADE =
            Comparator.comparingInt((Entrada e) -> e.classificacao().ordinal())
                    .thenComparingInt(Entrada::senha);

    private final HeapDePrioridade<Entrada> heap = new HeapDePrioridade<>(PRIORIDADE);
    private final Clock clock;
    private int proximaSenha = 1;

    public FilaAtendimentoService() {
        this(Clock.systemDefaultZone());
    }

    FilaAtendimentoService(Clock clock) {
        this.clock = clock;
    }

    public synchronized EntradaFilaResponse inserir(EntradaFilaRequest request) {
        return inserir(request, Instant.now(clock));
    }

    /** Usado também para popular a demonstração com horários de chegada no passado. */
    public synchronized EntradaFilaResponse inserir(EntradaFilaRequest request, Instant chegada) {
        String queixa = request.queixa() == null ? "" : request.queixa().trim();
        Entrada entrada = new Entrada(
                "fila-" + UUID.randomUUID(),
                proximaSenha++,
                request.iniciais().trim().toUpperCase(),
                request.classificacao(),
                queixa,
                chegada
        );
        heap.inserir(entrada);
        int posicao = heap.emOrdem().indexOf(entrada) + 1;
        return paraResposta(entrada, posicao);
    }

    public synchronized List<EntradaFilaResponse> listarEmOrdem() {
        List<Entrada> ordenadas = heap.emOrdem();
        List<EntradaFilaResponse> resposta = new ArrayList<>(ordenadas.size());
        for (int i = 0; i < ordenadas.size(); i++) {
            resposta.add(paraResposta(ordenadas.get(i), i + 1));
        }
        return resposta;
    }

    public synchronized Optional<EntradaFilaResponse> chamarProximo() {
        if (heap.vazio()) {
            return Optional.empty();
        }
        return Optional.of(paraResposta(heap.remover(), 0));
    }

    public synchronized boolean remover(String id) {
        return heap.removerSe(e -> e.id().equals(id));
    }

    public synchronized int tamanho() {
        return heap.tamanho();
    }

    public synchronized void limpar() {
        while (!heap.vazio()) {
            heap.remover();
        }
        proximaSenha = 1;
    }

    private EntradaFilaResponse paraResposta(Entrada e, int posicao) {
        long minutos = Math.max(0, Duration.between(e.chegada(), Instant.now(clock)).toMinutes());
        int alvo = e.classificacao().getTempoAlvoMinutos();
        return new EntradaFilaResponse(
                e.id(),
                e.senha(),
                posicao,
                e.iniciais(),
                e.classificacao(),
                e.classificacao().getDescricao(),
                e.queixa(),
                e.chegada().toString(),
                minutos,
                alvo,
                minutos > alvo
        );
    }
}
