-- =====================================================================
-- FAQ SCHEMA - Chatbot de Suporte (UBS/SUS)
-- Compatível com PostgreSQL
-- =====================================================================

-- -----------------------------------------------------------------
-- Tabela principal de perguntas/respostas
-- -----------------------------------------------------------------
CREATE TABLE faq_entries (
    id          BIGSERIAL PRIMARY KEY,
    question    TEXT        NOT NULL,
    answer      TEXT        NOT NULL,
    category    VARCHAR(50) NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- -----------------------------------------------------------------
-- Tabela de keywords (relação 1:N com faq_entries)
-- Usada pelo ChatService para fazer o matching da pergunta do usuário
-- -----------------------------------------------------------------
CREATE TABLE faq_entry_keywords (
    faq_entry_id BIGINT      NOT NULL REFERENCES faq_entries(id) ON DELETE CASCADE,
    keyword      VARCHAR(100) NOT NULL
);

CREATE INDEX idx_faq_entry_keywords_entry_id ON faq_entry_keywords(faq_entry_id);
CREATE INDEX idx_faq_entry_keywords_keyword  ON faq_entry_keywords(keyword);
CREATE INDEX idx_faq_entries_category        ON faq_entries(category);

-- -----------------------------------------------------------------
-- Tabela para registrar perguntas que o bot não conseguiu responder
-- (útil para depois cadastrar novas entradas na FAQ)
-- -----------------------------------------------------------------
CREATE TABLE unanswered_questions (
    id           BIGSERIAL PRIMARY KEY,
    user_message TEXT      NOT NULL,
    asked_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    resolved     BOOLEAN   NOT NULL DEFAULT FALSE
);

-- =====================================================================
-- SEED DATA
-- Categorias: AGENDAMENTO, EXAMES, DOCUMENTOS, CARTAO_SUS,
--             VACINACAO, URGENCIA, LOCALIZACAO, MEDICAMENTOS, FARMACIA
-- =====================================================================

-- 1. Agendar clínico geral
INSERT INTO faq_entries (question, answer, category) VALUES
('Como faço para agendar um clínico geral na UBS do meu bairro?',
 'Você pode agendar pelo aplicativo, na aba "Agendar Consulta", selecionando a especialidade "Clínico Geral" e a UBS mais próxima do seu endereço cadastrado. Também é possível agendar presencialmente na recepção da unidade ou pelo telefone da UBS.',
 'AGENDAMENTO');

INSERT INTO faq_entry_keywords (faq_entry_id, keyword) VALUES
(1, 'agendar'), (1, 'clinico geral'), (1, 'ubs'), (1, 'marcar consulta'), (1, 'consulta');

-- 2. Cancelar ou remarcar consulta
INSERT INTO faq_entries (question, answer, category) VALUES
('Surgiu um imprevisto e não poderei ir à consulta amanhã. Como faço para cancelar ou remarcar?',
 'Acesse "Minhas Consultas" no aplicativo, selecione o agendamento e toque em "Cancelar" ou "Remarcar". Recomendamos fazer isso com no mínimo 24 horas de antecedência para liberar a vaga para outro paciente.',
 'AGENDAMENTO');

INSERT INTO faq_entry_keywords (faq_entry_id, keyword) VALUES
(2, 'cancelar'), (2, 'remarcar'), (2, 'consulta'), (2, 'imprevisto'), (2, 'desmarcar');

-- 3. Marcar exame para terceiros
INSERT INTO faq_entries (question, answer, category) VALUES
('Consigo marcar um exame para outra pessoa, como minha avó, usando o meu aplicativo?',
 'Não é possível agendar exames para outra pessoa pelo seu login. Cada paciente precisa ter cadastro próprio no aplicativo, com seu CPF e Cartão SUS. Caso a outra pessoa tenha dificuldade para usar o app, o agendamento pode ser feito presencialmente na UBS, com um responsável.',
 'AGENDAMENTO');

INSERT INTO faq_entry_keywords (faq_entry_id, keyword) VALUES
(3, 'exame'), (3, 'terceiro'), (3, 'outra pessoa'), (3, 'avo'), (3, 'dependente');

-- 4. Ver data/horário/médico da próxima consulta
INSERT INTO faq_entries (question, answer, category) VALUES
('Como faço para ver a data, o horário e o nome do médico a minha próxima consulta?',
 'Abra o aplicativo e acesse "Minhas Consultas" na tela inicial. Lá aparecem todos os agendamentos futuros, com data, horário, unidade e nome do profissional responsável.',
 'AGENDAMENTO');

INSERT INTO faq_entry_keywords (faq_entry_id, keyword) VALUES
(4, 'proxima consulta'), (4, 'horario'), (4, 'medico'), (4, 'data'), (4, 'minhas consultas');

-- 5. Jejum exame de sangue
INSERT INTO faq_entries (question, answer, category) VALUES
('Preciso fazer jejum de quantas horas para o exame de sangue de rotina?',
 'Para a maioria dos exames de sangue de rotina, o jejum recomendado é de 8 a 12 horas. Alguns exames específicos podem ter orientação diferente — confira o pedido médico ou as instruções enviadas no aplicativo ao confirmar o agendamento.',
 'EXAMES');

INSERT INTO faq_entry_keywords (faq_entry_id, keyword) VALUES
(5, 'jejum'), (5, 'exame de sangue'), (5, 'horas jejum'), (5, 'preparo exame');

-- 6. Remédio de pressão antes de eletrocardiograma
INSERT INTO faq_entries (question, answer, category) VALUES
('Posso tomar meu remédio de pressão normalmente antes de fazer o eletrocardiograma?',
 'Sim, na maioria dos casos os medicamentos de uso contínuo, incluindo os de pressão, devem ser tomados normalmente antes do eletrocardiograma. Em caso de dúvida sobre seu caso específico, confirme com o médico que solicitou o exame ou com a equipe da UBS no dia do atendimento.',
 'EXAMES');

INSERT INTO faq_entry_keywords (faq_entry_id, keyword) VALUES
(6, 'remedio'), (6, 'pressao'), (6, 'eletrocardiograma'), (6, 'medicamento continuo');

-- 7. O que levar no ultrassom
INSERT INTO faq_entries (question, answer, category) VALUES
('O que eu preciso levar no dia do meu ultrassom? Precisa do pedido impresso?',
 'Leve um documento de identificação com foto, o Cartão SUS e o pedido médico impresso (ou o número de autorização, se o pedido foi feito digitalmente). Verifique também se o exame exige algum preparo específico, como jejum ou bexiga cheia.',
 'EXAMES');

INSERT INTO faq_entry_keywords (faq_entry_id, keyword) VALUES
(7, 'ultrassom'), (7, 'pedido impresso'), (7, 'documentos exame'), (7, 'o que levar');

-- 8. Beber água antes de ultrassom pélvico
INSERT INTO faq_entries (question, answer, category) VALUES
('Preciso beber água antes do exame de ultrassom pélvico?',
 'Sim. Para o ultrassom pélvico geralmente é necessário estar com a bexiga cheia. Recomenda-se beber cerca de 4 a 6 copos de água (aproximadamente 1 litro) na hora antes do exame e não urinar até o procedimento ser realizado.',
 'EXAMES');

INSERT INTO faq_entry_keywords (faq_entry_id, keyword) VALUES
(8, 'ultrassom pelvico'), (8, 'beber agua'), (8, 'bexiga cheia'), (8, 'preparo ultrassom');

-- 9. Horário da farmácia da UBS
INSERT INTO faq_entries (question, answer, category) VALUES
('Qual é o horário de funcionamento da farmácia da UBS para retirada de remédios?',
 'A farmácia da UBS geralmente funciona de segunda a sexta-feira, das 7h às 17h, seguindo o horário de atendimento da unidade. Os horários podem variar por localidade — confira o horário específico da sua UBS na aba "Unidades" do aplicativo.',
 'FARMACIA');

INSERT INTO faq_entry_keywords (faq_entry_id, keyword) VALUES
(9, 'farmacia'), (9, 'horario funcionamento'), (9, 'retirar remedio'), (9, 'ubs horario');

-- 10. Documentos para primeira consulta
INSERT INTO faq_entries (question, answer, category) VALUES
('Quais documentos físicos eu preciso levar na minha primeira consulta?',
 'Leve um documento de identificação com foto (RG ou CNH), CPF, Cartão SUS (ou comprovante de cadastro) e comprovante de residência atualizado. Se tiver exames ou laudos anteriores relacionados ao motivo da consulta, também é recomendado levá-los.',
 'DOCUMENTOS');

INSERT INTO faq_entry_keywords (faq_entry_id, keyword) VALUES
(10, 'documentos'), (10, 'primeira consulta'), (10, 'rg'), (10, 'cpf'), (10, 'comprovante residencia');

-- 11. Emitir/atualizar/recuperar Cartão SUS
INSERT INTO faq_entries (question, answer, category) VALUES
('Como faço para emitir, atualizar ou recuperar o número do meu cartão do SUS?',
 'Você pode emitir ou recuperar o número do Cartão SUS pelo aplicativo, na aba "Meu Cadastro", ou presencialmente em qualquer UBS, levando documento de identidade, CPF e comprovante de residência. Para atualização de dados cadastrais, o procedimento é o mesmo.',
 'CARTAO_SUS');

INSERT INTO faq_entry_keywords (faq_entry_id, keyword) VALUES
(11, 'cartao sus'), (11, 'emitir cartao'), (11, 'recuperar numero'), (11, 'atualizar cadastro sus');

-- 12. Campanha de vacinação contra gripe
INSERT INTO faq_entries (question, answer, category) VALUES
('A campanha de vacinação contra a gripe já começou na minha unidade?',
 'As datas da campanha de vacinação contra a gripe variam por município e são divulgadas anualmente pela Secretaria de Saúde. Consulte a aba "Vacinação" no aplicativo ou entre em contato com sua UBS para confirmar se a campanha já está em andamento na sua unidade.',
 'VACINACAO');

INSERT INTO faq_entry_keywords (faq_entry_id, keyword) VALUES
(12, 'vacinacao'), (12, 'campanha gripe'), (12, 'vacina gripe'), (12, 'influenza');

-- 13. Febre alta e falta de ar: UBS ou UPA
INSERT INTO faq_entries (question, answer, category) VALUES
('Estou com febre alta e falta de ar. Devo ir à UBS ou direto a uma UPA?',
 'Febre alta associada a falta de ar pode indicar um quadro que precisa de atendimento de urgência. Nesses casos, procure diretamente uma UPA (Unidade de Pronto Atendimento) ou o pronto-socorro mais próximo, em vez da UBS. Em caso de piora rápida dos sintomas ou dificuldade grave para respirar, ligue para o SAMU (192).',
 'URGENCIA');

INSERT INTO faq_entry_keywords (faq_entry_id, keyword) VALUES
(13, 'febre alta'), (13, 'falta de ar'), (13, 'upa'), (13, 'urgencia'), (13, 'samu');

-- 14. Posto de saúde mais próximo
INSERT INTO faq_entries (question, answer, category) VALUES
('Onde fica o posto de saúde mais próximo do meu endereço atual?',
 'No aplicativo, acesse a aba "Unidades" e toque em "Usar minha localização" para ver as UBSs mais próximas do seu endereço, com endereço completo, horário de funcionamento e distância.',
 'LOCALIZACAO');

INSERT INTO faq_entry_keywords (faq_entry_id, keyword) VALUES
(14, 'posto de saude'), (14, 'unidade proxima'), (14, 'ubs proxima'), (14, 'localizacao');

-- 15. Medicamentos de uso contínuo (ex.: insulina) pelo SUS
INSERT INTO faq_entries (question, answer, category) VALUES
('Como funciona o processo para pegar medicamentos de uso contínuo (como insulina) pelo SUS?',
 'Medicamentos de uso contínuo, como insulina, são retirados na farmácia da UBS mediante apresentação da receita médica atualizada e do Cartão SUS. Em geral, é necessário fazer o cadastro no Programa de Medicamentos de Uso Contínuo da unidade para garantir a retirada mensal sem necessidade de nova consulta a cada vez.',
 'MEDICAMENTOS');

INSERT INTO faq_entry_keywords (faq_entry_id, keyword) VALUES
(15, 'medicamento continuo'), (15, 'insulina'), (15, 'retirar remedio sus'), (15, 'receita medica');
