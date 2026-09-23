package br.com.conectasaude.dto.admin;

import br.com.conectasaude.model.Role;

import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String nome,
        String cpf,
        Role role
) {
}
