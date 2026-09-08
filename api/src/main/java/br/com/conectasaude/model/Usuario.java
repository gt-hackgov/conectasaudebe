package br.com.conectasaude.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Pattern(
        regexp = "\\d{11}",
        message = "CPF deve conter 11 dígitos"
    )
    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @NotBlank
    @Size(min = 3, max = 150)
    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    public Usuario() {
    }

    public Usuario(String cpf, String nome, String passwordHash, Role role) {
        this.cpf = cpf;
        this.nome = nome;
        this.passwordHash = passwordHash;
        this.role = role;
        this.ativo = true;
    }

    @PrePersist
    public void prePersist() {
        this.criadoEm = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}