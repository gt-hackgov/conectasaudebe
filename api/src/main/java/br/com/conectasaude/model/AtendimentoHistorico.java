package br.com.conectasaude.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Histórico de atendimentos usado nos indicadores de gestão.
 *
 * IMPORTANTE: no protótipo esta tabela é preenchida com dados SINTÉTICOS
 * (AtendimentoHistoricoInitializer), gerados com semente fixa para serem reproduzíveis.
 * Não contém dados pessoais: é um registro agregável (unidade, data, espera, comparecimento).
 */
@Entity
@Table(name = "atendimentos_historico", indexes = {
        @Index(name = "idx_atend_unidade", columnList = "unidade"),
        @Index(name = "idx_atend_data", columnList = "data_atendimento")
})
public class AtendimentoHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String unidade;

    @Column(name = "tipo_unidade", nullable = false, length = 3)
    private String tipoUnidade;

    @Column(nullable = false, length = 40)
    private String especialidade;

    @Column(name = "data_atendimento", nullable = false)
    private LocalDate dataAtendimento;

    /** 1 = segunda ... 7 = domingo (ISO-8601). Guardado para facilitar a análise por dia da semana. */
    @Column(name = "dia_semana", nullable = false)
    private int diaSemana;

    @Column(name = "espera_minutos", nullable = false)
    private int esperaMinutos;

    @Column(nullable = false)
    private boolean compareceu;

    @Column(name = "classificacao_risco", length = 10)
    private String classificacaoRisco;

    protected AtendimentoHistorico() {
    }

    public AtendimentoHistorico(
            String unidade,
            String tipoUnidade,
            String especialidade,
            LocalDate dataAtendimento,
            int esperaMinutos,
            boolean compareceu,
            String classificacaoRisco
    ) {
        this.unidade = unidade;
        this.tipoUnidade = tipoUnidade;
        this.especialidade = especialidade;
        this.dataAtendimento = dataAtendimento;
        this.diaSemana = dataAtendimento.getDayOfWeek().getValue();
        this.esperaMinutos = esperaMinutos;
        this.compareceu = compareceu;
        this.classificacaoRisco = classificacaoRisco;
    }

    public Long getId() { return id; }
    public String getUnidade() { return unidade; }
    public String getTipoUnidade() { return tipoUnidade; }
    public String getEspecialidade() { return especialidade; }
    public LocalDate getDataAtendimento() { return dataAtendimento; }
    public int getDiaSemana() { return diaSemana; }
    public int getEsperaMinutos() { return esperaMinutos; }
    public boolean isCompareceu() { return compareceu; }
    public String getClassificacaoRisco() { return classificacaoRisco; }
}
