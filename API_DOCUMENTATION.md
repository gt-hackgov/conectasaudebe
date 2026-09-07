# Documentação da API RESTful (HackGov - Conecta Saúde)

Esta documentação descreve os endpoints RESTful implementados para as principais funcionalidades do sistema Conecta Saúde.

## Base URL
Todas as requisições devem ser feitas com o prefixo: `/api`

---

## 1. Autenticação

### `POST /api/auth/login`
Autentica um cidadão no sistema.

- **Autenticação**: Não requerida.
- **Corpo da Requisição (JSON)**:
  ```json
  {
    "cpf": "123.456.789-00",
    "password": "senha"
  }
  ```
- **Respostas Esperadas**:
  - `200 OK`: Login bem-sucedido.
    ```json
    {
      "message": "Login realizado com sucesso",
      "token": "mock-token-1678888888",
      "user": {
        "id": "usr-12345",
        "name": "Cidadão Exemplo",
        "cpf": "123.456.789-00"
      }
    }
    ```
  - `400 Bad Request`: CPF ou senha ausentes.
  - `500 Internal Server Error`: Erro inesperado no servidor.

---

## 2. Agendamentos (Appointments)

### `GET /api/appointments`
Retorna a lista de consultas médicas agendadas do paciente.

- **Autenticação**: Requerida (`Authorization: Bearer <token>`).
- **Respostas Esperadas**:
  - `200 OK`:
    ```json
    {
      "appointments": [
        {
          "id": "apt-1678888999",
          "date": "15/10/2026",
          "time": "14:30",
          "location": "UBS Centro",
          "specialty": "Clínico Geral",
          "notes": "Levar exames anteriores",
          "createdAt": "2026-09-07T12:00:00Z"
        }
      ]
    }
    ```
  - `401 Unauthorized`: Token inválido ou ausente.
  - `500 Internal Server Error`: Erro no servidor.

### `POST /api/appointments`
Cria um novo agendamento.

- **Autenticação**: Requerida (`Authorization: Bearer <token>`).
- **Corpo da Requisição (JSON)**:
  ```json
  {
    "date": "20/10/2026",
    "time": "09:00",
    "location": "Posto de Saúde Vila Nova",
    "specialty": "Pediatria",
    "notes": "Primeira consulta"
  }
  ```
- **Respostas Esperadas**:
  - `201 Created`: Consulta agendada.
    ```json
    {
      "message": "Agendamento criado com sucesso",
      "appointment": { ... }
    }
    ```
  - `400 Bad Request`: Campos obrigatórios ausentes.
  - `401 Unauthorized`: Token inválido.

### `DELETE /api/appointments/[id]`
Cancela/remove um agendamento específico.

- **Autenticação**: Requerida (`Authorization: Bearer <token>`).
- **Parâmetros da Rota**: `id` - O identificador único da consulta.
- **Respostas Esperadas**:
  - `200 OK`:
    ```json
    {
      "message": "Consulta cancelada com sucesso"
    }
    ```
  - `400 Bad Request`: ID não fornecido.
  - `401 Unauthorized`: Token inválido.
  - `404 Not Found`: Consulta não localizada com o ID informado.
  - `500 Internal Server Error`: Erro no servidor.

---

## 3. Notificações

### `GET /api/notifications`
Retorna as notificações e alertas de saúde pública.

- **Autenticação**: Requerida (`Authorization: Bearer <token>`).
- **Respostas Esperadas**:
  - `200 OK`:
    ```json
    {
      "notifications": [
        {
          "id": "notif-1",
          "title": "Previna-se da Dengue!",
          "message": "Elimine focos de água parada.",
          "time": "Agora"
        }
      ]
    }
    ```
  - `401 Unauthorized`: Token ausente ou inválido.
