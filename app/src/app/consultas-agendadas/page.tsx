"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { LinkedList, Stack } from "@/lib/dataStructures";

type Appointment = {
  id?: string;
  date: string;
  time: string;
  location: string;
  specialty: string;
  notes: string;
  createdAt: string;
};

type UndoAction = {
  type: "CANCEL";
  appointment: Appointment;
};

export default function ScheduledAppointmentsPage() {
  const router = useRouter();
  const [appointmentList, setAppointmentList] = useState<LinkedList<Appointment>>(new LinkedList());
  const [undoStack] = useState<Stack<UndoAction>>(() => new Stack<UndoAction>());
  const [appointmentsArray, setAppointmentsArray] = useState<Appointment[]>([]);
  const [undoCount, setUndoCount] = useState<number>(0);

  useEffect(() => {
    async function fetchAppointments() {
      try {
        const res = await fetch("/api/appointments", {
          headers: { Authorization: "Bearer mock-token-123" },
        });
        if (res.ok) {
          const data = await res.json();
          const list = LinkedList.fromArray<Appointment>(data.appointments);
          setAppointmentList(list);
          setAppointmentsArray(list.toArray());
        }
      } catch (error) {
        console.error("Erro ao buscar consultas:", error);
      }
    }
    fetchAppointments();
  }, []);

  const syncList = (list: LinkedList<Appointment>) => {
    setAppointmentsArray(list.toArray());
  };

  const handleCancelAppointment = async (createdAt: string) => {
    const target = appointmentList.toArray().find((a) => a.createdAt === createdAt);
    if (!target) return;

    try {
      if (target.id) {
        await fetch(`/api/appointments/${target.id}`, {
          method: "DELETE",
          headers: { Authorization: "Bearer mock-token-123" },
        });
      }
      const removed = appointmentList.remove((item) => item.createdAt === createdAt);
      if (removed) {
        undoStack.push({ type: "CANCEL", appointment: removed });
        setUndoCount(undoStack.size());
        syncList(appointmentList);
      }
    } catch (error) {
      console.error("Erro ao cancelar consulta:", error);
    }
  };

  const handleUndo = async () => {
    const lastAction = undoStack.pop();
    if (lastAction && lastAction.type === "CANCEL") {
      try {
        await fetch("/api/appointments", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer mock-token-123",
          },
          body: JSON.stringify(lastAction.appointment),
        });
        appointmentList.append(lastAction.appointment);
        setUndoCount(undoStack.size());
        syncList(appointmentList);
      } catch (error) {
        console.error("Erro ao desfazer cancelamento:", error);
      }
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-white via-indigo-50 to-white px-6 py-10">
      <div className="mx-auto w-full max-w-6xl rounded-3xl bg-white p-8 shadow-lg dark:bg-zinc-950">
        <header className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-sm font-semibold text-indigo-600">Consultas agendadas</p>
            <h1 className="text-3xl font-semibold text-zinc-900 dark:text-zinc-50">Histórico de agendamentos</h1>
            <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
              Veja suas consultas marcadas, cancele ou desfaça alterações de forma simples.
            </p>
          </div>
          <div className="flex flex-wrap items-center gap-3">
            {undoCount > 0 && (
              <button
                type="button"
                onClick={handleUndo}
                className="inline-flex items-center gap-2 rounded-xl bg-amber-500 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-amber-600"
              >
                <span>↩ Desfazer cancelamento</span>
                <span className="rounded-full bg-amber-700/40 px-2 py-0.5 text-xs">{undoCount}</span>
              </button>
            )}
            <button
              type="button"
              onClick={() => router.push("/dashboard")}
              className="inline-flex items-center justify-center rounded-xl border border-zinc-200 bg-white px-4 py-2 text-sm font-semibold text-zinc-700 shadow-sm transition hover:bg-zinc-50 dark:border-zinc-800 dark:bg-zinc-950 dark:text-zinc-100 dark:hover:bg-zinc-900"
            >
              Voltar ao painel
            </button>
          </div>
        </header>

        {appointmentsArray.length === 0 ? (
          <div className="rounded-3xl border border-dashed border-zinc-200 bg-zinc-50 p-10 text-center text-zinc-600 dark:border-zinc-800 dark:bg-zinc-900 dark:text-zinc-300">
            <p className="text-lg font-semibold">Nenhuma consulta agendada no momento.</p>
            <p className="mt-2 text-sm">Use o painel para agendar uma nova consulta médica.</p>
          </div>
        ) : (
          <div className="space-y-6">
            {appointmentsArray.map((appointment) => (
              <div
                key={appointment.createdAt}
                className="rounded-3xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-950"
              >
                <div className="flex flex-wrap items-center justify-between gap-4">
                  <div>
                    <p className="text-sm font-semibold text-zinc-600 dark:text-zinc-400">Data e horário</p>
                    <p className="mt-1 text-lg font-semibold text-zinc-900 dark:text-zinc-50">
                      {appointment.date} às {appointment.time}
                    </p>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="rounded-full bg-indigo-100 px-3 py-1 text-sm font-semibold text-indigo-700 dark:bg-indigo-500/10 dark:text-indigo-200">
                      {appointment.specialty}
                    </span>
                    <button
                      type="button"
                      onClick={() => handleCancelAppointment(appointment.createdAt)}
                      className="rounded-xl border border-red-200 bg-red-50 px-3 py-1.5 text-xs font-semibold text-red-600 transition hover:bg-red-100 dark:border-red-900/30 dark:bg-red-950/40 dark:text-red-400"
                    >
                      Cancelar
                    </button>
                  </div>
                </div>

                <div className="mt-6 grid gap-4 md:grid-cols-2">
                  <div>
                    <p className="text-sm font-semibold text-zinc-600 dark:text-zinc-400">Unidade</p>
                    <p className="mt-1 text-sm text-zinc-900 dark:text-zinc-100">{appointment.location}</p>
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-zinc-600 dark:text-zinc-400">Agendado em</p>
                    <p className="mt-1 text-sm text-zinc-900 dark:text-zinc-100">
                      {new Date(appointment.createdAt).toLocaleString()}
                    </p>
                  </div>
                </div>

                {appointment.notes ? (
                  <div className="mt-6 rounded-2xl bg-zinc-50 p-4 text-sm text-zinc-700 dark:bg-zinc-900 dark:text-zinc-200">
                    <p className="font-semibold">Observações</p>
                    <p className="mt-2 whitespace-pre-wrap">{appointment.notes}</p>
                  </div>
                ) : null}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
