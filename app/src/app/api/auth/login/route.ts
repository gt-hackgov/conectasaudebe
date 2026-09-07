import { NextRequest, NextResponse } from "next/server";

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { cpf, password } = body;

    // Simulação: qualquer CPF e senha são aceitos, exceto se estiverem em branco
    if (!cpf || !password) {
      return NextResponse.json(
        { error: "CPF e Senha são obrigatórios." },
        { status: 400 }
      );
    }

    // Token simulado e dados de usuário
    const mockToken = `mock-token-${Date.now()}`;
    const user = {
      id: "usr-12345",
      name: "Cidadão Exemplo",
      cpf: cpf,
    };

    return NextResponse.json(
      {
        message: "Login realizado com sucesso",
        token: mockToken,
        user,
      },
      { status: 200 }
    );
  } catch (error) {
    return NextResponse.json(
      { error: "Erro interno do servidor." },
      { status: 500 }
    );
  }
}
