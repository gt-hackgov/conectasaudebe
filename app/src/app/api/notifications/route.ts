import { NextRequest, NextResponse } from "next/server";
import { validateBearerToken } from "@/lib/authMiddleware";
import { getDb } from "@/lib/db";

export async function GET(request: NextRequest) {
  if (!validateBearerToken(request)) {
    return NextResponse.json({ error: "Acesso não autorizado." }, { status: 401 });
  }

  try {
    const db = getDb();
    return NextResponse.json({ notifications: db.notifications }, { status: 200 });
  } catch (error) {
    return NextResponse.json({ error: "Erro interno do servidor." }, { status: 500 });
  }
}
