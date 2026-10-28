"use client";

import Image from "next/image";
import { useMemo, useState } from "react";
import { evaluatorProjectsMock, type EvaluatorProjectStatus } from "./mockData";
import styles from "./page.module.css";

type ProjectFilter = "all" | EvaluatorProjectStatus;

const filters: { value: ProjectFilter; label: string }[] = [
  { value: "all", label: "Todos" },
  { value: "pending", label: "Pendentes" },
  { value: "evaluated", label: "Concluídos" },
];

export default function WorkSearch() {
  const [isOpen, setIsOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [filter, setFilter] = useState<ProjectFilter>("all");

  const visibleProjects = useMemo(() => {
    const term = searchTerm.trim().toLocaleLowerCase("pt-BR");
    return evaluatorProjectsMock.filter((project) => {
      const matchesFilter = filter === "all" || project.status === filter;
      const matchesSearch = !term || `${project.title} ${project.level}`.toLocaleLowerCase("pt-BR").includes(term);
      return matchesFilter && matchesSearch;
    });
  }, [filter, searchTerm]);

  function openSearch() {
    setSearchTerm("");
    setFilter("all");
    setIsOpen(true);
  }

  function closeSearch() {
    setSearchTerm("");
    setFilter("all");
    setIsOpen(false);
  }

  return (
    <>
      <button className={styles.primaryButton} type="button" onClick={openSearch}>
        Ver trabalhos
      </button>

      {isOpen && (
        <aside className={styles.searchDrawer} aria-label="Busca e filtro de trabalhos">
          <div className={styles.drawerHeader}>
            <button className={styles.backButton} type="button" onClick={closeSearch}>
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
            {visibleProjects.map((project) => (
              <article className={styles.projectItem} key={project.id}>
                <span className={styles.projectLevel}>{project.level}</span>
                <h2 className={styles.projectTitle}>{project.title}</h2>
                <p className={styles.projectTeam}>{project.team} · {project.location}</p>
                <span className={`${styles.projectStatus} ${project.status === "evaluated" ? styles.evaluatedStatus : styles.pendingStatus}`}>
                  <Image
                    src={project.status === "evaluated" ? "/avaliador/evaluated-check.svg" : "/avaliador/pending-clock.svg"}
                    alt=""
                    width={14}
                    height={14}
                  />
                  {project.status === "evaluated" ? "Avaliado" : "Pendente"}
                </span>
                <Image className={styles.projectChevron} src="/avaliador/chevron-right.svg" alt="" width={12} height={12} />
              </article>
            ))}
            {visibleProjects.length === 0 && <p className={styles.emptyProjects}>Nenhum trabalho encontrado.</p>}
          </div>
        </aside>
      )}
    </>
  );
}
