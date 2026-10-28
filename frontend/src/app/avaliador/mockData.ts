// These demo values are fake and should be removed when real evaluator data is connected.
export const evaluatorDashboardMock = {
  evaluatorName: "Ana Costa",
  assignedCount: 12,
  pendingCount: 8,
  completedCount: 4,
  dueDate: "15/10/2026",
  deadlineTime: "Até 18h (Brasília)",
  eventStage: "etapa classificatória da MNR 2026",
};

export type EvaluatorProjectStatus = "evaluated" | "pending";

export type EvaluatorProject = {
  id: number;
  level: string;
  title: string;
  team: string;
  location: string;
  status: EvaluatorProjectStatus;
};

// TODO: Remove these fake projects when the evaluator search is connected to real data.
export const evaluatorProjectsMock: EvaluatorProject[] = [
  { id: 1, level: "N1", title: "Robô de Precisão para Agricultura Familiar", team: "AgroTech Júnior", location: "Bento Gonçalves/RS", status: "evaluated" },
  { id: 2, level: "N2", title: "Impressora Braille de Baixo Custo", team: "InkTech", location: "Carambeí/PR", status: "pending" },
  { id: 3, level: "N2", title: "Monitor Autônomo de Qualidade da Água", team: "MarTech Robótica", location: "Contagem/MG", status: "pending" },
  { id: 4, level: "N3", title: "IA para Otimização de Semáforos", team: "SmartCity Makers", location: "Foz do Iguaçu/PR", status: "pending" },
  { id: 5, level: "N2", title: "Classificador Automatizado de Resíduos", team: "GreenBots", location: "Chapecó/SC", status: "pending" },
  { id: 6, level: "N1", title: "Estufa Inteligente de Baixo Custo", team: "AgroLab", location: "Caxias do Sul/RS", status: "evaluated" },
  { id: 7, level: "N2", title: "Aplicativo de Acessibilidade Escolar", team: "IncluiTech", location: "Curitiba/PR", status: "evaluated" },
  { id: 8, level: "N3", title: "Monitoramento de Nascentes", team: "EcoTrack", location: "Londrina/PR", status: "evaluated" },
  { id: 9, level: "N1", title: "Horta Automatizada para Escolas", team: "Cultiva Júnior", location: "Passo Fundo/RS", status: "pending" },
  { id: 10, level: "N2", title: "Alerta de Enchentes Comunitário", team: "SafeCity", location: "Blumenau/SC", status: "pending" },
  { id: 11, level: "N3", title: "Reuso de Água em Residências", team: "BlueFuture", location: "Maringá/PR", status: "pending" },
  { id: 12, level: "N2", title: "Leitor de Rótulos para Baixa Visão", team: "VisioTech", location: "Porto Alegre/RS", status: "pending" },
];
