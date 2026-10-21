package br.com.conectasaude.model;

/**
 * Classificação de risco inspirada no Protocolo de Manchester (cores),
 * usada no acolhimento de UPAs. A ordem do enum é a ordem de prioridade.
 * O tempo-alvo é o tempo máximo recomendado até o atendimento, em minutos.
 * Valores de referência para o protótipo — cada município define o seu protocolo.
 */
public enum ClassificacaoRisco {
    VERMELHO("Emergência", 0),
    LARANJA("Muito urgente", 10),
    AMARELO("Urgente", 60),
    VERDE("Pouco urgente", 120),
    AZUL("Não urgente", 240);

    private final String descricao;
    private final int tempoAlvoMinutos;

    ClassificacaoRisco(String descricao, int tempoAlvoMinutos) {
        this.descricao = descricao;
        this.tempoAlvoMinutos = tempoAlvoMinutos;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getTempoAlvoMinutos() {
        return tempoAlvoMinutos;
    }
}
