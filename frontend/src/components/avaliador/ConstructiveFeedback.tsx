"use client";

import styles from "./ProjectEvaluation.module.css";

interface ConstructiveFeedbackProps {
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;
}

export default function ConstructiveFeedback({
  value,
  onChange,
  disabled = false,
}: ConstructiveFeedbackProps) {
  return (
    <section className={styles.feedbackCard} aria-labelledby="feedback-heading">
      <h2 id="feedback-heading" className={styles.feedbackTitle}>
        Feedback construtivo
      </h2>
      <p className={styles.feedbackSubtitle}>
        Compartilhe sugestões, pontos positivos e críticas para o desenvolvimento do projeto.
      </p>

      <textarea
        className={styles.feedbackTextarea}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder="Escreva um feedback construtivo para a equipe."
        disabled={disabled}
        aria-label="Feedback construtivo para a equipe"
      />
    </section>
  );
}
