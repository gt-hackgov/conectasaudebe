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

    @Override
    public void run(String... args) {
        criarAdministradorSeConfigurado();
        criarMedicoSeConfigurado();
        seedMockData();
    }

    private final br.com.conectasaude.repository.NotificacaoRepository notificacaoRepository;
    private final br.com.conectasaude.repository.AgendamentoRepository agendamentoRepository;

    public DevDataInitializer(
        UsuarioService usuarioService,
        br.com.conectasaude.repository.NotificacaoRepository notificacaoRepository,
        br.com.conectasaude.repository.AgendamentoRepository agendamentoRepository
    ) {
        this.usuarioService = usuarioService;
        this.notificacaoRepository = notificacaoRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    private void seedMockData() {
        if (notificacaoRepository.count() == 0) {
            notificacaoRepository.save(new br.com.conectasaude.model.Notificacao("notif-1", "Previna-se da Dengue!", "Elimine focos de água parada. Converse com nossa assistente virtual para saber os sintomas e cuidados.", "Agora"));
            notificacaoRepository.save(new br.com.conectasaude.model.Notificacao("notif-2", "Lembrete de Consulta", "Sua consulta agendada está próxima. Chegue com 15 minutos de antecedência na UBS.", "Há 1 hora"));
            notificacaoRepository.save(new br.com.conectasaude.model.Notificacao("notif-3", "Campanha de Vacinação", "Vacinação contra Influenza disponível em todas as UBS do município.", "Ontem"));
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
}