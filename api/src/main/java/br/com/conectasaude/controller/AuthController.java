package br.com.conectasaude.controller;

import br.com.conectasaude.dto.auth.LoginRequest;
import br.com.conectasaude.dto.auth.LoginResponse;
import br.com.conectasaude.dto.auth.RegisterRequest;
import br.com.conectasaude.dto.auth.RegisterResponse;
import br.com.conectasaude.exception.CredenciaisInvalidasException;
import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.Usuario;
import br.com.conectasaude.model.audit.AuditAction;
import br.com.conectasaude.model.audit.AuditResult;
import br.com.conectasaude.service.AuditService;
import br.com.conectasaude.service.AuthenticationService;
import br.com.conectasaude.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UsuarioService usuarioService;
    private final AuditService auditService;

    public AuthController(
            AuthenticationService authenticationService,
            UsuarioService usuarioService,
            AuditService auditService
    ) {
        this.authenticationService = authenticationService;
        this.usuarioService = usuarioService;
        this.auditService = auditService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {

        try {

            LoginResponse response =
                    authenticationService.autenticar(request);

            auditService.registrar(
                    response.usuarioId(),
                    response.role(),
                    AuditAction.LOGIN,
                    "AUTENTICACAO",
                    AuditResult.SUCESSO,
                    httpRequest
            );

            return ResponseEntity.ok(response);

        } catch (CredenciaisInvalidasException exception) {

            auditService.registrar(
                    null,
                    null,
                    AuditAction.LOGIN,
                    "AUTENTICACAO",
                    AuditResult.NEGADO,
                    httpRequest
            );

            throw exception;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        Usuario usuario = usuarioService.cadastrarUsuario(
                request.cpf(),
                request.nome(),
                request.senha(),
                Role.PACIENTE
        );

        RegisterResponse response = new RegisterResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getCpf(),
                usuario.getRole()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}