"use client";

import { useEffect, useMemo, useState, useRef } from "react";
import { useRouter } from "next/navigation";
import { FeatureCard } from "@/components/FeatureCard";
import { ChatModal } from "@/components/ChatModal";
import { Queue } from "@/lib/dataStructures";

type NotificationItem = {
  id: string;
  title: string;
  message: string;
  time: string;
};

const INITIAL_NOTIFICATIONS: NotificationItem[] = [
  {
    id: "notif-1",
    title: "Previna-se da Dengue!",
    message: "Elimine focos de água parada. Converse com nossa assistente virtual para saber os sintomas e cuidados.",
    time: "Agora",
  },
  {
    id: "notif-2",
    title: "Lembrete de Consulta",
    message: "Sua consulta agendada está próxima. Chegue com 15 minutos de antecedência na UBS.",
    time: "Há 1 hora",
  },
  {
    id: "notif-3",
    title: "Campanha de Vacinação",
    message: "Vacinação contra Influenza disponível em todas as UBS do município.",
    time: "Ontem",
  },
];

export default function DashboardPage() {
  const router = useRouter();
  const [userName] = useState<string | null>(() => {
    if (typeof window === "undefined") return null;
    const raw = localStorage.getItem("saudeDigitalUser");
    if (!raw) return null;

    try {
      const user = JSON.parse(raw);
      return user.username ?? "Usuário";
    } catch {
      return null;
    }
  });

  const [isChatOpen, setIsChatOpen] = useState(false);
  const [notificationQueue] = useState<Queue<NotificationItem>>(() => Queue.fromArray(INITIAL_NOTIFICATIONS));
  const [notificationsArray, setNotificationsArray] = useState<NotificationItem[]>(() => INITIAL_NOTIFICATIONS);
  const [selectedNotification, setSelectedNotification] = useState<NotificationItem | null>(null);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!userName) {
      router.replace("/");
    }
  }, [router, userName]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setDropdownOpen(false);
      }
    };
    if (dropdownOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    }
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [dropdownOpen]);

  const welcomeMessage = useMemo(() => {
    if (!userName) return "Olá";
    return `Olá, ${userName.split("@")[0]}!`;
  }, [userName]);

  const displayName = useMemo(() => {
    if (!userName) return "Usuário";
    return userName.split("@")[0];
  }, [userName]);

  const handleLogout = () => {
    localStorage.removeItem("saudeDigitalUser");
    router.push("/");
  };

  const handleAction = (action: string) => {
    alert(`Função: ${action} (demo)`);
  };

  const handleDequeueNotification = () => {
    notificationQueue.dequeue();
    setNotificationsArray(notificationQueue.toArray());
  };

  const handleOpenNotification = (notif: NotificationItem) => {
    setSelectedNotification(notif);
    setDropdownOpen(false);
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-white via-indigo-50 to-white px-6 py-10">
      <div className="mx-auto w-full max-w-6xl">
        <header className="relative flex flex-col gap-6 rounded-3xl bg-white p-8 shadow-lg dark:bg-zinc-950">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <p className="text-sm font-semibold text-indigo-600">Painel do usuário</p>
              <h1 className="text-3xl font-semibold text-zinc-900 dark:text-zinc-50">{welcomeMessage}</h1>
              <p className="mt-2 max-w-xl text-sm text-zinc-600 dark:text-zinc-400">
                Selecione uma opção abaixo para continuar.
              </p>
            </div>

            <div className="flex items-center gap-3">
              <div className="relative">
                <button
                  type="button"
                  onClick={() => setDropdownOpen(!dropdownOpen)}
                  className="relative rounded-xl border border-zinc-200 bg-white p-2 text-sm shadow-sm transition hover:bg-zinc-50 dark:border-zinc-800 dark:bg-zinc-950 dark:text-zinc-100 dark:hover:bg-zinc-900"
                  aria-label="Notificações"
                >
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 2C10.896 2 10 2.896 10 4V5.586L7.707 8.293C7.512 8.488 7.256 8.744 7.061 9.061L4.707 11.414C4.512 11.609 4.256 11.865 4.061 12.182L2 14.182V16H22V14.182L19.939 12.182C19.744 11.865 19.488 11.609 19.293 11.414L16.939 9.061C16.744 8.744 16.488 8.488 16.293 8.293L14 5.586V4C14 2.896 13.104 2 12 2Z" fill="currentColor"/>
                    <path d="M12 22C13.1046 22 14 21.1046 14 20H10C10 21.1046 10.8954 22 12 22Z" fill="currentColor"/>
                  </svg>
                  {notificationsArray.length > 0 && (
                    <span className="absolute -right-1 -top-1 flex h-5 w-5 items-center justify-center rounded-full bg-indigo-600 text-[10px] font-bold text-white">
                      {notificationsArray.length}
                    </span>
                  )}
                </button>

                {dropdownOpen && (
                  <div
                    ref={dropdownRef}
                    className="absolute right-0 z-30 mt-2 w-80 rounded-2xl border border-zinc-200 bg-white shadow-xl dark:border-zinc-800 dark:bg-zinc-900"
                  >
                    <div className="flex items-center justify-between border-b border-zinc-100 p-4 dark:border-zinc-800">
                      <p className="text-sm font-semibold text-zinc-900 dark:text-zinc-100">Fila de Notificações</p>
                      {notificationsArray.length > 0 && (
                        <button
                          type="button"
                          onClick={handleDequeueNotification}
                          className="text-xs font-semibold text-indigo-600 hover:text-indigo-700 dark:text-indigo-400"
                        >
                          Ler primeira (Dequeue)
                        </button>
                      )}
                    </div>

                    {notificationsArray.length === 0 ? (
                      <div className="p-4 text-center text-sm text-zinc-500">
                        Nenhuma notificação pendente.
                      </div>
                    ) : (
                      <ul className="max-h-72 divide-y divide-zinc-100 overflow-y-auto dark:divide-zinc-800">
                        {notificationsArray.map((notif, index) => (
                          <li key={notif.id}>
                            <button
                              type="button"
                              onClick={() => handleOpenNotification(notif)}
                              className="w-full p-4 text-left transition hover:bg-zinc-50 dark:hover:bg-zinc-800/50"
                            >
                              <div className="flex items-center justify-between">
                                <span className="text-xs font-bold text-indigo-600">
                                  {index === 0 ? "Próxima (FIFO)" : `Item ${index + 1}`}
                                </span>
                                <span className="text-[10px] text-zinc-400">{notif.time}</span>
                              </div>
                              <p className="mt-1 text-sm font-semibold text-zinc-900 dark:text-zinc-100">
                                {notif.title}
                              </p>
                            </button>
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                )}
              </div>

              <button
                type="button"
                onClick={handleLogout}
                className="rounded-xl border border-zinc-200 bg-white px-4 py-2 text-sm font-semibold text-zinc-700 shadow-sm transition hover:bg-zinc-50 dark:border-zinc-800 dark:bg-zinc-950 dark:text-zinc-100 dark:hover:bg-zinc-900"
              >
                Sair
              </button>
            </div>
          </div>
        </header>

        <section className="mt-10 grid gap-6 md:grid-cols-2">
          <FeatureCard
            title="Agendar consulta"
            description="Escolha data e horário para sua próxima consulta."
            onClick={() => router.push("/agendar-consulta")}
            icon={<span className="text-lg">🩺</span>}
          />
          <FeatureCard
            title="Consultas agendadas"
            description="Veja suas consultas marcadas e histórico de agendamentos."
            onClick={() => router.push("/consultas-agendadas")}
            icon={<span className="text-lg">📅</span>}
          />
          <FeatureCard
            title="Agendar exame"
            description="Escolha o tipo de exame e o local de coleta."
            onClick={() => handleAction("Agendar exame")}
            icon={<span className="text-lg">🧪</span>}
          />
          <FeatureCard
            title="Resultado de exames"
            description="Veja os resultados dos exames já realizados."
            onClick={() => handleAction("Resultado de exames")}
            icon={<span className="text-lg">📄</span>}
          />
          <FeatureCard
            title="Procurar UBS mais próxima"
            description="Encontre a unidade básica de saúde mais próxima de você."
            onClick={() => handleAction("Procurar UBS mais próxima")}
            icon={<span className="text-lg">📍</span>}
          />
        </section>
      </div>

      <div className="fixed bottom-6 right-6 z-40">
        <button
          onClick={() => setIsChatOpen(true)}
          className="rounded-full bg-indigo-600 p-4 text-white shadow-lg transition hover:bg-indigo-700"
          aria-label="Abrir chat"
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C6.48 2 2 6.48 2 12c0 1.54.36 3.05 1.05 4.42L2 22l5.58-1.05C9.95 21.64 11.46 22 13 22h7c1.1 0 2-.9 2-2V12c0-5.52-4.48-10-10-10z" fill="currentColor"/>
          </svg>
        </button>
      </div>

      {selectedNotification && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-xl dark:bg-zinc-900">
            <h3 className="text-lg font-semibold text-zinc-900 dark:text-zinc-50">{selectedNotification.title}</h3>
            <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
              {selectedNotification.message}
            </p>
            {selectedNotification.id === "notif-1" && (
              <div className="mt-3">
                <button
                  type="button"
                  onClick={() => {
                    setIsChatOpen(true);
                    setSelectedNotification(null);
                  }}
                  className="text-sm font-semibold text-indigo-600 underline hover:no-underline"
                >
                  Abrir Assistente Virtual
                </button>
              </div>
            )}
            <div className="mt-6 flex justify-end gap-2">
              <button
                type="button"
                onClick={() => setSelectedNotification(null)}
                className="rounded-xl bg-zinc-200 px-4 py-2 text-sm font-semibold text-zinc-700 hover:bg-zinc-300 dark:bg-zinc-700 dark:text-zinc-200 dark:hover:bg-zinc-600"
              >
                Fechar
              </button>
            </div>
          </div>
        </div>
      )}

      <ChatModal open={isChatOpen} onClose={() => setIsChatOpen(false)} userName={displayName} />
    </div>
  );
}
