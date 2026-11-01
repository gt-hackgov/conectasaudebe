package br.com.conectasaude.service;

import br.com.conectasaude.estatistica.RegressaoLinear;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Indicadores de gestão calculados DIRETAMENTE no banco com SQL.
 *
 * Cada consulta é devolvida junto com o próprio SQL e a pergunta de gestão que responde,
 * para que o painel do administrador (e a documentação) mostrem de onde vem cada número.
 *
 * Recursos de SQL usados: GROUP BY, COUNT, AVG, SUM com CASE, HAVING com SUBCONSULTA,
 * funções de janela RANK() OVER e SUM() OVER, e EXTRACT.
 *
 * Além das consultas, o serviço calcula ANÁLISES ESTATÍSTICAS (correlação de Pearson e
 * regressão linear simples) sobre séries agregadas pelo próprio SQL (ver ANALISES).
 */
@Service
public class IndicadoresService {

    public record Consulta(String id, String titulo, String pergunta, String sql, List<String> colunas) {
    }

    static final List<Consulta> CONSULTAS = List.of(
            new Consulta(
                    "volume_por_unidade",
                    "Volume, espera média e faltas por unidade",
                    "Quais unidades concentram mais atendimentos e mais faltas?",
                    """
                    SELECT unidade,
                           tipo_unidade,
                           COUNT(*) AS total,
                           ROUND(AVG(CASE WHEN compareceu THEN espera_minutos * 1.0 END), 1) AS espera_media_min,
                           SUM(CASE WHEN compareceu THEN 0 ELSE 1 END) AS faltas
                    FROM atendimentos_historico
                    GROUP BY unidade, tipo_unidade
                    ORDER BY total DESC""",
                    List.of("unidade", "tipo_unidade", "total", "espera_media_min", "faltas")
            ),
            new Consulta(
                    "absenteismo_especialidade",
                    "Taxa de faltas por especialidade (UBS)",
                    "Em quais especialidades vale reforçar lembretes de consulta?",
                    """
                    SELECT especialidade,
                           COUNT(*) AS agendados,
                           SUM(CASE WHEN compareceu THEN 0 ELSE 1 END) AS faltas,
                           ROUND(100.0 * SUM(CASE WHEN compareceu THEN 0 ELSE 1 END) / COUNT(*), 1) AS taxa_falta_pct
                    FROM atendimentos_historico
                    WHERE tipo_unidade = 'UBS'
                    GROUP BY especialidade
                    ORDER BY taxa_falta_pct DESC""",
                    List.of("especialidade", "agendados", "faltas", "taxa_falta_pct")
            ),
            new Consulta(
                    "espera_acima_media",
                    "UBS com espera acima da média da rede (subconsulta)",
                    "Quais UBS esperam mais do que a média de todas as UBS?",
                    """
                    SELECT unidade,
                           ROUND(AVG(espera_minutos * 1.0), 1) AS espera_media_min
                    FROM atendimentos_historico
                    WHERE tipo_unidade = 'UBS' AND compareceu = TRUE
                    GROUP BY unidade
                    HAVING AVG(espera_minutos * 1.0) > (
                        SELECT AVG(espera_minutos * 1.0)
                        FROM atendimentos_historico
                        WHERE tipo_unidade = 'UBS' AND compareceu = TRUE
                    )
                    ORDER BY espera_media_min DESC""",
                    List.of("unidade", "espera_media_min")
            ),
            new Consulta(
                    "ranking_dia_semana",
                    "Ranking de demanda por dia da semana (função de janela RANK)",
                    "Em quais dias da semana reforçar a escala das UPAs?",
                    """
                    SELECT dia_semana,
                           COUNT(*) AS atendimentos,
                           RANK() OVER (ORDER BY COUNT(*) DESC) AS posicao
                    FROM atendimentos_historico
                    WHERE tipo_unidade = 'UPA'
                    GROUP BY dia_semana
                    ORDER BY posicao""",
                    List.of("dia_semana", "atendimentos", "posicao")
            ),
            new Consulta(
                    "perfil_risco_upa",
                    "Perfil de risco nas UPAs (percentual com SUM() OVER)",
                    "Quantos casos da UPA poderiam ter sido atendidos na UBS?",
                    """
                    SELECT classificacao_risco,
                           COUNT(*) AS total,
                           ROUND(100.0 * COUNT(*) / SUM(COUNT(*)) OVER (), 1) AS percentual,
                           ROUND(AVG(espera_minutos * 1.0), 1) AS espera_media_min
                    FROM atendimentos_historico
                    WHERE tipo_unidade = 'UPA'
                    GROUP BY classificacao_risco
                    ORDER BY total DESC""",
                    List.of("classificacao_risco", "total", "percentual", "espera_media_min")
            ),
            new Consulta(
                    "tendencia_mensal",
                    "Tendência mensal de atendimentos",
                    "A demanda está crescendo ou diminuindo?",
                    """
                    SELECT EXTRACT(YEAR FROM data_atendimento) AS ano,
                           EXTRACT(MONTH FROM data_atendimento) AS mes,
                           COUNT(*) AS atendimentos,
                           SUM(CASE WHEN tipo_unidade = 'UPA' THEN 1 ELSE 0 END) AS upa,
                           SUM(CASE WHEN tipo_unidade = 'UBS' THEN 1 ELSE 0 END) AS ubs
                    FROM atendimentos_historico
                    GROUP BY EXTRACT(YEAR FROM data_atendimento), EXTRACT(MONTH FROM data_atendimento)
                    ORDER BY ano, mes""",
                    List.of("ano", "mes", "atendimentos", "upa", "ubs")
            )
    );


