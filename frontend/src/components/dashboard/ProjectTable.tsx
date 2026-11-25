"use client";

import ProjectRow, { type DashboardProjectItem } from "./ProjectRow";
import styles from "./DashboardRefined.module.css";

interface ProjectTableProps {
  projects: DashboardProjectItem[];
  onViewDiagnostic?: (projectId: number) => void;
}

export default function ProjectTable({ projects, onViewDiagnostic }: ProjectTableProps) {
  return (
    <div className={styles.tableCard}>
      <div className={styles.tableContainer}>
        <table className={styles.projectsTable}>
          <thead className={styles.tableHeader}>
            <tr>
              <th scope="col" style={{ width: "90px" }}>Nível</th>
              <th scope="col">Projeto</th>
              <th scope="col" style={{ width: "210px" }}>Status do Projeto</th>
              <th scope="col" style={{ width: "180px", textAlign: "right" }}>Ações</th>
            </tr>
          </thead>
          <tbody>
            {projects.map((project) => (
              <ProjectRow
                key={project.id}
                project={project}
                onViewDiagnostic={onViewDiagnostic}
              />
            ))}
          </tbody>
        </table>

        {projects.length === 0 && (
          <div className={styles.emptyProjectsMessage}>
            Nenhum projeto encontrado para o filtro e busca aplicados.
          </div>
        )}
      </div>
    </div>
  );
}
