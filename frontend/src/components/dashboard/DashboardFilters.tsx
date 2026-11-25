"use client";

import styles from "./DashboardRefined.module.css";
import type { ProjectEvaluationStatus } from "./StatusBadge";

export type FilterStatus = "all" | ProjectEvaluationStatus;

interface DashboardFiltersProps {
  currentFilter: FilterStatus;
  onFilterChange: (filter: FilterStatus) => void;
  searchTerm: string;
  onSearchChange: (search: string) => void;
  counts: {
    total: number;
    pending: number;
    validated: number;
    rejected: number;
  };
}

export default function DashboardFilters({
  currentFilter,
  onFilterChange,
  searchTerm,
  onSearchChange,
  counts,
}: DashboardFiltersProps) {
  return (
    <div className={styles.filterSearchRow} role="region" aria-label="Filtros e busca de projetos">
      <div className={styles.filtersGroup} role="group" aria-label="Filtrar por status de validação">
        <button
          type="button"
          className={`${styles.filterPill} ${currentFilter === "all" ? styles.filterPillActive : ""}`}
          onClick={() => onFilterChange("all")}
          aria-pressed={currentFilter === "all"}
        >
          Todos ({counts.total})
        </button>

        <button
          type="button"
          className={`${styles.filterPill} ${currentFilter === "pending" ? styles.filterPillActive : ""}`}
          onClick={() => onFilterChange("pending")}
          aria-pressed={currentFilter === "pending"}
        >
          <svg
            width="14"
            height="14"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2.2"
            strokeLinecap="round"
            strokeLinejoin="round"
            style={{ color: currentFilter === "pending" ? "#ffffff" : "#ca8a04" }}
            aria-hidden="true"
          >
            <circle cx="12" cy="12" r="10" />
            <polyline points="12 6 12 12 16 14" />
          </svg>
          Pendentes de Revisão ({counts.pending})
        </button>

        <button
          type="button"
          className={`${styles.filterPill} ${currentFilter === "validated" ? styles.filterPillActive : ""}`}
          onClick={() => onFilterChange("validated")}
          aria-pressed={currentFilter === "validated"}
        >
          <svg
            width="14"
            height="14"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2.5"
            strokeLinecap="round"
            strokeLinejoin="round"
            style={{ color: currentFilter === "validated" ? "#ffffff" : "#16a34a" }}
            aria-hidden="true"
          >
            <polyline points="20 6 9 17 4 12" />
          </svg>
          Validados ({counts.validated})
        </button>

        <button
          type="button"
          className={`${styles.filterPill} ${currentFilter === "rejected" ? styles.filterPillActive : ""}`}
          onClick={() => onFilterChange("rejected")}
          aria-pressed={currentFilter === "rejected"}
        >
          <svg
            width="14"
            height="14"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2.5"
            strokeLinecap="round"
            strokeLinejoin="round"
            style={{ color: currentFilter === "rejected" ? "#ffffff" : "#dc2626" }}
            aria-hidden="true"
          >
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
          Reprovados ({counts.rejected})
        </button>
      </div>

      <div className={styles.searchBox}>
        <svg
          className={styles.searchIcon}
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2.2"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <circle cx="11" cy="11" r="8" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
        <input
          type="search"
          className={styles.searchInput}
          value={searchTerm}
          onChange={(e) => onSearchChange(e.target.value)}
          placeholder="Buscar por projeto, participante, CPF..."
          aria-label="Buscar por projeto, participante ou CPF"
        />
      </div>
    </div>
  );
}
