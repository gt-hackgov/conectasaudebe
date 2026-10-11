package br.com.conectasaude.config;

import br.com.conectasaude.dto.fila.EntradaFilaRequest;
import br.com.conectasaude.model.ClassificacaoRisco;
import br.com.conectasaude.service.FilaAtendimentoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Popula a fila com pacientes FICTÍCIOS (só iniciais) para a demonstração.
 * Roda apenas no perfil "dev".
 */
@Component
@Profile("dev")
public class FilaDemoInitializer implements CommandLineRunner {

    private final FilaAtendimentoService filaService;

    public FilaDemoInitializer(FilaAtendimentoService filaService) {
        this.filaService = filaService;
    }

    @Override
    public void run(String... args) {
        if (filaService.tamanho() > 0) {
            return;
        }
        Instant agora = Instant.now();
        adicionar("M.S.", ClassificacaoRisco.VERDE, "Dor de garganta há 3 dias", agora.minus(Duration.ofMinutes(95)));
        adicionar("J.P.", ClassificacaoRisco.AMARELO, "Febre 39,2 °C e vômitos", agora.minus(Duration.ofMinutes(70)));
        adicionar("A.C.", ClassificacaoRisco.AZUL, "Renovação de atestado", agora.minus(Duration.ofMinutes(60)));
        adicionar("R.L.", ClassificacaoRisco.VERDE, "Torção leve no tornozelo", agora.minus(Duration.ofMinutes(40)));
        adicionar("F.O.", ClassificacaoRisco.LARANJA, "Crise de asma, saturação 92%", agora.minus(Duration.ofMinutes(8)));
        adicionar("T.M.", ClassificacaoRisco.AMARELO, "Corte na mão com sangramento controlado", agora.minus(Duration.ofMinutes(25)));
    }

    private void adicionar(String iniciais, ClassificacaoRisco risco, String queixa, Instant chegada) {
        filaService.inserir(new EntradaFilaRequest(iniciais, risco, queixa), chegada);
    }
}
