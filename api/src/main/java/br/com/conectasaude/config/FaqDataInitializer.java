package br.com.conectasaude.config;

import br.com.conectasaude.model.FaqEntry;
import br.com.conectasaude.repository.FaqEntryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class FaqDataInitializer implements CommandLineRunner {

    private final FaqEntryRepository faqRepository;

    public FaqDataInitializer(FaqEntryRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (faqRepository.count() == 0) {
            initializeFaqData();
        }
    }

    private void initializeFaqData() {
        faqRepository.save(new FaqEntry(
                "Como faço para agendar um clínico geral na UBS do meu bairro?",
                "Você pode agendar pelo aplicativo, na aba \"Agendar Consulta\", selecionando a especialidade \"Clínico Geral\" e a UBS mais próxima do seu endereço cadastrado. Também é possível agendar presencialmente na recepção da unidade ou pelo telefone da UBS.",
                "AGENDAMENTO",
                Arrays.asList("agendar", "clinico geral", "ubs", "marcar consulta", "consulta")
        ));

        faqRepository.save(new FaqEntry(
                "Surgiu um imprevisto e não poderei ir à consulta amanhã. Como faço para cancelar ou remarcar?",
                "Acesse \"Minhas Consultas\" no aplicativo, selecione o agendamento e toque em \"Cancelar\" ou \"Remarcar\". Recomendamos fazer isso com no mínimo 24 horas de antecedência para liberar a vaga para outro paciente.",
                "AGENDAMENTO",
                Arrays.asList("cancelar", "remarcar", "consulta", "imprevisto", "desmarcar")
        ));

        faqRepository.save(new FaqEntry(
                "Consigo marcar um exame para outra pessoa, como minha avó, usando o meu aplicativo?",
                "Não é possível agendar exames para outra pessoa pelo seu login. Cada paciente precisa ter cadastro próprio no aplicativo, com seu CPF e Cartão SUS. Caso a outra pessoa tenha dificuldade para usar o app, o agendamento pode ser feito presencialmente na UBS, com um responsável.",
                "AGENDAMENTO",
                Arrays.asList("exame", "terceiro", "outra pessoa", "avo", "dependente")
        ));

        faqRepository.save(new FaqEntry(
                "Como faço para ver a data, o horário e o nome do médico a minha próxima consulta?",
                "Abra o aplicativo e acesse \"Minhas Consultas\" na tela inicial. Lá aparecem todos os agendamentos futuros, com data, horário, unidade e nome do profissional responsável.",
                "AGENDAMENTO",
                Arrays.asList("proxima consulta", "horario", "medico", "data", "minhas consultas")
        ));

        faqRepository.save(new FaqEntry(
                "Preciso fazer jejum de quantas horas para o exame de sangue de rotina?",
                "Para a maioria dos exames de sangue de rotina, o jejum recomendado é de 8 a 12 horas. Alguns exames específicos podem ter orientação diferente — confira o pedido médico ou as instruções enviadas no aplicativo ao confirmar o agendamento.",
                "EXAMES",
                Arrays.asList("jejum", "exame de sangue", "horas jejum", "preparo exame")
        ));

        faqRepository.save(new FaqEntry(
                "Posso tomar meu remédio de pressão normalmente antes de fazer o eletrocardiograma?",
                "Sim, na maioria dos casos os medicamentos de uso contínuo, incluindo os de pressão, devem ser tomados normalmente antes do eletrocardiograma. Em caso de dúvida sobre seu caso específico, confirme com o médico que solicitou o exame ou com a equipe da UBS no dia do atendimento.",
                "EXAMES",
                Arrays.asList("remedio", "pressao", "eletrocardiograma", "medicamento continuo")
        ));

        faqRepository.save(new FaqEntry(
                "O que eu preciso levar no dia do meu ultrassom? Precisa do pedido impresso?",
                "Leve um documento de identificação com foto, o Cartão SUS e o pedido médico impresso (ou o número de autorização, se o pedido foi feito digitalmente). Verifique também se o exame exige algum preparo específico, como jejum ou bexiga cheia.",
                "EXAMES",
                Arrays.asList("ultrassom", "pedido impresso", "documentos exame", "o que levar")
        ));

        faqRepository.save(new FaqEntry(
                "Preciso beber água antes do exame de ultrassom pélvico?",
                "Sim. Para o ultrassom pélvico geralmente é necessário estar com a bexiga cheia. Recomenda-se beber cerca de 4 a 6 copos de água (aproximadamente 1 litro) na hora antes do exame e não urinar até o procedimento ser realizado.",
                "EXAMES",
                Arrays.asList("ultrassom pelvico", "beber agua", "bexiga cheia", "preparo ultrassom")
        ));

        faqRepository.save(new FaqEntry(
                "Qual é o horário de funcionamento da farmácia da UBS para retirada de remédios?",
                "A farmácia da UBS geralmente funciona de segunda a sexta-feira, das 7h às 17h, seguindo o horário de atendimento da unidade. Os horários podem variar por localidade — confira o horário específico da sua UBS na aba \"Unidades\" do aplicativo.",
                "FARMACIA",
                Arrays.asList("farmacia", "horario funcionamento", "retirar remedio", "ubs horario")
        ));

        faqRepository.save(new FaqEntry(
                "Quais documentos físicos eu preciso levar na minha primeira consulta?",
                "Leve um documento de identificação com foto (RG ou CNH), CPF, Cartão SUS (ou comprovante de cadastro) e comprovante de residência atualizado. Se tiver exames ou laudos anteriores relacionados ao motivo da consulta, também é recomendado levá-los.",
                "DOCUMENTOS",
                Arrays.asList("documentos", "primeira consulta", "rg", "cpf", "comprovante residencia")
        ));

        faqRepository.save(new FaqEntry(
                "Como faço para emitir, atualizar ou recuperar o número do meu cartão do SUS?",
                "Você pode emitir ou recuperar o número do Cartão SUS pelo aplicativo, na aba \"Meu Cadastro\", ou presencialmente em qualquer UBS, levando documento de identidade, CPF e comprovante de residência. Para atualização de dados cadastrais, o procedimento é o mesmo.",
                "CARTAO_SUS",
                Arrays.asList("cartao sus", "emitir cartao", "recuperar numero", "atualizar cadastro sus")
        ));

        faqRepository.save(new FaqEntry(
                "A campanha de vacinação contra a gripe já começou na minha unidade?",
                "As datas da campanha de vacinação contra a gripe variam por município e são divulgadas anualmente pela Secretaria de Saúde. Consulte a aba \"Vacinação\" no aplicativo ou entre em contato com sua UBS para confirmar se a campanha já está em andamento na sua unidade.",
                "VACINACAO",
                Arrays.asList("vacinacao", "campanha gripe", "vacina gripe", "influenza")
        ));

        faqRepository.save(new FaqEntry(
                "Estou com febre alta e falta de ar. Devo ir à UBS ou direto a uma UPA?",
                "Febre alta associada a falta de ar pode indicar um quadro que precisa de atendimento de urgência. Nesses casos, procure diretamente uma UPA (Unidade de Pronto Atendimento) ou o pronto-socorro mais próximo, em vez da UBS. Em caso de piora rápida dos sintomas ou dificuldade grave para respirar, ligue para o SAMU (192).",
                "URGENCIA",
                Arrays.asList("febre alta", "falta de ar", "upa", "urgencia", "samu")
        ));

        faqRepository.save(new FaqEntry(
                "Onde fica o posto de saúde mais próximo do meu endereço atual?",
                "No aplicativo, acesse a aba \"Unidades\" e toque em \"Usar minha localização\" para ver as UBSs mais próximas do seu endereço, com endereço completo, horário de funcionamento e distância.",
                "LOCALIZACAO",
                Arrays.asList("posto de saude", "unidade proxima", "ubs proxima", "localizacao")
        ));

        faqRepository.save(new FaqEntry(
                "Como funciona o processo para pegar medicamentos de uso contínuo (como insulina) pelo SUS?",
                "Medicamentos de uso contínuo, como insulina, são retirados na farmácia da UBS mediante apresentação da receita médica atualizada e do Cartão SUS. Em geral, é necessário fazer o cadastro no Programa de Medicamentos de Uso Contínuo da unidade para garantir a retirada mensal sem necessidade de nova consulta a cada vez.",
                "MEDICAMENTOS",
                Arrays.asList("medicamento continuo", "insulina", "retirar remedio sus", "receita medica")
        ));
    }
}
