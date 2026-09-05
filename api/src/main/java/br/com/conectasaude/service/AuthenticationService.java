package br.com.conectasaude.service;

import br.com.conectasaude.dto.auth.LoginRequest;
import br.com.conectasaude.dto.auth.LoginResponse;
import br.com.conectasaude.model.Usuario;
import br.com.conectasaude.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse autenticar(LoginRequest request) {

        Usuario usuario = usuarioRepository
                .findByCpf(request.cpf())
                .orElseThrow(() ->
                        new IllegalArgumentException("CPF ou senha inválidos")
                );

        if (!usuario.isAtivo()) {
            throw new IllegalArgumentException("Usuário inativo");
        }

        if (!passwordEncoder.matches(
                request.senha(),
                usuario.getPasswordHash()
        )) {
            throw new IllegalArgumentException("CPF ou senha inválidos");
        }

        String token = jwtService.gerarToken(usuario);

        return new LoginResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getRole(),
                token
        );
    }
}