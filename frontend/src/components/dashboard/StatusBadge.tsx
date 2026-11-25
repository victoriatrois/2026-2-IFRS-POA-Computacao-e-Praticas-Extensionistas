"use client";

import styles from "./DashboardRefined.module.css";

export type ProjectEvaluationStatus = "pending" | "validated" | "rejected";

interface StatusBadgeProps {
  status: ProjectEvaluationStatus;
}

export default function StatusBadge({ status }: StatusBadgeProps) {
  if (status === "validated") {
    return (
      <span className={`${styles.statusBadge} ${styles.statusValidated}`}>
        <svg
          width="14"
          height="14"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2.5"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <polyline points="20 6 9 17 4 12" />
        </svg>
        Validação Concluída
      </span>
    );
  }

  if (status === "rejected") {
    return (
      <span className={`${styles.statusBadge} ${styles.statusRejected}`}>
        <svg
          width="14"
          height="14"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2.5"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </svg>
        Validação Reprovada
      </span>
    );
  }

  return (
    <span className={`${styles.statusBadge} ${styles.statusPending}`}>
      <svg
        width="14"
        height="14"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2.2"
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden="true"
      >
        <circle cx="12" cy="12" r="10" />
        <polyline points="12 6 12 12 16 14" />
      </svg>
      Validação Pendente
    </span>
  );
}
