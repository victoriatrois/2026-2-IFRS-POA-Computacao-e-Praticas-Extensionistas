"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import AppHeader from "@/components/AppHeader";
import DashboardMetrics from "@/components/dashboard/DashboardMetrics";
import DashboardFilters, { type FilterStatus } from "@/components/dashboard/DashboardFilters";
import ProjectTable from "@/components/dashboard/ProjectTable";
import type { DashboardProjectItem } from "@/components/dashboard/ProjectRow";
import { mockDashboardProjects } from "./mockData";
import styles from "@/components/dashboard/DashboardRefined.module.css";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

interface BackendProjectResponse {
  id: number;
  name?: string | null;
  level?: string | null;
  participantName?: string | null;
  participantCpf?: string | null;
  institutionName?: string | null;
  markedForReview?: boolean;
  validated?: boolean;
}

export default function DashboardPage() {
  const [apiProjects, setApiProjects] = useState<DashboardProjectItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [useDemoData, setUseDemoData] = useState(true);
  const [filter, setFilter] = useState<FilterStatus>("all");
  const [searchTerm, setSearchTerm] = useState("");

  const loadProjectsFromApi = useCallback(async () => {
    setLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/projects/dashboard`);
      if (!response.ok) {
        throw new Error(`Status ${response.status}`);
      }
      const data: BackendProjectResponse[] = await response.json();
      if (Array.isArray(data) && data.length > 0) {
        const mapped: DashboardProjectItem[] = data.map((item) => {
          let status: "validated" | "pending" | "rejected" = "pending";
          if (item.validated) {
            status = "validated";
          } else if (item.markedForReview) {
            status = "rejected";
          }
          return {
            id: item.id,
            name: item.name || "Projeto sem título",
            level: item.level || "N1",
            subtitle: item.institutionName || null,
            status,
            participantName: item.participantName,
            participantCpf: item.participantCpf,
            institutionName: item.institutionName,
          };
        });
        setApiProjects(mapped);
      }
    } catch {
      // Falha silenciosa: usa os dados mockados como fallback confiável
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadProjectsFromApi();
  }, [loadProjectsFromApi]);

  const activeProjects = useMemo(() => {
    if (useDemoData || apiProjects.length === 0) {
      return mockDashboardProjects;
    }
    return apiProjects;
  }, [useDemoData, apiProjects]);

  const counts = useMemo(() => {
    const total = activeProjects.length;
    const validated = activeProjects.filter((p) => p.status === "validated").length;
    const pending = activeProjects.filter((p) => p.status === "pending").length;
    const rejected = activeProjects.filter((p) => p.status === "rejected").length;
    return { total, validated, pending, rejected };
  }, [activeProjects]);

  const filteredProjects = useMemo(() => {
    const term = searchTerm.trim().toLowerCase();
    return activeProjects.filter((project) => {
      const matchesFilter = filter === "all" || project.status === filter;
      const searchable = `${project.name} ${project.subtitle || ""} ${project.level || ""} ${project.participantName || ""} ${project.participantCpf || ""}`.toLowerCase();
      const matchesSearch = !term || searchable.includes(term);
      return matchesFilter && matchesSearch;
    });
  }, [activeProjects, filter, searchTerm]);

  return (
    <div className={styles.dashboardPage}>
      <AppHeader variant="admin" activeItem="panel" userName="Ana Costa" />

      <main className={styles.dashboardContainer}>
        <header className={styles.dashboardHeader}>
          <div className={styles.dashboardHeaderTop}>
            <div>
              <h1 className={styles.dashboardTitle}>Pré-Avaliação Automática</h1>
              <p className={styles.dashboardSubtitle}>
                Resultado parcial dos projetos aprovados, pendentes e reprovados pelo sistema.
              </p>
            </div>

            <div className={styles.headerActions}>
              <button
                type="button"
                className={styles.toggleDemoButton}
                onClick={() => setUseDemoData((prev) => !prev)}
              >
                {useDemoData ? "Visualizar dados da API" : "Visualizar dados do Penpot (Demo)"}
              </button>

              <button
                type="button"
                className={styles.refreshButton}
                onClick={() => void loadProjectsFromApi()}
                disabled={loading}
              >
                {loading ? "Atualizando…" : "Atualizar"}
              </button>
            </div>
          </div>
        </header>

        <DashboardMetrics
          totalProcessed={counts.total}
          totalValidated={counts.validated}
          totalPending={counts.pending}
          totalRejected={counts.rejected}
        />

        <DashboardFilters
          currentFilter={filter}
          onFilterChange={setFilter}
          searchTerm={searchTerm}
          onSearchChange={setSearchTerm}
          counts={counts}
        />

        <ProjectTable
          projects={filteredProjects}
          onViewDiagnostic={(projectId) => {
            // Pronto para conexão com a Issue #91 que Taylan está implementando
            console.log(`[Issue #91 - Taylan] Ver diagnóstico clicado para o projeto ${projectId}`);
          }}
        />
      </main>
    </div>
  );
}
