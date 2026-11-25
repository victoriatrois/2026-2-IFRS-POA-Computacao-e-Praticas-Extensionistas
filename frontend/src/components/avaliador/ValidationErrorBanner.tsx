"use client";

import styles from "./ProjectEvaluation.module.css";

interface ValidationErrorBannerProps {
  onDismiss: () => void;
}

export default function ValidationErrorBanner({ onDismiss }: ValidationErrorBannerProps) {
  return (
    <div className={styles.validationWarning} role="alert" aria-live="assertive">
      <div className={styles.warningIconWrapper} aria-hidden="true">
        <svg
          width="20"
          height="20"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z" />
          <line x1="12" y1="9" x2="12" y2="13" />
          <line x1="12" y1="17" x2="12.01" y2="17" />
        </svg>
      </div>

      <div className={styles.warningContent}>
        <h3 className={styles.warningTitle}>Há critérios com avaliação pendente</h3>
        <p className={styles.warningDescription}>Para enviar a avaliação, avalie todos os critérios.</p>
      </div>

      <button
        type="button"
        className={styles.closeWarningButton}
        onClick={onDismiss}
        aria-label="Fechar aviso de validação"
      >
        ✕
      </button>
    </div>
  );
}
