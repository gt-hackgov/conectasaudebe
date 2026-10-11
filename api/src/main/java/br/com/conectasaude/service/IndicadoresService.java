package br.com.conectasaude.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Indicadores de gestão calculados DIRETAMENTE no banco com SQL.
 *
 * Cada consulta é devolvida junto com o próprio SQL e a pergunta de gestão que responde,
 * para que o painel do administrador (e a documentação) mostrem de onde vem cada número.
 *
 * Recursos de SQL usados: GROUP BY, COUNT, AVG, SUM com CASE, HAVING com SUBCONSULTA,
 * funções de janela RANK() OVER e SUM() OVER, e EXTRACT.
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
        return resposta;
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
