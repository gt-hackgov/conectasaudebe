# Conecta Saúde — Backend

Sistema de apoio ao agendamento e gestão de atendimentos de saúde, com foco em autenticação, autorização, agendamentos, notificações e auditoria de acesso a dados sensíveis.

## Status atual

O projeto evoluiu do protótipo inicial em Java puro para uma API REST em Spring Boot, com autenticação JWT, controle de acesso por perfil, persistência com JPA/H2 em ambiente de desenvolvimento, validações e testes de integração.

A estrutura atual inclui:
- autenticação e cadastro de usuários
- gerenciamento de agendamentos
- listagem e consulta de notificações
- auditoria de ações sensíveis
- configuração para execução local com variáveis de ambiente

## Tecnologias

- Java 21
- Spring Boot 3 / Spring Web MVC
- Spring Data JPA
- Spring Security + JWT
- Bean Validation
- H2 Database
- Maven Wrapper
- JUnit / testes de integração

## Estrutura do projeto

```text
.
├── api/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── br/com/conectasaude/
│   │   │   │       ├── config/
│   │   │   │       ├── controller/
│   │   │   │       ├── dto/
│   │   │   │       ├── exception/
│   │   │   │       ├── model/
│   │   │   │       ├── repository/
│   │   │   │       ├── security/
│   │   │   │       ├── service/
│   │   │   │       └── specification/
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   │       └── java/br/com/conectasaude/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
├── docs/
│   └── governanca-auditoria.md
├── README.md
└── .gitignore
```

## Pré-requisitos

- JDK 21 ou superior
- Maven (opcional, pois o projeto inclui `mvnw`)
- Git

## Como executar localmente

A partir da raiz do repositório:

```bash
cd api
./mvnw spring-boot:run
```

ou no Windows:

```bash
cd api
mvnw.cmd spring-boot:run
```

A aplicação será iniciada em:

```text
http://localhost:8080
```

## Variáveis de ambiente

O projeto utiliza variáveis de ambiente para autenticação e usuários de desenvolvimento. Configure, por exemplo:

```bash
export JWT_SECRET=sua-chave-secreta
export DEV_USER_CPF=00000000000
export DEV_USER_NOME="Usuário Dev"
export DEV_USER_PASSWORD=senha123

export DEV_MEDICO_CPF=11111111111
export DEV_MEDICO_NOME="Médico Dev"
export DEV_MEDICO_PASSWORD=senha123

export DEV_PACIENTE_CPF=22222222222
export DEV_PACIENTE_NOME="Paciente Dev"
export DEV_PACIENTE_PASSWORD=senha123
```

## Endpoints principais

A API expõe rotas de autenticação, agendamento e notificações:

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/appointments`
- `POST /api/appointments`
- `PUT /api/appointments/{id}`
- `DELETE /api/appointments/{id}`
- `GET /api/notifications`

## Funcionalidades implementadas

### Autenticação
- login com CPF e senha
- geração e validação de JWT
- perfis de usuário e papéis
- registro de pacientes

### Agendamentos
- criação de agendamentos
- listagem por usuário
- atualização e cancelamento
- respostas estruturadas em DTOs

### Notificações
- consulta de notificações por usuário
- integração com fluxo de acesso e ações do sistema

### Auditoria
- registro de ações sensíveis do sistema
- rastreabilidade de login, visualização e alteração de dados
- suporte para governança e auditoria

## Testes

O projeto inclui testes de integração para validar fluxos críticos:

```bash
cd api
./mvnw test
```

Os testes cobrem cenários de:
- autenticação
- agendamento
- notificações
- funcionamento geral da aplicação

## Fluxo de contribuição

O fluxo recomendado segue a estrutura:

```bash
git checkout develop
git pull
git checkout -b feat/nome-da-sua-task
```

Depois:
- abrir Pull Request para `develop`
- validar a branch
- realizar merge para `main` ao final de cada fase estável

## Governança e auditoria

A documentação de segurança, controle de acesso e trilha de auditoria está disponível em:

[Governança e Auditoria](docs/governanca-auditoria.md)

## Observações

Este repositório está em evolução contínua. A versão atual já contém uma API funcional em Spring Boot, mas ainda pode receber refinamentos em regras de negócio, persistência, cobertura de testes e documentação operacional.
