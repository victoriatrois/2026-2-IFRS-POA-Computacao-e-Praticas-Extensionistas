"use client";

import styles from "./DashboardRefined.module.css";

export type MetricType = "processed" | "validated" | "pending" | "rejected";

interface MetricCardProps {
  type: MetricType;
  title: string;
  value: number | string;
}

export default function MetricCard({ type, title, value }: MetricCardProps) {
  const cardClass = {
    processed: styles.metricCardProcessed,
    validated: styles.metricCardValidated,
    pending: styles.metricCardPending,
    rejected: styles.metricCardRejected,
  }[type];

  const iconCircleClass = {
    processed: styles.metricIconProcessed,
    validated: styles.metricIconValidated,
    pending: styles.metricIconPending,
    rejected: styles.metricIconRejected,
  }[type];

  const valueClass = {
    processed: "",
    validated: styles.metricValueValidated,
    pending: "",
    rejected: styles.metricValueRejected,
  }[type];

  return (
    <article className={`${styles.metricCard} ${cardClass}`}>
      <div className={`${styles.metricIconCircle} ${iconCircleClass}`} aria-hidden="true">
        {type === "processed" && (
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
            <line x1="16" y1="13" x2="8" y2="13" />
            <line x1="16" y1="17" x2="8" y2="17" />
            <line x1="10" y1="9" x2="8" y2="9" />
          </svg>
        )}
        {type === "validated" && (
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="20 6 9 17 4 12" />
          </svg>
        )}
        {type === "pending" && (
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="12" cy="12" r="10" />
            <polyline points="12 6 12 12 16 14" />
          </svg>
        )}
        {type === "rejected" && (
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        )}
      </div>

      <div className={styles.metricContent}>
        <h3 className={styles.metricTitle}>{title}</h3>
        <p className={`${styles.metricValue} ${valueClass}`}>{value}</p>
      </div>
    </article>
  );
}
