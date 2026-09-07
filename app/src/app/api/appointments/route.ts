import { NextRequest, NextResponse } from "next/server";
import { validateBearerToken } from "@/lib/authMiddleware";
import { getDb, saveDb, Appointment } from "@/lib/db";

export async function GET(request: NextRequest) {
  if (!validateBearerToken(request)) {
    return NextResponse.json({ error: "Acesso não autorizado." }, { status: 401 });
  }

  try {
    const db = getDb();
    return NextResponse.json({ appointments: db.appointments }, { status: 200 });
  } catch (error) {
    return NextResponse.json({ error: "Erro interno do servidor." }, { status: 500 });
  }
}

export async function POST(request: NextRequest) {
  if (!validateBearerToken(request)) {
    return NextResponse.json({ error: "Acesso não autorizado." }, { status: 401 });
  }

  try {
    const body = await request.json();
    const { date, time, location, specialty, notes } = body;

    if (!date || !time || !location || !specialty) {
      return NextResponse.json(
        { error: "Os campos date, time, location e specialty são obrigatórios." },
        { status: 400 }
      );
    }

    const db = getDb();
    const newAppointment: Appointment = {
      id: `apt-${Date.now()}`,
      createdAt: new Date().toISOString(),
      date,
      time,
      location,
      specialty,
      notes: notes || "",
    };

    db.appointments.push(newAppointment);
    saveDb(db);

    return NextResponse.json(
      { message: "Agendamento criado com sucesso", appointment: newAppointment },
      { status: 201 }
    );
  } catch (error) {
    return NextResponse.json({ error: "Erro interno do servidor." }, { status: 500 });
  }
}
