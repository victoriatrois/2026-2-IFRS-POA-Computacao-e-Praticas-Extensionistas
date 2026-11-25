"use client";

import styles from "./ProjectEvaluation.module.css";

interface EvaluationFooterProps {
  average: string;
  onSubmit: () => void;
  isSubmitted?: boolean;
}

export default function EvaluationFooter({
  average,
  onSubmit,
  isSubmitted = false,
}: EvaluationFooterProps) {
  return (
    <footer className={styles.footerContainer}>
      <div className={styles.evaluationSummary}>
        <span>Média da avaliação:</span>
        <strong className={styles.averageScoreBadge}>{average}</strong>
      </div>

      <button
        type="button"
        className={`${styles.submitEvaluationButton} ${isSubmitted ? styles.submittedButton : ""}`}
        onClick={onSubmit}
        disabled={isSubmitted}
      >
        {isSubmitted ? "✓ Avaliação enviada" : "Enviar avaliação"}
      </button>
    </footer>
  );
}
