import { NextRequest, NextResponse } from "next/server";
import { validateBearerToken } from "@/lib/authMiddleware";
import { getDb, saveDb } from "@/lib/db";

export async function DELETE(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  if (!validateBearerToken(request)) {
    return NextResponse.json({ error: "Acesso não autorizado." }, { status: 401 });
  }

  try {
    const resolvedParams = await params;
    const { id } = resolvedParams;
    if (!id) {
      return NextResponse.json({ error: "ID da consulta é obrigatório." }, { status: 400 });
    }

    const db = getDb();
    const initialLength = db.appointments.length;
    
    // Remove the appointment by filtering it out
    db.appointments = db.appointments.filter((apt) => apt.id !== id);

    if (db.appointments.length === initialLength) {
      return NextResponse.json({ error: "Consulta não encontrada." }, { status: 404 });
    }

    saveDb(db);

    return NextResponse.json(
      { message: "Consulta cancelada com sucesso" },
      { status: 200 }
    );
  } catch (error) {
    return NextResponse.json({ error: "Erro interno do servidor." }, { status: 500 });
  }
}
