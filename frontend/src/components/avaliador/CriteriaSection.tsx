"use client";

import type { EvaluationCriterion } from "@/app/avaliacao/mockData";
import CriterionCard from "./CriterionCard";
import styles from "./ProjectEvaluation.module.css";

interface CriteriaSectionProps {
  criteria: EvaluationCriterion[];
  scores: Record<string, number>;
  onScoreChange: (criterionId: string, score: number) => void;
  disabled?: boolean;
}

export default function CriteriaSection({
  criteria,
  scores,
  onScoreChange,
  disabled = false,
}: CriteriaSectionProps) {
  const answeredCount = Object.keys(scores).filter((key) => scores[key] !== undefined && scores[key] > 0).length;
  const totalCount = criteria.length;

  return (
    <section className={styles.criteriaCard} aria-labelledby="criteria-heading">
      <div className={styles.criteriaHeader}>
        <div className={styles.criteriaHeaderTop}>
          <div className={styles.criteriaTitleWithIcon}>
            <span className={styles.criteriaStarIcon} aria-hidden="true">★</span>
            <h2 id="criteria-heading" className={styles.criteriaMainTitle}>
              Critérios de avaliação
            </h2>
          </div>
          <span className={styles.criteriaCounterRight}>
            {answeredCount}/{totalCount} respondidos
          </span>
        </div>

        <div className={styles.criteriaSubtitles}>
          <p className={styles.criteriaSubtitle}>
            Avalie de 1 a 5 cada critério.
          </p>
          <p className={styles.criteriaTip}>
            Passe o mouse, sobre uma nota para entender o que ela significa.
          </p>
        </div>
      </div>

      <div className={styles.criteriaList}>
        {criteria.map((criterion) => (
          <CriterionCard
            key={criterion.id}
            criterion={criterion}
            selectedScore={scores[criterion.id]}
            onSelectScore={(score) => onScoreChange(criterion.id, score)}
            disabled={disabled}
          />
        ))}
      </div>
    </section>
  );
}
