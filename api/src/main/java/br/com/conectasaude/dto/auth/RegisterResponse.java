package br.com.conectasaude.dto.auth;

import br.com.conectasaude.model.Role;

import java.util.UUID;

public record RegisterResponse(
        UUID id,
        String nome,
        String cpf,
        Role role
) {
}