    /**
     * Análise de correlação e regressão. O SQL devolve pares (x, y), já agregados no banco;
     * a reta é calculada em Java pela classe RegressaoLinear.
     *
     * @param xEhData       true quando x é uma data (vira "dias desde o início do período")
     * @param xPrevisao     valor de x usado no exemplo de previsão (null = sem previsão)
     * @param xPrevisaoRelativo true quando xPrevisao é contado a partir do último x observado
     */
    public record Analise(String id, String titulo, String pergunta, String sql,
                          String rotuloX, String rotuloY, String unidadeX, String unidadeY,
                          boolean xEhData, Double xPrevisao, boolean xPrevisaoRelativo,
                          String acaoSeRelevante, String acaoSeFraca) {
    }

    static final List<Analise> ANALISES = List.of(
            new Analise(
                    "upa_volume_espera",
                    "UPA: demanda do dia × espera média",
                    "Dias mais cheios na UPA aumentam a espera? Quanto?",
                    """
                    SELECT COUNT(*) AS x,
                           AVG(espera_minutos * 1.0) AS y
                    FROM atendimentos_historico
                    WHERE tipo_unidade = 'UPA'
                    GROUP BY unidade, data_atendimento""",
                    "Atendimentos no dia (por UPA)", "Espera média (min)", "atendimento", "min",
                    false, 45.0, false,
                    "Use a reta para prever a espera a partir da demanda esperada e reforçar a escala antes de a espera passar da meta.",
                    "A demanda do dia explica pouco a espera; investigue outros fatores, como o perfil de risco e o número de profissionais."
            ),
            new Analise(
                    "ubs_volume_espera",
                    "UBS: agendamentos do dia × espera média",
                    "Agendas mais cheias na UBS aumentam a espera de quem comparece?",
                    """
                    SELECT COUNT(*) AS x,
                           AVG(CASE WHEN compareceu THEN espera_minutos * 1.0 END) AS y
                    FROM atendimentos_historico
                    WHERE tipo_unidade = 'UBS'
                    GROUP BY unidade, data_atendimento
                    HAVING SUM(CASE WHEN compareceu THEN 1 ELSE 0 END) > 0""",
                    "Agendamentos no dia (por UBS)", "Espera média (min)", "agendamento", "min",
                    false, 25.0, false,
                    "Limite o número de agendamentos por dia nas unidades sobrecarregadas ou redistribua a agenda para unidades vizinhas.",
                    "O volume de agendamentos explica pouco a espera na UBS; o tamanho da agenda não é o gargalo principal."
            ),
            new Analise(
                    "ubs_espera_faltas",
                    "UBS: espera média × taxa de faltas",
                    "Quando a espera aumenta, as pessoas faltam mais?",
                    """
                    SELECT AVG(CASE WHEN compareceu THEN espera_minutos * 1.0 END) AS x,
                           100.0 * SUM(CASE WHEN compareceu THEN 0 ELSE 1 END) / COUNT(*) AS y
                    FROM atendimentos_historico
                    WHERE tipo_unidade = 'UBS'
                    GROUP BY unidade, data_atendimento
                    HAVING SUM(CASE WHEN compareceu THEN 1 ELSE 0 END) > 0""",
                    "Espera média do dia (min)", "Faltas no dia (%)", "minuto", "p.p.",
                    false, null, false,
                    "Reduzir a espera pode diminuir as faltas: acompanhe os dois indicadores juntos.",
                    "As faltas não acompanham a espera. Concentre os lembretes nas especialidades com mais faltas (Saúde Mental e Ginecologia)."
            ),
            new Analise(
                    "upa_tendencia_demanda",
                    "UPA: tendência da demanda diária (regressão no tempo)",
                    "A procura pela UPA está crescendo? Quanto esperar nos próximos 30 dias?",
                    """
                    SELECT data_atendimento AS x,
                           COUNT(*) AS y
                    FROM atendimentos_historico
                    WHERE tipo_unidade = 'UPA'
                    GROUP BY data_atendimento
                    ORDER BY data_atendimento""",
                    "Dias desde o início do período", "Atendimentos no dia (2 UPAs)", "dia", "atendimento",
                    true, 30.0, true,
                    "A demanda tem tendência clara: ajuste a escala e os insumos para o volume previsto.",
                    "Não há tendência de crescimento: a variação vem do dia da semana. Planeje a escala pelo ranking semanal e pela média diária."
            )
    );

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public Map<String, Object> calcular() {
        List<Map<String, Object>> resultados = new ArrayList<>();
        for (Consulta consulta : CONSULTAS) {
            resultados.add(executar(consulta));
        }

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("fonteDados", "Histórico sintético gerado para o protótipo (semente fixa). Não são dados oficiais.");
        resposta.put("resumo", resumo());
        resposta.put("consultas", resultados);
        resposta.put("analises", analisesEstatisticas());
        return resposta;
    }

