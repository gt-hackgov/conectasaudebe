# Blockchain — Auditoria da fila (protótipo em Solidity)

Protótipo acadêmico do **Conecta Saúde** (HackGov · FIAP 2026) para a Parte 4 da atividade "Visão de Futuro".

O contrato `AuditoriaFilaConectaSaude.sol` registra na blockchain a **fila de atendimento por classificação de risco** da UPA, a mesma regra do heap do back-end (cor mais grave primeiro e, na mesma cor, quem chegou antes).

| Garantia | Como o contrato faz |
|---|---|
| Só a unidade autorizada registra | A Secretaria (dona do contrato) autoriza o endereço de cada UPA |
| Ninguém fura a fila | `chamar(senha)` só é aceito se a senha for a próxima pela regra; senão, `ForaDeOrdem` |
| Registro imutável e público | Eventos `PacienteEntrou` e `PacienteChamado` com data e hora |
| LGPD | Só vão para a cadeia a senha, a cor e o **hash** do registro; nenhum nome ou CPF |
| Auditoria | `conferirRegistro(senha, hash)` prova se o registro do sistema foi alterado |

## Testar no navegador (Remix, gratuito, sem instalar nada)
1. Abra https://remix.ethereum.org e crie o arquivo `AuditoriaFilaConectaSaude.sol` com o conteúdo deste repositório.
2. Aba **Solidity Compiler**: versão `0.8.24` → **Compile**.
3. Aba **Deploy & Run**: ambiente **Remix VM** → **Deploy** (a conta 1 vira a Secretaria).
4. `autorizarUnidade(<endereço da conta 2>, true)`. Troque para a conta 2 (a UPA).
5. `registrarEntrada(1, 3, 0x1111...)` (senha 1, cor 3 = VERDE) e `registrarEntrada(7, 0, 0x7777...)` (senha 7, VERMELHO). O hash precisa ter 32 bytes: `0x` + 64 caracteres hexadecimais.
6. `proximo()` → senha 7. `chamar(1)` → **erro ForaDeOrdem(1, 7)**. `chamar(7)` → aceito.

Cores: 0 VERMELHO, 1 LARANJA, 2 AMARELO, 3 VERDE, 4 AZUL.

## Testar pelo terminal (blockchain local)
```bash
npm install
npm test
```
Saída esperada em `saida_teste.txt`: 6 tentativas inválidas rejeitadas e ordem final `7 → 5 → 2 → 6 → 1 → 4 → 3`, idêntica à do heap do back-end.
