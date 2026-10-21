package br.com.conectasaude.dto.fila;

import br.com.conectasaude.model.ClassificacaoRisco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dados mínimos para colocar um paciente na fila.
 * Minimização de dados (LGPD): a fila guarda só as iniciais, nunca nome completo ou CPF.
 */
public record EntradaFilaRequest(

        @NotBlank(message = "Iniciais são obrigatórias")
        @Pattern(regexp = "^[A-Za-zÀ-ÿ.\\s]{1,8}$", message = "Use apenas as iniciais (ex.: M.S.)")
        String iniciais,

        @NotNull(message = "Classificação de risco é obrigatória")
        ClassificacaoRisco classificacao,

        @Size(max = 120, message = "Queixa deve ter no máximo 120 caracteres")
        String queixa
) {
}
