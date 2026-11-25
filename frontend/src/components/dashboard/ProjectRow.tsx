"use client";

import LevelBadge from "./LevelBadge";
import StatusBadge, { type ProjectEvaluationStatus } from "./StatusBadge";
import DiagnosticButton from "./DiagnosticButton";
import styles from "./DashboardRefined.module.css";

export interface DashboardProjectItem {
  id: number;
  name: string;
  subtitle?: string | null;
  level?: string | null;
  status: ProjectEvaluationStatus;
  participantName?: string | null;
  participantCpf?: string | null;
  institutionName?: string | null;
}

interface ProjectRowProps {
  project: DashboardProjectItem;
  onViewDiagnostic?: (projectId: number) => void;
}

export default function ProjectRow({ project, onViewDiagnostic }: ProjectRowProps) {
  return (
    <tr className={styles.tableRow}>
      <td className={styles.tableCell} style={{ width: "90px" }}>
        <LevelBadge level={project.level} />
      </td>

      <td className={styles.tableCell}>
        <div className={styles.projectInfo}>
          <h4 className={styles.projectTitle}>{project.name}</h4>
          {project.subtitle && (
            <p className={styles.projectSubtitle}>{project.subtitle}</p>
          )}
        </div>
      </td>

      <td className={styles.tableCell} style={{ width: "210px" }}>
        <StatusBadge status={project.status} />
      </td>

      <td className={styles.tableCell} style={{ width: "180px", textAlign: "right" }}>
        <DiagnosticButton
          projectId={project.id}
          onClick={onViewDiagnostic}
        />
      </td>
    </tr>
  );
}
