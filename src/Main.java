import java.time.LocalDateTime;
public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando Sistema SUS Conecta...");
        // Simulação de cenário
        Agendamento novo = new Agendamento();
        boolean sucesso = novo.validarAgendamento(new Paciente("123.456.789-00"), LocalDateTime.now().plusDays(2));
        System.out.println("Status do agendamento: " + (sucesso ? "Vaga Reservada" : "Falha na Data"));
    }
}
