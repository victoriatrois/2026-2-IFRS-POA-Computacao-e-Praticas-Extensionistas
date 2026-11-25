"use client";

import MetricCard from "./MetricCard";
import styles from "./DashboardRefined.module.css";

interface DashboardMetricsProps {
  totalProcessed: number;
  totalValidated: number;
  totalPending: number;
  totalRejected: number;
}

export default function DashboardMetrics({
  totalProcessed,
  totalValidated,
  totalPending,
  totalRejected,
}: DashboardMetricsProps) {
  return (
    <section className={styles.metricsGrid} aria-label="Indicadores gerais de pré-avaliação">
      <MetricCard
        type="processed"
        title="Total Processados"
        value={totalProcessed}
      />
      <MetricCard
        type="validated"
        title="Total Validados"
        value={totalValidated}
      />
      <MetricCard
        type="pending"
        title="Pendentes de Revisão"
        value={totalPending}
      />
      <MetricCard
        type="rejected"
        title="Reprovados"
        value={totalRejected}
      />
    </section>
  );
}
