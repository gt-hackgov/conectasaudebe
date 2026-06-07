import model.Paciente;

import java.util.Scanner;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        System.out.println(":::::Iniciando sistema:::::");

        Paciente paciente = new Paciente();

        Scanner scanner = new Scanner(System.in);
        int opcao;

        do {
            System.out.println("\nMenu: \n1. Cadastrar Paciente \n2. Exibir Paciente \n3. Sair");
            opcao = scanner.nextInt();

            switch (opcao) {
                case 1:
                    System.out.println("Digite o nome do paciente:");
                    String nome = scanner.next() + scanner.nextLine();

                    System.out.println("Digite o email:");
                    String email = scanner.next() + scanner.nextLine();

                    System.out.println("Digite o cpf:");
                    String cpf = scanner.nextLine();


                    paciente.nomeCompleto = nome;
                    paciente.email = email;
                    paciente.cpf = cpf;
                    paciente.id = UUID.randomUUID();
                    break;
                case 2:
                    System.out.println("Dados do paciente:" +
                            "\nNome: " + paciente.nomeCompleto +
                            "\nEmail: " + paciente.email +
                            "\nID: " + paciente.id +
                            "\nCPF: " + paciente.cpf);
                    break;
                case 3:
                    System.out.println("\n:::::Finalizando o sistema:::::");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        } while (opcao != 3);
        scanner.close();
    }
}