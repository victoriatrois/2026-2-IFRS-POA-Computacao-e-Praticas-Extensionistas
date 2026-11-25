"use client";

import { useState } from "react";
import type { EvaluationCriterion, RubricLevel } from "@/app/avaliacao/mockData";
import styles from "./ProjectEvaluation.module.css";

interface CriterionCardProps {
  criterion: EvaluationCriterion;
  selectedScore: number | undefined;
  onSelectScore: (score: number) => void;
  disabled?: boolean;
}

export default function CriterionCard({
  criterion,
  selectedScore,
  onSelectScore,
  disabled = false,
}: CriterionCardProps) {
  const [hoveredLevel, setHoveredLevel] = useState<RubricLevel | null>(null);

  const activeLevel =
    hoveredLevel ||
    criterion.levels.find((l) => l.value === selectedScore) ||
    null;

  return (
    <article
      className={styles.criterionCard}
      aria-labelledby={`criterion-${criterion.id}-title`}
      onMouseLeave={() => setHoveredLevel(null)}
    >
      <div className={styles.criterionTitleRow}>
        <span className={styles.criterionBadge}>Q{criterion.question}</span>
        <div className={styles.criterionHeaderInfo}>
          <h3 id={`criterion-${criterion.id}-title`} className={styles.criterionTitle}>
            {criterion.title}
          </h3>
          <p className={styles.criterionPrompt}>{criterion.prompt}</p>
        </div>
      </div>

      <div
        className={styles.levelGrid}
        role="radiogroup"
        aria-label={`Nota para ${criterion.title}`}
      >
        {criterion.levels.map((level) => {
          const isSelected = selectedScore === level.value;
          return (
            <button
              key={level.value}
              type="button"
              role="radio"
              aria-checked={isSelected}
              disabled={disabled}
              className={`${styles.levelOption} ${isSelected ? styles.levelSelected : ""}`}
              onMouseEnter={() => setHoveredLevel(level)}
              onFocus={() => setHoveredLevel(level)}
              onBlur={() => setHoveredLevel(null)}
              onClick={() => onSelectScore(level.value)}
            >
              <span className={styles.levelNumber}>{level.value}</span>
              <span className={styles.levelLabel}>{level.label}</span>
            </button>
          );
        })}
      </div>

      {activeLevel && (
        <div
          id={`guidance-${criterion.id}`}
          className={styles.guidancePanel}
          role="note"
          aria-live="polite"
        >
          <strong>
            {activeLevel.value} · {activeLevel.label}
          </strong>
          <p>{activeLevel.description}</p>
        </div>
      )}
    </article>
  );
}
