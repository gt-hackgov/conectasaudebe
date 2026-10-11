package br.com.conectasaude.config;

import br.com.conectasaude.model.AtendimentoHistorico;
import br.com.conectasaude.model.ClassificacaoRisco;
import br.com.conectasaude.repository.AtendimentoHistoricoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Gera um histórico SINTÉTICO de atendimentos (últimos N dias) para alimentar os indicadores.
 *
 * - Semente fixa (Random(2026)): os números são sempre os mesmos, o que permite documentar
 *   os resultados das consultas SQL e repetir a demonstração.
 * - As proporções são HIPÓTESES de modelagem para o protótipo, não estatísticas oficiais.
 * - UBS: atendimento agendado em dias úteis, com faltas (absenteísmo).
 * - UPA: porta aberta 24h, todos os dias, com classificação de risco por cor.
 */
@Component
public class AtendimentoHistoricoInitializer implements CommandLineRunner {

    static final String[][] UBS = {
            {"UBS Jardim Esperança", "1.00"},
            {"UBS Vila Nova", "0.80"},
            {"UBS Parque das Flores", "1.35"},   // unidade sobrecarregada (hipótese)
            {"UBS Cidade Líder", "0.90"},
    };
    static final String[] UPAS = {"UPA 24h Jardim Esperança", "UPA 24h Cidade Líder"};

    /** especialidade -> probabilidade de falta (hipótese) */
    static final Map<String, Double> FALTA_POR_ESPECIALIDADE = Map.of(
            "Clínica Geral", 0.16,
            "Pediatria", 0.12,
            "Ginecologia", 0.22,
            "Saúde Mental", 0.30,
            "Enfermagem/Vacinação", 0.08
    );

    private final AtendimentoHistoricoRepository repository;
    private final int dias;

    public AtendimentoHistoricoInitializer(
            AtendimentoHistoricoRepository repository,
            @Value("${app.indicadores.dias-historico:90}") int dias
    ) {
        this.repository = repository;
        this.dias = dias;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0 || dias <= 0) {
            return;
        }
        repository.saveAll(gerar(LocalDate.now().minusDays(1), dias, new Random(2026)));
    }

    static List<AtendimentoHistorico> gerar(LocalDate ultimoDia, int dias, Random random) {
        List<AtendimentoHistorico> registros = new ArrayList<>();
        String[] especialidades = FALTA_POR_ESPECIALIDADE.keySet().stream().sorted().toArray(String[]::new);
        ClassificacaoRisco[] cores = ClassificacaoRisco.values();
        double[] pesoCor = {0.02, 0.08, 0.30, 0.45, 0.15}; // VERMELHO..AZUL (hipótese)
        int[] esperaCor = {2, 12, 55, 110, 170};           // espera média por cor, em minutos

        for (int d = dias - 1; d >= 0; d--) {
            LocalDate data = ultimoDia.minusDays(d);
            DayOfWeek dia = data.getDayOfWeek();
            boolean fimDeSemana = dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY;
            double fatorDia = dia == DayOfWeek.MONDAY ? 1.30 : (fimDeSemana ? 1.15 : 1.0);

            if (!fimDeSemana) {
                for (String[] ubs : UBS) {
                    double carga = Double.parseDouble(ubs[1]);
                    int qtd = (int) Math.round((12 + random.nextInt(7)) * carga * (dia == DayOfWeek.MONDAY ? 1.2 : 1.0));
                    for (int i = 0; i < qtd; i++) {
                        String esp = especialidades[random.nextInt(especialidades.length)];
                        boolean compareceu = random.nextDouble() >= FALTA_POR_ESPECIALIDADE.get(esp);
                        int espera = compareceu ? positivo(25 * carga + random.nextGaussian() * 12) : 0;
                        registros.add(new AtendimentoHistorico(ubs[0], "UBS", esp, data, espera, compareceu, null));
                    }
                }
            }

            for (String upa : UPAS) {
                int qtd = (int) Math.round((26 + random.nextInt(12)) * fatorDia);
                for (int i = 0; i < qtd; i++) {
                    int cor = sortearCor(pesoCor, random);
                    int espera = positivo(esperaCor[cor] * fatorDia + random.nextGaussian() * esperaCor[cor] * 0.35);
                    registros.add(new AtendimentoHistorico(upa, "UPA", "Pronto atendimento", data, espera, true, cores[cor].name()));
                }
            }
        }
        return registros;
    }

    private static int sortearCor(double[] pesos, Random random) {
        double r = random.nextDouble();
        double acumulado = 0;
        for (int i = 0; i < pesos.length; i++) {
            acumulado += pesos[i];
            if (r < acumulado) {
                return i;
            }
        }
        return pesos.length - 1;
    }

    private static int positivo(double valor) {
        return (int) Math.max(0, Math.round(valor));
    }
}
