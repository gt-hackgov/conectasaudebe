// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

/**
 * @title  AuditoriaFilaConectaSaude
 * @notice Protótipo acadêmico (HackGov · FIAP 2026) de registro imutável da fila de
 *         atendimento por classificação de risco do Conecta Saúde.
 *
 * O que o contrato garante:
 *  1. Só unidades de saúde autorizadas pela Secretaria (dona do contrato) registram eventos.
 *  2. A ordem da fila é verificada pela própria blockchain: a chamada só é aceita se o
 *     paciente for o próximo pela regra "cor mais grave primeiro e, na mesma cor, quem
 *     chegou antes". Uma tentativa de "furar a fila" é rejeitada (revert).
 *  3. Cada entrada e cada chamada ficam gravadas com data e hora e geram um evento público
 *     que qualquer órgão de controle pode conferir.
 *
 * LGPD: NENHUM dado pessoal vai para a blockchain. Gravamos apenas o número da senha, a cor de
 * risco e um hash (SHA-256 calculado fora da cadeia, com "sal") do registro que fica no banco
 * do sistema. Sem o registro original, o hash não revela nada; com ele, prova que não foi alterado.
 */
contract AuditoriaFilaConectaSaude {
    // Ordem de gravidade do Protocolo de Manchester (0 = mais grave).
    enum Cor { VERMELHO, LARANJA, AMARELO, VERDE, AZUL }
    enum Situacao { INEXISTENTE, AGUARDANDO, CHAMADO }

    struct Registro {
        uint32 senha;          // número da senha na unidade (sem nome, sem CPF)
        Cor cor;               // classificação de risco
        Situacao situacao;
        uint64 entradaEm;      // carimbo de tempo da entrada (block.timestamp)
        uint64 chamadaEm;      // carimbo de tempo da chamada
        bytes32 hashRegistro;  // SHA-256 do registro guardado fora da cadeia
    }

    address public immutable secretaria;              // dona do contrato (gestor público)
    mapping(address => bool) public unidadeAutorizada;

    // Uma fila FIFO por cor: inicio[c] aponta para o próximo a ser chamado.
    mapping(uint8 => uint32[]) private filaPorCor;
    mapping(uint8 => uint256) private inicio;

    mapping(uint32 => Registro) public registros;     // senha -> registro
    uint256 public totalEntradas;
    uint256 public totalChamadas;

    event UnidadeAutorizada(address indexed unidade, bool autorizada);
    event PacienteEntrou(uint32 indexed senha, Cor cor, bytes32 hashRegistro, uint64 quando, address indexed unidade);
    event PacienteChamado(uint32 indexed senha, Cor cor, uint64 esperaSegundos, address indexed unidade);

    error SomenteSecretaria();
    error UnidadeNaoAutorizada();
    error SenhaJaRegistrada(uint32 senha);
    error HashInvalido();
    error FilaVazia();
    error ForaDeOrdem(uint32 senhaInformada, uint32 senhaCorreta);

    modifier somenteSecretaria() {
        if (msg.sender != secretaria) revert SomenteSecretaria();
        _;
    }

    modifier somenteUnidade() {
        if (!unidadeAutorizada[msg.sender]) revert UnidadeNaoAutorizada();
        _;
    }

    constructor() {
        secretaria = msg.sender;
    }

    /// @notice A Secretaria autoriza ou revoga o sistema de uma unidade (UPA).
    function autorizarUnidade(address unidade, bool autorizada) external somenteSecretaria {
        unidadeAutorizada[unidade] = autorizada;
        emit UnidadeAutorizada(unidade, autorizada);
    }

    /// @notice Registra a entrada de um paciente classificado. Custo O(1).
    function registrarEntrada(uint32 senha, Cor cor, bytes32 hashRegistro) external somenteUnidade {
        if (registros[senha].situacao != Situacao.INEXISTENTE) revert SenhaJaRegistrada(senha);
        if (hashRegistro == bytes32(0)) revert HashInvalido();

        registros[senha] = Registro({
            senha: senha,
            cor: cor,
            situacao: Situacao.AGUARDANDO,
            entradaEm: uint64(block.timestamp),
            chamadaEm: 0,
            hashRegistro: hashRegistro
        });
        filaPorCor[uint8(cor)].push(senha);
        totalEntradas++;
        emit PacienteEntrou(senha, cor, hashRegistro, uint64(block.timestamp), msg.sender);
    }

    /// @notice Próxima senha pela regra de prioridade (consulta gratuita, não grava nada).
    function proximo() public view returns (uint32 senha, Cor cor) {
        for (uint8 c = 0; c <= uint8(Cor.AZUL); c++) {
            if (inicio[c] < filaPorCor[c].length) {
                return (filaPorCor[c][inicio[c]], Cor(c));
            }
        }
        revert FilaVazia();
    }

    /// @notice Chama um paciente. Só aceita se ele for o próximo; senão, reverte (anti fura-fila).
    function chamar(uint32 senha) external somenteUnidade {
        (uint32 correta, Cor cor) = proximo();
        if (senha != correta) revert ForaDeOrdem(senha, correta);

        inicio[uint8(cor)]++;
        Registro storage r = registros[senha];
        r.situacao = Situacao.CHAMADO;
        r.chamadaEm = uint64(block.timestamp);
        totalChamadas++;
        emit PacienteChamado(senha, cor, r.chamadaEm - r.entradaEm, msg.sender);
    }

    /// @notice Auditor confere se o registro guardado no sistema é o mesmo gravado na blockchain.
    function conferirRegistro(uint32 senha, bytes32 hashCalculado) external view returns (bool) {
        return registros[senha].situacao != Situacao.INEXISTENTE && registros[senha].hashRegistro == hashCalculado;
    }

    /// @notice Quantidade de pacientes aguardando em cada cor.
    function aguardandoPorCor() external view returns (uint256[5] memory quantidades) {
        for (uint8 c = 0; c <= uint8(Cor.AZUL); c++) {
            quantidades[c] = filaPorCor[c].length - inicio[c];
        }
    }
}
