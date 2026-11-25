"use client";

import type { EvaluatorProject } from "@/app/avaliador/mockData";
import styles from "./ProjectEvaluation.module.css";

interface ProjectHeaderProps {
  project: EvaluatorProject;
  answeredCount: number;
  totalCriteria: number;
}

export default function ProjectHeader({
  project,
  answeredCount,
  totalCriteria,
}: ProjectHeaderProps) {
  const progressPercent = totalCriteria > 0 ? Math.round((answeredCount / totalCriteria) * 100) : 0;
  const institution = project.institution || "IFRS Campus Bento Gonçalves";
  const displayTeamAndInstitution = project.team ? `${project.team} — ${institution}` : institution;

  return (
    <header className={styles.headerCard} aria-labelledby="project-title">
      <h1 id="project-title" className={styles.projectTitle}>
        {project.title}
      </h1>

      <div className={styles.metaInfo}>
        <span className={styles.projectLevel}>{project.level}</span>
        <p className={styles.projectTeamInstitution}>{displayTeamAndInstitution}</p>
        <p className={styles.projectLocation}>{project.location}</p>
      </div>

      <div className={styles.headerActionsRow}>
        <div className={styles.actionButtonsGroup}>
          <a
            href={project.videoUrl || "https://www.youtube.com/"}
            target="_blank"
            rel="noopener noreferrer"
            className={styles.materialLinkButton}
          >
            <span aria-hidden="true">▶</span> Ver vídeo
          </a>
          <a
            href={project.pdfUrl || "https://example.com/resumo.pdf"}
            target="_blank"
            rel="noopener noreferrer"
            className={styles.materialLinkButton}
          >
            <span aria-hidden="true">☰</span> Ver resumo/artigo
          </a>
        </div>

        <div className={styles.headerProgressSection}>
          <span className={styles.progressLabel}>
            {answeredCount}/{totalCriteria} respondidos
          </span>
          <div
            className={styles.progressBarTrack}
            role="progressbar"
            aria-label="Progresso da avaliação"
            aria-valuemin={0}
            aria-valuemax={totalCriteria}
            aria-valuenow={answeredCount}
          >
            <div
              className={styles.progressBarFill}
              style={{ width: `${progressPercent}%` }}
            />
          </div>
        </div>
      </div>
    </header>
  );
}
