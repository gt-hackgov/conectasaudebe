Conecta Saúde — Back-end (API)
API RESTful do Conecta Saúde, projeto do HackGov (FIAP · Sistemas de Informação) desenvolvido no Enterprise Challenge 2026 com a EGESP. Projeto 2º lugar no FIAP NEXT 2026.
O Conecta Saúde conecta cidadão, profissional de saúde e gestor da atenção básica do SUS (recorte: Itaquera, São Paulo). Este repositório contém o back-end: autenticação, regras de negócio, fila de atendimento, indicadores de gestão, auditoria e acesso ao banco de dados.
O front-end (Next.js) fica em gt-hackgov/conecta-saude.
> Projeto acadêmico. Os dados de atendimento usados nos indicadores são **sintéticos** (sem dados pessoais) e o banco é **em memória**.
---
✨ O que a API faz
Recurso	O que oferece
Autenticação	Cadastro de paciente, login com CPF e senha, emissão de token JWT (60 min)
Agendamentos	CRUD de consultas; cada paciente só vê e altera os próprios registros
Notificações	Lista de avisos do paciente
Chatbot	Respostas da FAQ oficial guardada no banco; perguntas sem resposta ficam registradas
Fila por risco	Fila da UPA ordenada por cor de risco (Protocolo de Manchester) e ordem de chegada, com heap binário (inserir e chamar em O(log n)); guarda só as iniciais do paciente
Indicadores	6 consultas SQL avançadas (GROUP BY, subconsulta em HAVING, `RANK() OVER`, `SUM() OVER`) sobre ~10 mil atendimentos sintéticos
Administração	Cadastro de usuários e definição de perfil pelo administrador
Auditoria	Trilha de ações sensíveis e de acessos negados, com consulta paginada e filtros
A triagem com IA ("O que você tem?") e a chamada à IA do chatbot rodam no front-end (rotas de servidor do Next.js), para que a chave da IA nunca chegue ao navegador. O chatbot do front consulta primeiro a FAQ deste back-end.
---
🧱 Tecnologias
Item	Tecnologia
Linguagem	Java 21
Framework	Spring Boot 4 (Web MVC, Validation)
Segurança	Spring Security + OAuth2 Resource Server (JWT HS256), senhas com BCrypt
Persistência	Spring Data JPA / Hibernate
Banco	H2 em memória
Build	Maven Wrapper (`mvnw`)
Testes	JUnit 5, Spring Boot Test, MockMvc
CI	GitHub Actions (build e testes a cada Pull Request)
Todas as tecnologias são gratuitas e de código aberto.
---
🗂️ Estrutura
```text
.
├── api/                          → projeto Spring Boot
│   ├── src/main/java/br/com/conectasaude/
│   │   ├── config/               → segurança, JWT e dados iniciais (usuários, FAQ, fila, histórico)
│   │   ├── controller/           → rotas da API
│   │   ├── dto/                  → objetos de entrada e saída
│   │   ├── estrutura/            → HeapDePrioridade (fila por risco)
│   │   ├── exception/            → tratamento padronizado de erros
│   │   ├── model/                → entidades (Usuario, Agendamento, Notificacao, AtendimentoHistorico...)
│   │   ├── repository/           → acesso ao banco (JPA)
│   │   ├── security/             → respostas 401/403 e auditoria de acesso negado
│   │   ├── service/              → regras de negócio
│   │   └── specification/        → filtros da consulta de auditoria
│   ├── src/test/java/...         → testes de unidade e integração
│   ├── pom.xml
│   └── mvnw / mvnw.cmd
├── docs/governanca-auditoria.md  → perfis, permissões e trilha de auditoria
└── .github/workflows/            → integração contínua
```
---
🚀 Como rodar localmente
Pré-requisitos
JDK 21 ou superior (`java -version` para conferir)
Não é preciso instalar o Maven: o projeto usa o Maven Wrapper (`mvnw`)
Passo a passo
```bash
git clone https://github.com/gt-hackgov/conectasaudebe.git
cd conectasaudebe/api
```
Defina as variáveis de ambiente e suba o servidor.
Windows (PowerShell):
```powershell
$env:JWT_SECRET="MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
$env:SPRING_PROFILES_ACTIVE="dev"
$env:DEV_USER_CPF="11111111111";     $env:DEV_USER_NAME="Admin";   $env:DEV_USER_PASSWORD="Admin@123"
$env:DEV_MEDICO_CPF="22222222222";   $env:DEV_MEDICO_NAME="Joao";  $env:DEV_MEDICO_PASSWORD="Medico@123"
$env:DEV_PACIENTE_CPF="33333333333"; $env:DEV_PACIENTE_NAME="Maria"; $env:DEV_PACIENTE_PASSWORD="Paciente@123"
.\mvnw.cmd spring-boot:run
```
Linux ou macOS: use `export NOME=valor` para cada variável e depois `./mvnw spring-boot:run`.
A API sobe em http://localhost:8080. Para testar, abra http://localhost:8080/api/status no navegador.
> O `JWT_SECRET` acima serve **só para desenvolvimento local** (precisa ter pelo menos 256 bits). Nunca use esse valor em produção nem coloque segredos no GitHub.
Variáveis de ambiente
Variável	Obrigatória	Para que serve
`JWT_SECRET`	Sim	Chave que assina os tokens JWT (mínimo 256 bits)
`SPRING_PROFILES_ACTIVE=dev`	Recomendada	Cria os usuários de teste e a fila de demonstração
`DEV_USER_*`, `DEV_MEDICO_*`, `DEV_PACIENTE_*`	No perfil `dev`	CPF, nome e senha dos usuários de teste
Usuários de teste (perfil `dev`)
Perfil	CPF	Senha
Administrador	111.111.111-11	Admin@123
Médico	222.222.222-22	Medico@123
Paciente	333.333.333-33	Paciente@123
O banco é em memória: ao reiniciar o back-end, os dados voltam ao estado inicial.
Rodar os testes
```bash
cd api
./mvnw test          # Windows: .\mvnw.cmd test
```
---
🔌 Endpoints
Método e rota	Função	Perfil
`GET /api/status`	Verifica se a API está no ar	Público
`POST /api/auth/register`	Cadastro de paciente	Público
`POST /api/auth/login`	Login e emissão do JWT	Público
`GET /api/auth/me`	Dados do usuário logado	Autenticado
`GET` `POST /api/appointments` · `PUT` `DELETE /api/appointments/{id}`	Agendamentos do próprio paciente	PACIENTE
`GET /api/notifications`	Notificações	PACIENTE
`POST /api/chatbot/message`	Pergunta à FAQ oficial	Autenticado
`GET` `POST /api/medico/fila` · `POST /api/medico/fila/chamar` · `DELETE /api/medico/fila/{id}`	Fila por classificação de risco	MEDICO
`GET /api/admin/indicadores`	Indicadores de gestão (SQL)	ADMIN
`POST /api/admin/users`	Cadastro de usuários e perfis	ADMIN
`GET /api/auditoria/logs`	Trilha de auditoria (paginada, com filtros)	ADMIN, AUDITOR
Rotas protegidas exigem o cabeçalho `Authorization: Bearer <token>`. Sem token, a API responde 401; com perfil sem permissão, 403 (e o acesso negado é registrado na auditoria).
---
🔐 Segurança e governança
Senhas guardadas com BCrypt; nunca em texto puro.
JWT assinado, com expiração de 60 minutos; a API não guarda sessão.
Controle de acesso por perfil (RBAC): PACIENTE, MEDICO, ADMIN e AUDITOR. Quem audita não cadastra usuários (segregação de funções).
Isolamento de dados: o paciente só acessa os próprios agendamentos.
Minimização (LGPD): a fila guarda só iniciais; os dados de indicadores não têm informação pessoal.
Trilha de auditoria de ações sensíveis e acessos negados.
Detalhes em docs/governanca-auditoria.md.
---
🔄 Como contribuir
Nunca altere direto a `main` nem a `develop`.
```bash
git checkout develop
git pull
git checkout -b feature/nome-da-sua-task
# ... faça as alterações e rode os testes ...
git add .
git commit -m "feat: descrição curta"
git push -u origin feature/nome-da-sua-task
```
Ao enviar a branch, o GitHub Actions abre um Pull Request para a `develop`, compila e roda os testes. O merge depende da aprovação de outro integrante (CODEOWNERS). Ao fim de cada fase, a `develop` é levada para a `main`.
---
👥 Equipe
Integrante	RM
Bruna Boschi Santos	565704
Davi Grabalos	564926
Gabriela de Carvalho Gonçalves	562561
Lívia Scoralick	563840
Michael Marotto	562903
Tutor: Guilherme Gonçalves Guimarães · FIAP · 2026