package br.com.conectasaude.controller;

import br.com.conectasaude.dto.admin.AdminUserCreateRequest;
import br.com.conectasaude.dto.admin.AdminUserResponse;
import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.Usuario;
import br.com.conectasaude.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UsuarioService usuarioService;

    public AdminUserController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<AdminUserResponse> criar(
            @Valid @RequestBody AdminUserCreateRequest request
    ) {

        Usuario usuario = usuarioService.cadastrarUsuario(
                request.cpf(),
                request.nome(),
                request.senha(),
                Role.valueOf(request.role())
        );

        AdminUserResponse response = new AdminUserResponse(
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
