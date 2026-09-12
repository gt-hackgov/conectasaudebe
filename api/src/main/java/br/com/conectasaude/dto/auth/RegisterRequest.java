package br.com.conectasaude.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank
        @Pattern(
                regexp = "\\d{11}",
                message = "CPF deve conter 11 dígitos"
        )
        String cpf,

        @NotBlank
        @Size(
                min = 3,
                max = 150,
                message = "Nome deve conter entre 3 e 150 caracteres"
        )
        String nome,

        @NotBlank
        @Size(
                min = 8,
                max = 72,
                message = "Senha deve conter entre 8 e 72 caracteres"
        )
        String senha

) {
}
