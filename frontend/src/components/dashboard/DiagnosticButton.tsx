"use client";

import styles from "./DashboardRefined.module.css";

/**
 * ============================================================================
 * ATENÇÃO TAYLAN (PR / Issue #91):
 * Este botão "Ver diagnóstico" é o ponto de entrada para a tela de diagnóstico
 * de pré-avaliação que você está implementando na Issue #91.
 * 
 * Conecte a ação de clique ou navegação para a sua tela aqui:
 * (ex: router.push(`/dashboard/diagnostico/${projectId}`) ou abertura de modal).
 * ============================================================================
 */

interface DiagnosticButtonProps {
  projectId: number;
  onClick?: (projectId: number) => void;
}

export default function DiagnosticButton({ projectId, onClick }: DiagnosticButtonProps) {
  function handleClick() {
    if (onClick) {
      onClick(projectId);
    } else {
      // Placeholder informativo até a integração da tela da Issue #91
      alert(`[Issue #91 - Em desenvolvimento por Taylan]: Abrir diagnóstico do projeto ID ${projectId}`);
    }
  }

  return (
    <button
      type="button"
      className={styles.diagnosticButton}
      onClick={handleClick}
      aria-label={`Ver diagnóstico do projeto ${projectId}`}
    >
      <svg
        className={styles.diagnosticIcon}
        width="16"
        height="16"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden="true"
      >
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
        <polyline points="14 2 14 8 20 8" />
        <line x1="16" y1="13" x2="8" y2="13" />
        <line x1="16" y1="17" x2="8" y2="17" />
        <polyline points="10 9 9 9 8 9" />
      </svg>
      Ver diagnóstico
    </button>
  );
}
