package br.com.conectasaude.service;

import br.com.conectasaude.exception.CpfJaCadastradoException;
import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.Usuario;
import br.com.conectasaude.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario cadastrarUsuario(
            String cpf,
            String nome,
            String senha,
            Role role
    ) {

        if (usuarioRepository.existsByCpf(cpf)) {
            throw new CpfJaCadastradoException();
        }

        String passwordHash = passwordEncoder.encode(senha);

        Usuario usuario = new Usuario(
                cpf,
                nome,
                passwordHash,
                role
        );

        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorCpf(String cpf) {
        return usuarioRepository.findByCpf(cpf);
    }

    public boolean verificarSenha(String senha, String passwordHash) {
        return passwordEncoder.matches(senha, passwordHash);
    }
}