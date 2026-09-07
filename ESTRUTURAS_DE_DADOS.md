# Estruturas de Dados Avançadas no Conecta Saúde

Este documento descreve a identificação, implementação e justificativa técnica do uso de estruturas de dados clássicas (**Lista Encadeada**, **Pilha** e **Fila**) no portal **Conecta Saúde** (HackGov).

---

## 1. Visão Geral das Estruturas Utilizadas

As estruturas foram implementadas no módulo [`app/src/lib/dataStructures.ts`](file:///Volumes/HardDrive/conecta-saude/app/src/lib/dataStructures.ts) utilizando **TypeScript** tipado e aderente aos requisitos funcionais do cidadão/paciente.

| Estrutura de Dados | Tipo | Aplicação Principal no Sistema | Arquivo Fonte |
| :--- | :--- | :--- | :--- |
| **Lista Encadeada** (`LinkedList`) | Linear / Dinâmica | Gerenciamento e ordenação sequencial do Histórico de Consultas | [`consultas-agendadas/page.tsx`](file:///Volumes/HardDrive/conecta-saude/app/src/app/consultas-agendadas/page.tsx) |
| **Pilha** (`Stack`) | LIFO (Last-In, First-Out) | Histórico de Ações e Funcionalidade de "Desfazer Cancelamento" (Undo) | [`consultas-agendadas/page.tsx`](file:///Volumes/HardDrive/conecta-saude/app/src/app/consultas-agendadas/page.tsx) |
| **Fila** (`Queue`) | FIFO (First-In, First-Out) | Central de Notificações e Alertas de Saúde Pública do Cidadão | [`dashboard/page.tsx`](file:///Volumes/HardDrive/conecta-saude/app/src/app/dashboard/page.tsx) |

---

## 2. Detalhamento por Estrutura

### 2.1 Lista Encadeada (`LinkedList`)

- **Problema Resolvido**: Manutenção eficiente de um histórico dinâmico de agendamentos. A estrutura encadeada permite inserções, remoções e percursos de elementos sem a necessidade de recomputar alocações contíguas de memória.
- **Funcionamento no Sistema**:
  1. Quando o usuário acessa a página de consultas agendadas, o vetor original é carregado e transformado em uma `LinkedList`.
  2. Cada nó (`Node<Appointment>`) contém os dados do agendamento e a referência `next` para o próximo.
  3. A remoção de uma consulta (cancelamento) é realizada via método `remove()`, desvinculando o nó da cadeia.
  4. O estado persiste sincronizado via `toArray()`.

---

### 2.2 Pilha (`Stack`)

- **Problema Resolvido**: Prevenção de perda acidental de agendamentos. Permite que o cidadão desfaça cancelamentos realizados por engano.
- **Funcionamento no Sistema**:
  1. Ao clicar em **Cancelar** em um agendamento, o objeto removido é empilhado (`push`) na `undoStack`.
  2. Ao clicar no botão **Desfazer cancelamento**, a última ação registrada é desempilhada (`pop`) e o agendamento correspondente é inserido novamente na `LinkedList` de consultas.
  3. Garante o comportamento **LIFO** (Last-In, First-Out): o último cancelamento realizado é sempre o primeiro a ser desfeito.

---

### 2.3 Fila (`Queue`)

- **Problema Resolvido**: Organização e consumo sequencial de notificações e alertas em ordem cronológica de chegada (First-In, First-Out).
- **Funcionamento no Sistema**:
  1. Avisos e lembretes (ex: Alerta de Dengue, Lembrete de Consulta, Vacinação) são enfileirados (`enqueue`).
  2. A notificação no topo da fila (First-In) é destacada como a próxima a ser lida.
  3. Quando o usuário clica em "Ler primeira (Dequeue)", a notificação mais antiga é removida (`dequeue`) e a fila avança para o item seguinte.

---

## 3. Validação e Compilação

- **Implementação**: Código limpo, sem dependências externas e com tipagem forte.
- **Integração**: Integração total com `localStorage` e fluxo de navegação do Next.js App Router.
