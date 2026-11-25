"use client";

import Image from "next/image";
import { useMemo, useState } from "react";
import {
  evaluatorProjectsMock,
  type EvaluatorProject,
  type EvaluatorProjectStatus,
} from "./mockData";
import styles from "./page.module.css";

type ProjectFilter = "all" | EvaluatorProjectStatus;

const filters: { value: ProjectFilter; label: string }[] = [
  { value: "all", label: "Todos" },
  { value: "pending", label: "Pendentes" },
  { value: "evaluated", label: "Concluídos" },
];

interface WorkSearchProps {
  isOpen?: boolean;
  onClose?: () => void;
  selectedProjectId?: number | null;
  onSelectProject?: (project: EvaluatorProject) => void;
  projects?: EvaluatorProject[];
  hideTriggerButton?: boolean;
}

export default function WorkSearch({
  isOpen: controlledIsOpen,
  onClose: controlledOnClose,
  selectedProjectId,
  onSelectProject,
  projects = evaluatorProjectsMock,
  hideTriggerButton = false,
}: WorkSearchProps) {
  const [internalIsOpen, setInternalIsOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [filter, setFilter] = useState<ProjectFilter>("all");

  const isControlled = typeof controlledIsOpen === "boolean";
  const isOpen = isControlled ? controlledIsOpen : internalIsOpen;

  const visibleProjects = useMemo(() => {
    const term = searchTerm.trim().toLocaleLowerCase("pt-BR");
    return projects.filter((project) => {
      const matchesFilter = filter === "all" || project.status === filter;
      const searchableText = `${project.title} ${project.level} ${project.team} ${project.location}`.toLocaleLowerCase("pt-BR");
      const matchesSearch = !term || searchableText.includes(term);
      return matchesFilter && matchesSearch;
    });
  }, [filter, projects, searchTerm]);

  function openSearch() {
    setSearchTerm("");
    setFilter("all");
    if (!isControlled) {
      setInternalIsOpen(true);
    }
  }

  function handleClose() {
    setSearchTerm("");
    setFilter("all");
    if (controlledOnClose) {
      controlledOnClose();
    } else {
      setInternalIsOpen(false);
    }
  }

  function handleSelect(project: EvaluatorProject) {
    if (onSelectProject) {
      onSelectProject(project);
    }
  }

  return (
    <>
      {!hideTriggerButton && (
        <button className={styles.primaryButton} type="button" onClick={openSearch}>
          Ver trabalhos
        </button>
      )}

      {isOpen && (
        <aside className={styles.searchDrawer} aria-label="Busca e filtro de trabalhos">
          <div className={styles.drawerHeader}>
            <button className={styles.backButton} type="button" onClick={handleClose}>
              <Image src="/avaliador/arrow-left.svg" alt="" width={16} height={16} />
              Voltar
            </button>
          </div>

          <div className={styles.drawerControls}>
            <label className={styles.controlLabel} htmlFor="work-search">Busca</label>
            <p className={styles.controlDescription}>Procure projetos por título ou nível</p>
            <input
              className={styles.searchInput}
              id="work-search"
              type="search"
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              placeholder="Digite o título ou nível..."
              aria-label="Buscar projetos por título ou nível"
            />

            <span className={styles.controlLabel}>Filtro</span>
            <p className={styles.controlDescription}>Filtre projetos por status de avaliação</p>
            <div className={styles.filterOptions} role="group" aria-label="Status da avaliação">
              {filters.map((option) => (
                <button
                  key={option.value}
                  className={`${styles.filterOption} ${filter === option.value ? styles.filterOptionActive : ""}`}
                  type="button"
                  aria-pressed={filter === option.value}
                  onClick={() => setFilter(option.value)}
                >
                  {option.label}
                </button>
              ))}
            </div>
          </div>

          <div className={styles.projectList} aria-live="polite">
            {visibleProjects.map((project) => {
              const isSelected = selectedProjectId === project.id;
              return (
                <button
                  key={project.id}
                  type="button"
                  className={`${styles.projectItem} ${isSelected ? styles.projectItemSelected : ""}`}
                  onClick={() => handleSelect(project)}
                  aria-pressed={isSelected}
                  aria-label={`Projeto ${project.title}, nível ${project.level}, status ${project.status === "evaluated" ? "avaliado" : "pendente"}`}
                >
                  <span className={styles.projectLevel}>{project.level}</span>
                  <h2 className={styles.projectTitle}>{project.title}</h2>
                  <p className={styles.projectTeam}>{project.team} · {project.location}</p>
                  <span
                    className={`${styles.projectStatus} ${
                      isSelected
                        ? styles.selectedStatusBadge
                        : project.status === "evaluated"
                        ? styles.evaluatedStatus
                        : styles.pendingStatus
                    }`}
                  >
                    <Image
                      src={project.status === "evaluated" ? "/avaliador/evaluated-check.svg" : "/avaliador/pending-clock.svg"}
                      alt=""
                      width={14}
                      height={14}
                    />
                    {project.status === "evaluated" ? "Avaliado" : "Pendente"}
                  </span>
                  <span className={styles.projectChevronWrapper} aria-hidden="true">
                    <Image
                      className={`${styles.projectChevron} ${isSelected ? styles.projectChevronSelected : ""}`}
                      src="/avaliador/chevron-right.svg"
                      alt=""
                      width={12}
                      height={12}
                    />
                  </span>
                </button>
              );
            })}
            {visibleProjects.length === 0 && (
              <div className={styles.emptyProjects}>
                <span className={styles.emptyProjectsIcon} aria-hidden="true">
                  <Image src="/avaliador/search.svg" alt="" width={20} height={20} />
                </span>
                <h2 className={styles.emptyProjectsTitle}>Nenhum projeto encontrado</h2>
                <p className={styles.emptyProjectsDescription}>
                  Altere o termo de busca ou remova filtros para encontrar mais projetos.
                </p>
              </div>
            )}
          </div>
        </aside>
      )}
    </>
  );
}
