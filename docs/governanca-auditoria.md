# ARQUIVO: docs/governanca-auditoria.md

# Governança, Segurança e Auditoria

## 1. Perfis de acesso

O Conecta Saúde utiliza controle de acesso baseado em papéis (RBAC).


| Perfil   | Permissões principais                                     |
| -------- | --------------------------------------------------------- |
| PACIENTE | Consultar e gerenciar informações próprias                |
| MEDICO   | Acessar funcionalidades clínicas autorizadas              |
| ADMIN    | Acessar funções administrativas e indicadores             |
| AUDITOR  | Consultar trilhas de auditoria sem alterar dados clínicos |


As permissões são validadas no backend por meio do Spring Security.

## 2. Autenticação

A autenticação utiliza:

- CPF como identificador
- Senhas armazenadas com BCrypt
- JWT assinado pelo backend
- Sessões stateless
- Perfil do usuário obtido do banco e incorporado ao token

O frontend não determina o perfil do usuário.

## 3. Proteção de dados sensíveis

A API aplica princípios de minimização de dados.

Não são incluídos em logs de auditoria:

- Senhas
- Hashes de senha
- Tokens JWT
- CPF
- Conteúdo clínico
- Sintomas ou observações médicas

DTOs são utilizados para evitar exposição direta das entidades internas.

## 4. Trilha de auditoria

Eventos relevantes são registrados na tabela de auditoria.

Exemplos:

- Login realizado
- Login negado
- Consulta de auditoria
- Acesso negado
- Consulta de dados sensíveis
- Alteração de status
- Exportação de dados
- Exclusão de registros
- Alteração de permissões

Cada evento pode registrar:

- Data e hora
- ID do usuário
- Perfil
- Ação
- Recurso
- Resultado
- IP
- Método HTTP
- Endpoint



## 5. Acesso negado

Tentativas de acesso a recursos incompatíveis com o perfil são bloqueadas pelo Spring Security e registradas na auditoria.

Exemplo:

```text
MEDICO
  ↓
GET /api/admin/...
  ↓
403 Forbidden
  ↓
ACESSO_NEGADO registrado
```



## 6. Consulta da auditoria

A trilha pode ser consultada apenas por:

- ADMIN
- AUDITOR

Endpoint:

```text
GET /api/auditoria/logs
```

Filtros disponíveis:

- Perfil
- Ação
- Resultado
- Data inicial
- Data final
- Paginação

Exemplo:

```text
/api/auditoria/logs?perfil=MEDICO&acao=ACESSO_NEGADO&resultado=NEGADO
```



## 7. Logs técnicos x registros de auditoria

Logs técnicos ajudam a identificar o comportamento da aplicação:

- Erros internos
- Exceções
- Falhas de infraestrutura
- Problemas de banco
- Inicialização do serviço

Registros de auditoria representam ações realizadas sobre o sistema:

- Quem executou
- Quando executou
- Qual ação ocorreu
- Sobre qual recurso
- Qual foi o resultado

Em resumo:

> Logs técnicos explicam o comportamento do software. Registros de auditoria explicam as ações dos usuários sobre recursos protegidos.



## 8. Proteções aplicadas à API

A API implementa:

- Spring Security
- Autenticação JWT
- BCrypt
- RBAC
- Default deny
- DTOs
- Validação de entrada
- Paginação
- Limite máximo de registros por página
- Respostas de erro padronizadas
- Segredos via variáveis de ambiente
- Usuários de teste restritos ao profile `dev`
- Auditoria de acessos negados
- Minimização de dados



## 9. Códigos HTTP


| Código | Significado                               |
| ------ | ----------------------------------------- |
| 200    | Operação realizada                        |
| 400    | Dados ou parâmetros inválidos             |
| 401    | Usuário não autenticado ou token inválido |
| 403    | Usuário autenticado sem permissão         |
| 500    | Erro interno do servidor                  |




## 10. Arquitetura de segurança

```text
Cliente
   ↓
Login
   ↓
CPF + senha
   ↓
BCrypt
   ↓
Banco de usuários
   ↓
JWT
   ↓
Spring Security
   ↓
RBAC
   ↓
Endpoint autorizado
   ↓
Auditoria
```





