package br.com.conectasaude.dto.agendamento;

import br.com.conectasaude.model.Agendamento;

public record AgendamentoResponse(
        String id,
        String date,
        String time,
        String location,
        String specialty,
        String notes,
        String createdAt
) {

    public static AgendamentoResponse fromEntity(Agendamento agendamento) {
        return new AgendamentoResponse(
                agendamento.getId(),
                agendamento.getDate(),
                agendamento.getTime(),
                agendamento.getLocation(),
                agendamento.getSpecialty(),
                agendamento.getNotes(),
                agendamento.getCreatedAt()
        );
    }
}
