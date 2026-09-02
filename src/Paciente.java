//Classe de domínio essencial
import java.time.LocalDate;
public class Paciente {
    private String id;
    private String cpf;
    private String nomeCompleto;
    private LocalDate dataNascimento;
    private String endereco;
    private String email;
    private String telefone;

    public Paciente(String cpf) {
        this.cpf = cpf;
    }
}