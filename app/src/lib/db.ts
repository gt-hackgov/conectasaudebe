import fs from "fs";
import path from "path";

const dbPath = path.join(process.cwd(), "db.json");

export type Appointment = {
  id?: string;
  date: string;
  time: string;
  location: string;
  specialty: string;
  notes: string;
  createdAt: string;
};

export type NotificationItem = {
  id: string;
  title: string;
  message: string;
  time: string;
};

export type Database = {
  appointments: Appointment[];
  notifications: NotificationItem[];
};

const defaultDb: Database = {
  appointments: [],
  notifications: [
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
  ],
};

export const getDb = (): Database => {
  if (!fs.existsSync(dbPath)) {
    fs.writeFileSync(dbPath, JSON.stringify(defaultDb, null, 2));
    return defaultDb;
  }
  try {
    const data = fs.readFileSync(dbPath, "utf-8");
    return JSON.parse(data);
  } catch (error) {
    return defaultDb;
  }
};

export const saveDb = (data: Database) => {
  fs.writeFileSync(dbPath, JSON.stringify(data, null, 2));
};
