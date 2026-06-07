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

        //Exibindo valores dos atributos do objeto paciente1
        System.out.println("Dados do paciente 1:" +
                "\nNome: " + paciente1.nomeCompleto +
                "\nID: " + paciente1.id +
                "\nEmail: " + paciente1.email
        );


        //Paciente 2
        Paciente paciente2 = new Paciente();
        paciente2.id = UUID.randomUUID();
        paciente2.cpf = "111.111.111-12";
        paciente2.nomeCompleto = "Lara Croft";
        paciente2.dataNascimento = LocalDate.of(1991, 01, 01);
        paciente2.endereco = "Rua 2";
        paciente2.email = "email2@email.com";
        paciente2.telefone = "(11)91111-1112";
        System.out.println("Dados do paciente 2:" +
                "\nNome: " + paciente2.nomeCompleto +
                "\nID: " + paciente2.id +
                "\nEmail: " + paciente2.email
        );
    }
}