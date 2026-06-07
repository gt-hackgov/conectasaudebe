import model.Paciente;

import java.time.LocalDate;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        System.out.println(":::::Iniciando sistema:::::");

        //Criando um paciente (objeto do tipo Paciente)
        Paciente paciente1 = new Paciente("Ava Daniels");

        //Atribuindo valores ao objeto paciente1
        paciente1.id = UUID.randomUUID();
        paciente1.cpf = "111.111.111-11";
        paciente1.nomeCompleto = "Ava Daniels";
        paciente1.dataNascimento = LocalDate.of(1990, 01, 01);
        paciente1.endereco = "Rua 1";
        paciente1.email = "email@email.com";
        paciente1.telefone = "(11)91111-1111";


        //Exibindo valores dos atributos do objeto
        System.out.println("Dados do paciente:" +
                "\nNome: " + paciente1.nomeCompleto +
                "\nID: " + paciente1.id +
                "\nEmail: " + paciente1.email
        );
    }
}