package model;

import java.time.LocalDate;
import java.util.UUID;

public class Paciente {
    public UUID id;
    public String cpf;
    public String nomeCompleto;
    public LocalDate dataNascimento;
    public String endereco;
    public String email;
    public String telefone;
}