    /** Executa as análises estatísticas (correlação e regressão) sobre as séries agregadas no banco. */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> analisesEstatisticas() {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Analise analise : ANALISES) {
            lista.add(analisar(analise));
        }
        return lista;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> analisar(Analise analise) {
        List<Object[]> brutas = entityManager.createNativeQuery(analise.sql()).getResultList();
        int n = brutas.size();
        double[] x = new double[n];
        double[] y = new double[n];
        Long primeiroDia = null;
        for (int i = 0; i < n; i++) {
            Object[] linha = brutas.get(i);
            if (analise.xEhData()) {
                long dia = diaEpoch(linha[0]);
                if (primeiroDia == null || dia < primeiroDia) {
                    primeiroDia = dia;
                }
                x[i] = dia;
            } else {
                x[i] = numero(linha[0]).doubleValue();
            }
            y[i] = numero(linha[1]).doubleValue();
        }
        if (analise.xEhData() && primeiroDia != null) {
            for (int i = 0; i < n; i++) {
                x[i] -= primeiroDia; // dia 0 = primeiro dia do período
            }
        }

        RegressaoLinear reg = RegressaoLinear.ajustar(x, y);
        boolean relevante = Math.abs(reg.r()) >= 0.3;

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("id", analise.id());
        resultado.put("titulo", analise.titulo());
        resultado.put("pergunta", analise.pergunta());
        resultado.put("sql", analise.sql());
        resultado.put("rotuloX", analise.rotuloX());
        resultado.put("rotuloY", analise.rotuloY());
        resultado.put("n", reg.n());
        resultado.put("r", arredondar(reg.r(), 3));
        resultado.put("r2", arredondar(reg.r2(), 3));
        resultado.put("inclinacao", arredondar(reg.inclinacao(), 4));
        resultado.put("intercepto", arredondar(reg.intercepto(), 3));
        resultado.put("forca", reg.forca());
        resultado.put("sentido", reg.sentido());
        resultado.put("interpretacao", interpretar(analise, reg));
        resultado.put("acaoSugerida", relevante ? analise.acaoSeRelevante() : analise.acaoSeFraca());

        if (analise.xPrevisao() != null && n > 1) {
            double maxX = 0;
            for (double v : x) {
                maxX = Math.max(maxX, v);
            }
            double xp = analise.xPrevisaoRelativo() ? maxX + analise.xPrevisao() : analise.xPrevisao();
            Map<String, Object> previsao = new LinkedHashMap<>();
            previsao.put("x", arredondar(xp, 1));
            previsao.put("y", arredondar(reg.prever(xp), 1));
            resultado.put("previsao", previsao);
        }

        List<Map<String, Object>> pontos = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            pontos.add(Map.of("x", arredondar(x[i], 1), "y", arredondar(y[i], 1)));
        }
        resultado.put("pontos", pontos);
        return resultado;
    }

    private static String interpretar(Analise analise, RegressaoLinear reg) {
        Locale br = Locale.forLanguageTag("pt-BR");
        String texto = String.format(br, "Correlação %s %s (r = %.2f), com %d observações. ",
                reg.forca(), reg.sentido().equals("nula") ? "e praticamente nula" : reg.sentido(), reg.r(), reg.n());
        if (Math.abs(reg.r()) >= 0.3) {
            texto += String.format(br, "Em média, cada %s a mais está associado a %.2f %s %s. ",
                    analise.unidadeX(), Math.abs(reg.inclinacao()), analise.unidadeY(),
                    reg.inclinacao() >= 0 ? "a mais" : "a menos");
        }
        texto += String.format(br, "A reta explica %.0f%% da variação (R² = %.2f). Correlação não prova causa.",
                reg.r2() * 100, reg.r2());
        return texto;
    }

    private static long diaEpoch(Object valor) {
        if (valor instanceof LocalDate data) {
            return data.toEpochDay();
        }
        if (valor instanceof java.sql.Date data) {
            return data.toLocalDate().toEpochDay();
        }
        if (valor instanceof java.util.Date data) {
            return new java.sql.Date(data.getTime()).toLocalDate().toEpochDay();
        }
        return LocalDate.parse(String.valueOf(valor).substring(0, 10)).toEpochDay();
    }

    private static double arredondar(double valor, int casas) {
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            return 0;
        }
        return BigDecimal.valueOf(valor).setScale(casas, RoundingMode.HALF_UP).doubleValue();
    }

    private Map<String, Object> resumo() {
        Object[] linha = (Object[]) entityManager.createNativeQuery("""
                SELECT COUNT(*),
                       SUM(CASE WHEN compareceu THEN 0 ELSE 1 END),
                       ROUND(AVG(CASE WHEN tipo_unidade = 'UPA' THEN espera_minutos * 1.0 END), 1),
                       SUM(CASE WHEN classificacao_risco IN ('VERDE', 'AZUL') THEN 1 ELSE 0 END),
                       SUM(CASE WHEN tipo_unidade = 'UPA' THEN 1 ELSE 0 END),
                       MIN(data_atendimento),
                       MAX(data_atendimento),
                       SUM(CASE WHEN tipo_unidade = 'UBS' THEN 1 ELSE 0 END)
                FROM atendimentos_historico""").getSingleResult();

        long total = numero(linha[0]).longValue();
        long faltas = numero(linha[1]).longValue();
        long baixoRiscoUpa = numero(linha[3]).longValue();
        long totalUpa = numero(linha[4]).longValue();
        long totalUbs = numero(linha[7]).longValue();

        Map<String, Object> resumo = new LinkedHashMap<>();
        resumo.put("totalAtendimentos", total);
        resumo.put("taxaFaltaUbsPct", percentual(faltas, totalUbs)); // faltas só existem em atendimento agendado (UBS)
        resumo.put("esperaMediaUpaMin", normalizar(linha[2]));
        resumo.put("baixoRiscoNaUpaPct", percentual(baixoRiscoUpa, totalUpa));
        resumo.put("periodoInicio", linha[5] == null ? null : linha[5].toString());
        resumo.put("periodoFim", linha[6] == null ? null : linha[6].toString());
        return resumo;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> executar(Consulta consulta) {
        List<Object> brutas = entityManager.createNativeQuery(consulta.sql()).getResultList();
        List<Map<String, Object>> linhas = new ArrayList<>();
        for (Object bruta : brutas) {
            Object[] valores = bruta instanceof Object[] array ? array : new Object[]{bruta};
            Map<String, Object> linha = new LinkedHashMap<>();
            for (int i = 0; i < consulta.colunas().size() && i < valores.length; i++) {
                linha.put(consulta.colunas().get(i), normalizar(valores[i]));
            }
            linhas.add(linha);
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("id", consulta.id());
        resultado.put("titulo", consulta.titulo());
        resultado.put("pergunta", consulta.pergunta());
        resultado.put("sql", consulta.sql());
        resultado.put("colunas", consulta.colunas());
        resultado.put("linhas", linhas);
        return resultado;
    }

    /** Converte tipos do banco (BigDecimal, Long, Integer...) em números simples para o JSON. */
    private static Object normalizar(Object valor) {
        if (valor instanceof BigDecimal decimal) {
            if (decimal.scale() <= 0) {
                return decimal.longValue();
            }
            return decimal.setScale(1, RoundingMode.HALF_UP).doubleValue();
        }
        if (valor instanceof Number || valor instanceof String || valor instanceof Boolean || valor == null) {
            return valor;
        }
        return valor.toString();
    }

    private static Number numero(Object valor) {
        return valor instanceof Number n ? n : 0;
    }

    private static double percentual(long parte, long todo) {
        if (todo == 0) {
            return 0;
        }
        return BigDecimal.valueOf(100.0 * parte / todo).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
