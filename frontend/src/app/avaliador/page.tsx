import type { Metadata } from "next";
import EvaluatorWorkspace from "./EvaluatorWorkspace";
import { evaluatorDashboardMock as dashboard } from "./mockData";

export const metadata: Metadata = {
  title: "Início do avaliador | MNR",
  description: "Resumo das avaliações atribuídas ao avaliador da MNR.",
};

export default function EvaluatorHomePage() {
  return <EvaluatorWorkspace initialDashboard={dashboard} />;
}
