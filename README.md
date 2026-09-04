# Conecta Saúde — Back-end

## Status atual
Projeto em desenvolvimento incremental. Na branch `main`, contém as classes de
domínio essenciais (`Paciente`, `Agendamento`, `Main`), sem framework web, sem
persistência em banco de dados e sem API REST ainda.

A implementação da API RESTful (Spring Boot) está em andamento nas branches
`develop` / `feature/*`.

## Pré-requisitos
- JDK 21 ou superior (testado com Temurin 25)

## Como rodar localmente (versão atual da main)
```bash
cd src
javac Main.java Paciente.java Agendamento.java
java Main
```

Saída esperada:
Iniciando Sistema SUS Conecta...
Status do agendamento: Vaga Reservada


## Próximos passos
- Implementação da API RESTful com Spring Boot
- Estruturas de dados avançadas
- Persistência em banco de dados

## Fluxo de contribuição

Branches novas a partir de `develop` → Pull Request para `develop` → validação →
merge de `develop` para `main` ao final de cada fase.

```bash
git checkout develop
git pull
git checkout -b feat/nome-da-sua-task
```
