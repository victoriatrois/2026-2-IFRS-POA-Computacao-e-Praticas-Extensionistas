"use client";

import Image from "next/image";
import { useState, useMemo } from "react";
import AppHeader from "@/components/AppHeader";
import ProjectEvaluationPanel from "@/components/avaliador/ProjectEvaluationPanel";
import { evaluationCriteria } from "@/app/avaliacao/mockData";
import WorkSearch from "./WorkSearch";
import {
  evaluatorProjectsMock,
  type EvaluatorProject,
} from "./mockData";
import styles from "./page.module.css";

interface EvaluatorWorkspaceProps {
  initialDashboard: {
    evaluatorName: string;
    assignedCount: number;
    pendingCount: number;
    completedCount: number;
    dueDate: string;
    deadlineTime: string;
    eventStage: string;
  };
}

export default function EvaluatorWorkspace({ initialDashboard }: EvaluatorWorkspaceProps) {
  const [projects, setProjects] = useState<EvaluatorProject[]>(evaluatorProjectsMock);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [selectedProjectId, setSelectedProjectId] = useState<number | null>(null);
  const [scores, setScores] = useState<Record<number, Record<string, number>>>({});
  const [feedback, setFeedback] = useState<Record<number, string>>({});
  const [hasValidationError, setHasValidationError] = useState(false);
  const [submittedProjectIds, setSubmittedProjectIds] = useState<Set<number>>(() => {
    return new Set(
      evaluatorProjectsMock.filter((p) => p.status === "evaluated").map((p) => p.id)
    );
  });

  const selectedProject = useMemo(() => {
    if (!selectedProjectId) return null;
    return projects.find((p) => p.id === selectedProjectId) ?? null;
  }, [projects, selectedProjectId]);

  const completedCount = useMemo(() => {
    return projects.filter((p) => p.status === "evaluated").length;
  }, [projects]);

  const pendingCount = useMemo(() => {
    return projects.filter((p) => p.status === "pending").length;
  }, [projects]);

  const completionPercentage = Math.round(
    (completedCount / initialDashboard.assignedCount) * 100,
  );

  const initials = initialDashboard.evaluatorName
    .split(" ")
    .map((part) => part[0])
    .slice(0, 2)
    .join("")
    .toUpperCase();

  function handleOpenDrawer() {
    setIsDrawerOpen(true);
  }

  function handleCloseDrawer() {
    setIsDrawerOpen(false);
    setSelectedProjectId(null);
    setHasValidationError(false);
  }

  function handleSelectProject(project: EvaluatorProject) {
    setSelectedProjectId(project.id);
    setIsDrawerOpen(true);
    setHasValidationError(false);
  }

  function handleScoreChange(criterionId: string, score: number) {
    if (!selectedProjectId) return;
    setScores((prev) => ({
      ...prev,
      [selectedProjectId]: {
        ...(prev[selectedProjectId] || {}),
        [criterionId]: score,
      },
    }));
  }

  function handleFeedbackChange(text: string) {
    if (!selectedProjectId) return;
    setFeedback((prev) => ({
      ...prev,
      [selectedProjectId]: text,
    }));
  }

  function handleSubmitEvaluation() {
    if (!selectedProject) return;

    const currentScores = scores[selectedProject.id] || {};
    const allAnswered = evaluationCriteria.every(
      (criterion) => typeof currentScores[criterion.id] === "number" && currentScores[criterion.id] > 0
    );

    if (!allAnswered) {
      setHasValidationError(true);
      return;
    }

    setHasValidationError(false);
    setSubmittedProjectIds((prev) => new Set(prev).add(selectedProject.id));
    setProjects((prev) =>
      prev.map((p) => (p.id === selectedProject.id ? { ...p, status: "evaluated" } : p))
    );
  }

  return (
    <div className={styles.page}>
      <AppHeader variant="evaluator" userName={initialDashboard.evaluatorName} />

      {isDrawerOpen ? (
        <div className={styles.workspaceSplit}>
          <WorkSearch
            isOpen={true}
            onClose={handleCloseDrawer}
            selectedProjectId={selectedProjectId}
            onSelectProject={handleSelectProject}
            projects={projects}
            hideTriggerButton={true}
          />

          <main className={styles.evaluationMain}>
            <div className={styles.evaluationInner}>
              {selectedProject ? (
                <ProjectEvaluationPanel
                  project={selectedProject}
                  scores={scores[selectedProject.id] || {}}
                  onScoreChange={handleScoreChange}
                  feedback={feedback[selectedProject.id] || ""}
                  onFeedbackChange={handleFeedbackChange}
                  onSubmit={handleSubmitEvaluation}
                  hasValidationError={hasValidationError}
                  onDismissValidation={() => setHasValidationError(false)}
                  isSubmitted={submittedProjectIds.has(selectedProject.id)}
                />
              ) : (
                <div className={styles.emptySelectionPrompt}>
                  <p>Selecione um projeto na lista lateral para iniciar a avaliação.</p>
                </div>
              )}
            </div>
          </main>
        </div>
      ) : (
        <main className={styles.main}>
          <section className={styles.heroCard} aria-labelledby="welcome-title">
            <div className={styles.heroIntro}>
              <h1 id="welcome-title">Olá, {initialDashboard.evaluatorName}</h1>
              <p>
                Você está na {initialDashboard.eventStage}. Escolha um trabalho para iniciar sua avaliação.
              </p>
            </div>

            <div className={styles.stats} aria-label="Resumo das avaliações">
              <article className={`${styles.statCard} ${styles.assignedCard}`}>
                <strong className={styles.statNumber}>{initialDashboard.assignedCount}</strong>
                <div className={styles.statHeading}>
                  <h2>Trabalhos atribuídos</h2>
                  <span className={styles.smallAvatar} aria-hidden="true">{initials}</span>
                </div>
                <p className={styles.statCaption}>a você</p>
              </article>

              <article className={styles.statCard}>
                <strong className={styles.statNumber}>{pendingCount}</strong>
                <div className={styles.statHeading}>
                  <h2>Pendentes de avaliação</h2>
                  <Image src="/avaliador/clock.svg" alt="" width={18} height={18} />
                </div>
                <p className={styles.statCaption}>{completedCount} concluídos</p>
              </article>

              <article className={styles.statCard}>
                <strong className={`${styles.statNumber} ${styles.dateNumber}`}>
                  {initialDashboard.dueDate}
                </strong>
                <div className={styles.statHeading}>
                  <h2>Prazo final</h2>
                  <Image src="/avaliador/clock.svg" alt="" width={18} height={18} />
                </div>
                <p className={styles.statCaption}>{initialDashboard.deadlineTime}</p>
              </article>
            </div>

            <div className={styles.progressSection}>
              <div
                className={styles.progressTrack}
                role="progressbar"
                aria-label="Trabalhos concluídos"
                aria-valuemin={0}
                aria-valuemax={initialDashboard.assignedCount}
                aria-valuenow={completedCount}
              >
                <span style={{ width: `${completionPercentage}%` }} />
              </div>
              <p>{completedCount} de {initialDashboard.assignedCount} concluídos</p>
              <button
                className={styles.primaryButton}
                type="button"
                onClick={handleOpenDrawer}
              >
                Ver trabalhos
              </button>
            </div>
          </section>

          <section className={styles.guidanceCard} aria-labelledby="guidance-title">
            <h2 id="guidance-title">Como começar</h2>
            <ul>
              <li>
                <Image src="/avaliador/check.svg" alt="" width={16} height={16} />
                <span>Acesse a lista completa de trabalhos atribuídos para selecionar o primeiro projeto.</span>
              </li>
              <li>
                <Image src="/avaliador/check.svg" alt="" width={16} height={16} />
                <span>Utilize os filtros para priorizar os pendentes e organizar sua sessão de avaliação.</span>
              </li>
              <li>
                <Image src="/avaliador/check.svg" alt="" width={16} height={16} />
                <span>Escolha um trabalho na lista lateral para abrir seu formulário de avaliação.</span>
              </li>
            </ul>
          </section>
        </main>
      )}
    </div>
  );
}
