"use client";

import { useMemo, useRef, useEffect } from "react";
import type { EvaluatorProject } from "@/app/avaliador/mockData";
import { evaluationCriteria } from "@/app/avaliacao/mockData";
import ValidationErrorBanner from "./ValidationErrorBanner";
import ProjectHeader from "./ProjectHeader";
import CriteriaSection from "./CriteriaSection";
import ConstructiveFeedback from "./ConstructiveFeedback";
import EvaluationFooter from "./EvaluationFooter";
import styles from "./ProjectEvaluation.module.css";

interface ProjectEvaluationPanelProps {
  project: EvaluatorProject;
  scores: Record<string, number>;
  onScoreChange: (criterionId: string, score: number) => void;
  feedback: string;
  onFeedbackChange: (feedback: string) => void;
  onSubmit: () => void;
  hasValidationError: boolean;
  onDismissValidation: () => void;
  isSubmitted: boolean;
}

export default function ProjectEvaluationPanel({
  project,
  scores,
  onScoreChange,
  feedback,
  onFeedbackChange,
  onSubmit,
  hasValidationError,
  onDismissValidation,
  isSubmitted,
}: ProjectEvaluationPanelProps) {
  const panelRef = useRef<HTMLDivElement>(null);

  const answeredCount = useMemo(() => {
    return Object.keys(scores).filter((id) => scores[id] !== undefined && scores[id] > 0).length;
  }, [scores]);

  const average = useMemo(() => {
    if (answeredCount === 0) return "—";
    const sum = Object.values(scores).reduce((acc, curr) => acc + (curr || 0), 0);
    return (sum / answeredCount).toFixed(1);
  }, [scores, answeredCount]);

  useEffect(() => {
    if (hasValidationError && panelRef.current) {
      panelRef.current.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  }, [hasValidationError]);

  return (
    <div className={styles.evaluationPanel} ref={panelRef}>
      {hasValidationError && (
        <ValidationErrorBanner onDismiss={onDismissValidation} />
      )}

      {isSubmitted && (
        <div className={styles.successBanner} role="status">
          <span aria-hidden="true">✓</span> Avaliação concluída para este projeto nesta sessão!
        </div>
      )}

      <ProjectHeader
        project={project}
        answeredCount={answeredCount}
        totalCriteria={evaluationCriteria.length}
      />

      <CriteriaSection
        criteria={evaluationCriteria}
        scores={scores}
        onScoreChange={onScoreChange}
        disabled={isSubmitted}
      />

      <ConstructiveFeedback
        value={feedback}
        onChange={onFeedbackChange}
        disabled={isSubmitted}
      />

      <EvaluationFooter
        average={average}
        onSubmit={onSubmit}
        isSubmitted={isSubmitted}
      />
    </div>
  );
}
