package br.com.conectasaude.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthMeController {

    @GetMapping("/me")
    public Map<String, Object> me(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Map.of(
                "usuarioId", jwt.getSubject(),
                "nome", jwt.getClaimAsString("nome"),
                "role", jwt.getClaimAsString("role")
        );
    }
}