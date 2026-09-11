package br.com.conectasaude.config;

import br.com.conectasaude.model.Notificacao;
import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.TipoNotificacao;
import br.com.conectasaude.repository.AgendamentoRepository;
import br.com.conectasaude.repository.NotificacaoRepository;
import br.com.conectasaude.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

    private final UsuarioService usuarioService;
    private final NotificacaoRepository notificacaoRepository;
    private final AgendamentoRepository agendamentoRepository;

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

    @Value("${app.dev.paciente.cpf:}")
    private String pacienteCpf;

    @Value("${app.dev.paciente.nome:}")
    private String pacienteNome;

    @Value("${app.dev.paciente.password:}")
    private String pacienteSenha;

    public DevDataInitializer(
            UsuarioService usuarioService,
            NotificacaoRepository notificacaoRepository,
            AgendamentoRepository agendamentoRepository
    ) {
        this.usuarioService = usuarioService;
        this.notificacaoRepository = notificacaoRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    @Override
    public void run(String... args) {
        criarAdministradorSeConfigurado();
        criarMedicoSeConfigurado();
        criarPacienteSeConfigurado();
        seedMockData();
    }

    private void seedMockData() {
        if (notificacaoRepository.count() == 0) {
            notificacaoRepository.save(new Notificacao(
                    "notif-1",
                    "Previna-se da Dengue!",
                    "Elimine focos de água parada. Converse com nossa assistente virtual para saber os sintomas e cuidados.",
                    "Agora",
                    TipoNotificacao.GLOBAL
            ));
            notificacaoRepository.save(new Notificacao(
                    "notif-3",
                    "Campanha de Vacinação",
                    "Vacinação contra Influenza disponível em todas as UBS do município.",
                    "Ontem",
                    TipoNotificacao.GLOBAL
            ));
        }
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

    private void criarPacienteSeConfigurado() {

        if (pacienteCpf.isBlank()
                || pacienteNome.isBlank()
                || pacienteSenha.isBlank()) {
            return;
        }

        if (usuarioService.buscarPorCpf(pacienteCpf).isPresent()) {
            return;
        }

        usuarioService.cadastrarUsuario(
                pacienteCpf,
                pacienteNome,
                pacienteSenha,
                Role.PACIENTE
        );

        System.out.println(
                "Usuário paciente de desenvolvimento criado com sucesso."
        );
    }
}
