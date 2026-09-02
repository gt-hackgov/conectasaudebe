# Conecta Saúde — Back-end

## Status atual
Projeto em estágio inicial: contém apenas as classes de domínio essenciais
(`Paciente`, `Agendamento`, `Main`), sem framework web, sem persistência em
banco de dados e sem API REST ainda. A implementação da API RESTful está
em andamento em outra task (SCRUM-28).

## Pré-requisitos
- JDK 21 ou superior (testado com Temurin 25)

## Como rodar localmente
```bash
cd src
javac Main.java Paciente.java Agendamento.java
java Main
```

Saída esperada:Iniciando Sistema SUS Conecta...
Status do agendamento: Vaga Reservada
