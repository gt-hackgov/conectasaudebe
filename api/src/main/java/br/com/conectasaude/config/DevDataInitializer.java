package br.com.conectasaude.config;

import br.com.conectasaude.model.Role;
import br.com.conectasaude.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

    private final UsuarioService usuarioService;

    @Value("${app.dev.user.cpf:}")
    private String cpf;

    @Value("${app.dev.user.nome:}")
    private String nome;

    @Value("${app.dev.user.password:}")
    private String senha;

    @Value("${app.dev.medico.cpf:}")
    private String medicoCpf;

    @Value("${app.dev.medico.nome:}")
    private String medicoNome;

    @Value("${app.dev.medico.password:}")
    private String medicoSenha;

    public DevDataInitializer(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(String... args) {

        criarAdministradorSeConfigurado();
        criarMedicoSeConfigurado();
    }

    private void criarAdministradorSeConfigurado() {

        if (cpf.isBlank() || nome.isBlank() || senha.isBlank()) {
            return;
        }

        if (usuarioService.buscarPorCpf(cpf).isPresent()) {
            return;
        }

        usuarioService.cadastrarUsuario(
                cpf,
                nome,
                senha,
                Role.ADMIN
        );

        System.out.println(
                "Usuário administrador de desenvolvimento criado com sucesso."
        );
    }

    private void criarMedicoSeConfigurado() {

        if (medicoCpf.isBlank()
                || medicoNome.isBlank()
                || medicoSenha.isBlank()) {
            return;
        }

        if (usuarioService.buscarPorCpf(medicoCpf).isPresent()) {
            return;
        }

        usuarioService.cadastrarUsuario(
                medicoCpf,
                medicoNome,
                medicoSenha,
                Role.MEDICO
        );

        System.out.println(
                "Usuário médico de desenvolvimento criado com sucesso."
        );
    }
}