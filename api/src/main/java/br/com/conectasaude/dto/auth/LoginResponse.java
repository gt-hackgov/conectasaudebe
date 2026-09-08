package br.com.conectasaude.dto.auth;

import br.com.conectasaude.model.Role;

import java.util.UUID;

public record LoginResponse(
        UUID usuarioId,
        String nome,
        Role role,
        String token
) {
}