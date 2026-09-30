"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { mockProjects } from "./mockData";

export interface DashboardProject {
  id: number;
  name: string;
  level?: string | null;
  participantName?: string | null;
  institutionName?: string | null;
  markedForReview: boolean;
  validated: boolean;
  createdAt?: string | null;
  updatedAt?: string | null;
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

function formatDate(value?: string | null) {
  if (!value) return "—";
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? "—"
    : new Intl.DateTimeFormat("pt-BR", { dateStyle: "short" }).format(date);
}

export default function DashboardPage() {
  const [projects, setProjects] = useState<DashboardProject[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [demoMode, setDemoMode] = useState(false);

  const loadProjects = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`${API_BASE_URL}/api/projects`);
      if (!response.ok) throw new Error(`A API respondeu com status ${response.status}.`);
      const data: DashboardProject[] = await response.json();
      setProjects(data);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Não foi possível carregar os projetos.");
    } finally {
      setLoading(false);
    }
  }, []);

  const displayedProjects = demoMode ? mockProjects : projects;

  useEffect(() => {
    void loadProjects();
  }, [loadProjects]);

  const stats = useMemo(() => {
    const validated = displayedProjects.filter((project) => project.validated).length;
    const review = displayedProjects.filter((project) => project.markedForReview).length;
    const pending = displayedProjects.length - validated;
    const percent = displayedProjects.length ? Math.round((validated / displayedProjects.length) * 100) : 0;
    const levels = new Map<string, number>();
    for (const project of displayedProjects) {
      const level = project.level?.trim() || "Não informado";
      levels.set(level, (levels.get(level) ?? 0) + 1);
    }
    return {
      validated,
      review,
      pending,
      percent,
      levels: [...levels.entries()].sort((a, b) => b[1] - a[1]),
      recent: [...displayedProjects]
        .sort((a, b) => {
          const dateA = new Date(a.updatedAt || a.createdAt || 0).getTime();
          const dateB = new Date(b.updatedAt || b.createdAt || 0).getTime();
          return dateB - dateA;
        })
        .slice(0, 5),
    };
  }, [displayedProjects]);

  return (
    <main className="min-h-screen bg-[#f5f7f8] px-4 py-8 text-slate-900 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-7xl space-y-7">
        <header className="flex flex-col gap-5 border-b border-slate-200 pb-6 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <div className="mb-2 flex items-center gap-2">
              <span className="rounded bg-emerald-700 px-2 py-1 text-xs font-bold tracking-wide text-white">IFRS POA</span>
              <span className="text-sm font-medium text-slate-500">Mostra Nacional de Robótica</span>
            </div>
            <h1 className="text-3xl font-bold tracking-tight">Painel de projetos</h1>
            <p className="mt-1 text-sm text-slate-600">Acompanhamento dos projetos e da validação dos dados importados.</p>
          </div>
          <div className="flex flex-wrap gap-3">
            <button onClick={() => setDemoMode((enabled) => !enabled)} className={`rounded-lg border px-4 py-2.5 text-sm font-semibold shadow-sm ${demoMode ? "border-violet-300 bg-violet-50 text-violet-800 hover:bg-violet-100" : "border-slate-300 bg-white text-slate-700 hover:bg-slate-50"}`}>
              {demoMode ? "Sair da demonstração" : "Ver dados de demonstração"}
            </button>
            <button onClick={() => void loadProjects()} disabled={loading} className="rounded-lg border border-slate-300 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-sm hover:bg-slate-50 disabled:opacity-60">
              {loading ? "Atualizando…" : "Atualizar dados"}
            </button>
            <a href="/" className="rounded-lg bg-emerald-700 px-4 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-emerald-800">Importação e lista</a>
          </div>
        </header>

        {demoMode && <section role="status" className="rounded-xl border border-violet-200 bg-violet-50 px-4 py-3 text-sm text-violet-900">Modo de demonstração: os indicadores abaixo usam dados fictícios de exemplo.</section>}

        {error && !demoMode && (
          <section role="alert" className="flex flex-col gap-3 rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800 sm:flex-row sm:items-center sm:justify-between">
            <p>Não foi possível carregar os dados do backend. {error}</p>
            <button onClick={() => void loadProjects()} className="font-semibold underline">Tentar novamente</button>
          </section>
        )}

        <section aria-label="Indicadores dos projetos" className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <Metric title="Projetos cadastrados" value={loading && !demoMode ? "…" : displayedProjects.length} detail={demoMode ? "Dados fictícios de demonstração" : "Registros retornados pela API"} accent="emerald" />
          <Metric title="Validados" value={loading && !demoMode ? "…" : stats.validated} detail="Marcados como validados" accent="blue" />
          <Metric title="Pendentes de validação" value={loading && !demoMode ? "…" : stats.pending} detail="Projetos ainda não validados" accent="amber" />
          <Metric title="Marcados para revisão" value={loading && !demoMode ? "…" : stats.review} detail="Sinalizados na importação" accent="rose" />
        </section>

        <section className="grid gap-5 lg:grid-cols-[1.35fr_1fr]">
          <article className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="flex items-start justify-between gap-4">
              <div>
                <h2 className="text-lg font-bold">Progresso da validação</h2>
                <p className="mt-1 text-sm text-slate-500">Projetos validados em relação ao total cadastrado</p>
              </div>
              <span className="text-2xl font-bold text-emerald-700">{loading && !demoMode ? "—" : `${stats.percent}%`}</span>
            </div>
            <div className="mt-6 h-3 overflow-hidden rounded-full bg-slate-100" role="progressbar" aria-label="Percentual de projetos validados" aria-valuenow={loading && !demoMode ? 0 : stats.percent} aria-valuemin={0} aria-valuemax={100}>
              <div className="h-full rounded-full bg-emerald-600 transition-all" style={{ width: `${loading && !demoMode ? 0 : stats.percent}%` }} />
            </div>
            <div className="mt-3 flex justify-between text-sm text-slate-600">
              <span>{loading && !demoMode ? "Carregando…" : `${stats.validated} validados`}</span>
              <span>{loading && !demoMode ? "" : `${stats.pending} pendentes`}</span>
            </div>
          </article>

          <article className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-bold">Projetos por nível</h2>
            <p className="mt-1 text-sm text-slate-500">Distribuição conforme o campo disponível no cadastro</p>
            {loading && !demoMode ? <p className="mt-6 text-sm text-slate-500">Carregando dados…</p> : stats.levels.length === 0 ? <p className="mt-6 text-sm text-slate-500">Nenhum projeto cadastrado.</p> : (
              <ul className="mt-5 space-y-4">
                {stats.levels.slice(0, 5).map(([level, count]) => (
                  <li key={level}>
                    <div className="mb-1.5 flex justify-between gap-3 text-sm"><span className="truncate font-medium">{level}</span><span className="text-slate-500">{count}</span></div>
                    <div className="h-2 rounded-full bg-slate-100"><div className="h-2 rounded-full bg-emerald-600" style={{ width: `${Math.max(4, (count / displayedProjects.length) * 100)}%` }} /></div>
                  </li>
                ))}
              </ul>
            )}
          </article>
        </section>

        <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
          <div className="flex flex-col gap-1 px-6 py-5 sm:flex-row sm:items-end sm:justify-between">
            <div><h2 className="text-lg font-bold">Projetos recentes</h2><p className="mt-1 text-sm text-slate-500">Ordenados pela última atualização informada pela API</p></div>
            <a href="/" className="text-sm font-semibold text-emerald-700 hover:text-emerald-900">Ver lista completa →</a>
          </div>
          {loading && !demoMode ? <p className="px-6 pb-6 text-sm text-slate-500">Carregando projetos…</p> : stats.recent.length === 0 ? <p className="px-6 pb-6 text-sm text-slate-500">Nenhum projeto disponível.</p> : (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[620px] text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500"><tr><th className="px-6 py-3 font-semibold">Projeto</th><th className="px-6 py-3 font-semibold">Participante</th><th className="px-6 py-3 font-semibold">Nível</th><th className="px-6 py-3 font-semibold">Atualizado</th><th className="px-6 py-3 font-semibold">Situação</th></tr></thead>
                <tbody className="divide-y divide-slate-100">
                  {stats.recent.map((project) => (
                    <tr key={project.id} className="hover:bg-slate-50"><td className="max-w-xs px-6 py-4 font-semibold text-slate-800">{project.name || "Projeto sem nome"}<span className="mt-1 block truncate text-xs font-normal text-slate-500">{project.institutionName || "Instituição não informada"}</span></td><td className="px-6 py-4 text-slate-600">{project.participantName || "—"}</td><td className="px-6 py-4 text-slate-600">{project.level || "—"}</td><td className="px-6 py-4 text-slate-600">{formatDate(project.updatedAt || project.createdAt)}</td><td className="px-6 py-4"><span className={`whitespace-nowrap rounded-full px-2.5 py-1 text-xs font-semibold ${project.markedForReview ? "bg-rose-50 text-rose-700" : project.validated ? "bg-emerald-50 text-emerald-700" : "bg-amber-50 text-amber-700"}`}>{project.markedForReview ? "Revisar" : project.validated ? "Validado" : "Pendente"}</span></td></tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
        <footer className="text-center text-xs text-slate-500">{demoMode ? "Dados fictícios para visualização do painel." : <>Indicadores calculados a partir dos projetos retornados por <code>/api/projects</code>.</>}</footer>
      </div>
    </main>
  );
}


function Metric({ title, value, detail, accent }: { title: string; value: string | number; detail: string; accent: "emerald" | "blue" | "amber" | "rose" }) {
  const colors = {
    emerald: "bg-emerald-50 text-emerald-700",
    blue: "bg-blue-50 text-blue-700",
    amber: "bg-amber-50 text-amber-700",
    rose: "bg-rose-50 text-rose-700",
  };
  return (
    <article className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-3"><h2 className="text-sm font-semibold text-slate-600">{title}</h2><span aria-hidden="true" className={`h-2.5 w-2.5 rounded-full ${colors[accent].split(" ")[0]}`} /></div>
      <p className="mt-4 text-3xl font-bold tracking-tight">{value}</p>
      <p className="mt-1 text-xs text-slate-500">{detail}</p>
    </article>
  );
}
