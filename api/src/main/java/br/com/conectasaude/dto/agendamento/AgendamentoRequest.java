package br.com.conectasaude.dto.agendamento;

import jakarta.validation.constraints.NotBlank;

public record AgendamentoRequest(

        @NotBlank(message = "Data é obrigatória")
        String date,

        @NotBlank(message = "Horário é obrigatório")
        String time,

        @NotBlank(message = "Local é obrigatório")
        String location,

        @NotBlank(message = "Especialidade é obrigatória")
        String specialty,

        String notes
) {
}
