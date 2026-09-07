import { NextRequest } from "next/server";

export const validateBearerToken = (request: NextRequest): boolean => {
  const authHeader = request.headers.get("authorization");
  
  if (!authHeader) {
    return false;
  }

  // Verifica se o token segue o padrão "Bearer <token>"
  const parts = authHeader.split(" ");
  if (parts.length !== 2 || parts[0] !== "Bearer") {
    return false;
  }

  const token = parts[1];

  // Simulação: qualquer token não vazio e que comece com 'mock-token' é aceito.
  if (!token || !token.startsWith("mock-token")) {
    return false;
  }

  return true;
};
