"use client";

import { useCallback, useEffect, useState, useMemo, ChangeEvent, FormEvent } from "react";

interface Project {
  id: number;
  eventId?: number | null;
  name: string;
  pdfUrl?: string | null;
  level?: string | null;
  videoUrl?: string | null;
  participantName?: string | null;
  participantCpf?: string | null;
  participantEmail?: string | null;
  institutionName?: string | null;
  markedForReview: boolean;
  validated: boolean;
  createdAt?: string | null;
  updatedAt?: string | null;
}

interface ProjectEvent {
  id: number;
  name: string;
}

interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

interface ImportSummary {
  eventId: number;
  eventName: string;
  totalProcessed: number;
  totalCreated: number;
  totalUpdated: number;
  totalMarkedForReview: number;
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export default function Home() {
  const [eventName, setEventName] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [summary, setSummary] = useState<ImportSummary | null>(null);
  const [projects, setProjects] = useState<Project[]>([]);
  const [events, setEvents] = useState<ProjectEvent[]>([]);
  const [selectedEventId, setSelectedEventId] = useState<number | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalProjects, setTotalProjects] = useState(0);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<"all" | "review" | "validated" | "clean">("all");

  const fetchProjects = useCallback(async (eventId: number, page = 0) => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await fetch(
        `${API_BASE_URL}/api/events/${eventId}/projects?page=${page}&size=10`
      );

      if (!response.ok) {
        throw new Error(`Erro ao buscar projetos: HTTP ${response.status}`);
      }

      const data: PageResponse<Project> = await response.json();

      setProjects(data.content);
      setCurrentPage(data.number);
      setTotalPages(data.totalPages);
      setTotalProjects(data.totalElements);
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : "Erro desconhecido ao carregar projetos.";
      setError(message);
      setProjects([]);
      setCurrentPage(0);
      setTotalPages(0);
      setTotalProjects(0);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const fetchEvents = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await fetch(`${API_BASE_URL}/api/events`);

      if (!response.ok) {
        throw new Error(`Erro ao buscar eventos: HTTP ${response.status}`);
      }

      const data: ProjectEvent[] = await response.json();
      const firstEventId = data[0]?.id ?? null;

      setEvents(data);
      setSelectedEventId(firstEventId);

      if (firstEventId !== null) {
        await fetchProjects(firstEventId, 0);
      } else {
        setProjects([]);
        setCurrentPage(0);
        setTotalPages(0);
        setTotalProjects(0);
      }
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : "Erro desconhecido ao carregar eventos.";
      setError(message);
      setEvents([]);
      setSelectedEventId(null);
      setProjects([]);
    } finally {
      setIsLoading(false);
    }
  }, [fetchProjects]);

  useEffect(() => {
    void fetchEvents();
  }, [fetchEvents]);

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const selected = e.target.files[0];
      const ext = selected.name.split(".").pop()?.toLowerCase();
      if (!["csv", "xlsx", "xls"].includes(ext || "")) {
        setError("Formato inválido. Selecione um arquivo .csv, .xlsx ou .xls");
        setFile(null);
        return;
      }
      setError(null);
      setFile(selected);
    }
  };

  const handleImport = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    if (!eventName.trim()) {
      setError("O Nome do Evento é obrigatório.");
      return;
    }

    if (!file) {
      setError("Por favor, selecione um arquivo CSV ou Excel (.xlsx, .xls).");
      return;
    }

    setIsUploading(true);
    const formData = new FormData();
    formData.append("file", file);
    formData.append("eventName", eventName.trim());

    try {
      const res = await fetch(`${API_BASE_URL}/api/projects/import`, {
        method: "POST",
        body: formData,
      });

      if (!res.ok) {
        const errorText = await res.text();
        throw new Error(errorText || `Falha no envio (HTTP ${res.status})`);
      }

      const data = await res.json();
      setSummary({
        eventId: data.eventId,
        eventName: data.eventName,
        totalProcessed: data.totalProcessed,
        totalCreated: data.totalCreated,
        totalUpdated: data.totalUpdated,
        totalMarkedForReview: data.totalMarkedForReview,
      });

      setEvents((currentEvents) =>
        currentEvents.some((event) => event.id === data.eventId)
          ? currentEvents
          : [...currentEvents, { id: data.eventId, name: data.eventName }]
      );
      setSelectedEventId(data.eventId);
      await fetchProjects(data.eventId, 0);

      setSuccess(`Importação concluída com sucesso! Processados: ${data.totalProcessed}`);
      setFile(null);
      // Reset input element
      const fileInput = document.getElementById("file-input") as HTMLInputElement;
      if (fileInput) fileInput.value = "";
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Erro ao processar arquivo no servidor.";
      setError(msg);
    } finally {
      setIsUploading(false);
    }
  };

  const filteredProjects = useMemo(() => {
    return projects.filter((p) => {
      // Text match
      const term = searchTerm.toLowerCase().trim();
      const matchesSearch =
        !term ||
        p.name?.toLowerCase().includes(term) ||
        p.participantName?.toLowerCase().includes(term) ||
        p.participantCpf?.toLowerCase().includes(term) ||
        p.participantEmail?.toLowerCase().includes(term) ||
        p.institutionName?.toLowerCase().includes(term) ||
        p.level?.toLowerCase().includes(term);

      if (!matchesSearch) return false;

      // Status match
      if (statusFilter === "review") return p.markedForReview;
      if (statusFilter === "validated") return p.validated;
      if (statusFilter === "clean") return !p.markedForReview;
      return true;
    });
  }, [projects, searchTerm, statusFilter]);

  const counts = useMemo(() => {
    return {
      total: projects.length,
      review: projects.filter((p) => p.markedForReview).length,
      validated: projects.filter((p) => p.validated).length,
    };
  }, [projects]);

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 pb-16">
      {/* Header */}
      <header className="bg-white border-b border-slate-200 sticky top-0 z-10 shadow-xs">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <div className="flex items-center gap-2">
              <span className="bg-emerald-600 text-white font-bold px-2 py-0.5 rounded text-xs tracking-wider">
                IFRS POA
              </span>
              <h1 className="text-xl font-bold tracking-tight text-slate-900">
                Avaliação MNR - Importação e Gestão
              </h1>
            </div>
            <p className="text-sm text-slate-500 mt-0.5">
              Envio e acompanhamento de submissões de eventos, participantes e projetos
            </p>
          </div>

          <button
            onClick={() => {
              if (selectedEventId !== null) {
                void fetchProjects(selectedEventId, 0);
              }
            }}
            disabled={isLoading || selectedEventId === null}
            className="inline-flex items-center justify-center gap-1.5 px-3 py-1.5 border border-slate-300 rounded-md text-sm font-medium text-slate-700 bg-white hover:bg-slate-50 disabled:opacity-50 transition cursor-pointer"
          >
            {isLoading ? "Atualizando..." : "🔄 Atualizar Lista"}
          </button>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-8 space-y-6">
        {/* Upload Form Card */}
        <section className="bg-white rounded-xl border border-slate-200 shadow-xs p-6">
          <h2 className="text-lg font-semibold text-slate-900 mb-1">
            📥 Importar Arquivo de Submissões
          </h2>
          <p className="text-sm text-slate-500 mb-6">
            Selecione o arquivo de submissões nos formatos <strong>.CSV</strong> ou <strong>.XLSX / .XLS</strong>.
          </p>

          <form onSubmit={handleImport} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Nome do Evento <span className="text-rose-500">*</span>
                </label>
                <input
                  type="text"
                  required
                  value={eventName}
                  onChange={(e) => setEventName(e.target.value)}
                  placeholder="Ex: Etapa Regional 2026, MNR Nacional..."
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 bg-white"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Arquivo (.csv, .xlsx, .xls) <span className="text-rose-500">*</span>
                </label>
                <input
                  id="file-input"
                  type="file"
                  accept=".csv,.xlsx,.xls"
                  onChange={handleFileChange}
                  className="w-full px-3 py-1.5 border border-slate-300 rounded-lg text-sm file:mr-3 file:py-1 file:px-3 file:rounded-md file:border-0 file:text-xs file:font-semibold file:bg-emerald-50 file:text-emerald-700 hover:file:bg-emerald-100 bg-white cursor-pointer"
                />
              </div>
            </div>

            {error && (
              <div className="p-3.5 bg-rose-50 border border-rose-200 rounded-lg text-sm text-rose-700 flex items-start gap-2">
                <span>⚠️</span>
                <span>{error}</span>
              </div>
            )}

            {success && (
              <div className="p-3.5 bg-emerald-50 border border-emerald-200 rounded-lg text-sm text-emerald-800 flex items-start gap-2">
                <span>✅</span>
                <span>{success}</span>
              </div>
            )}

            <div className="flex justify-end">
              <button
                type="submit"
                disabled={isUploading}
                className="inline-flex items-center gap-2 px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-medium text-sm rounded-lg transition shadow-xs disabled:opacity-60 cursor-pointer"
              >
                {isUploading ? (
                  <>
                    <span className="inline-block animate-spin">⏳</span>
                    <span>Importando e validando projetos...</span>
                  </>
                ) : (
                  <>
                    <span>🚀 Iniciar Importação</span>
                  </>
                )}
              </button>
            </div>
          </form>
        </section>

        {/* Import Summary Counters (If available) */}
        {summary && (
          <section className="bg-slate-900 text-white rounded-xl p-5 shadow-xs">
            <div className="flex items-center justify-between mb-4 border-b border-slate-800 pb-3">
              <div>
                <span className="text-xs uppercase font-semibold text-emerald-400 tracking-wider">
                  Resultado do Último Upload
                </span>
                <h3 className="text-lg font-bold">
                  {summary.eventName} (ID: #{summary.eventId})
                </h3>
              </div>
            </div>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              <div className="bg-slate-800/80 p-3.5 rounded-lg border border-slate-700">
                <span className="text-xs text-slate-400 block mb-1">Total Processados</span>
                <span className="text-2xl font-bold">{summary.totalProcessed}</span>
              </div>
              <div className="bg-slate-800/80 p-3.5 rounded-lg border border-slate-700">
                <span className="text-xs text-emerald-400 block mb-1">Novos Cadastrados</span>
                <span className="text-2xl font-bold text-emerald-300">{summary.totalCreated}</span>
              </div>
              <div className="bg-slate-800/80 p-3.5 rounded-lg border border-slate-700">
                <span className="text-xs text-cyan-400 block mb-1">Atualizados</span>
                <span className="text-2xl font-bold text-cyan-300">{summary.totalUpdated}</span>
              </div>
              <div className="bg-slate-800/80 p-3.5 rounded-lg border border-slate-700">
                <span className="text-xs text-amber-400 block mb-1">Pendentes de Revisão</span>
                <span className="text-2xl font-bold text-amber-300">
                  {summary.totalMarkedForReview}
                </span>
              </div>
            </div>
          </section>
        )}

        {/* Global Summary Statistics & Controls */}
        <section className="space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-end gap-3">
            <div className="w-full sm:max-w-md">
              <label htmlFor="event-select" className="block text-sm font-medium text-slate-700 mb-1">
                Evento
              </label>
              <select
                id="event-select"
                value={selectedEventId ?? ""}
                onChange={(e) => {
                  const value = e.target.value;
                  const eventId = value ? Number(value) : null;

                  setSelectedEventId(eventId);
                  setCurrentPage(0);
                  setSearchTerm("");
                  setStatusFilter("all");

                  if (eventId === null) {
                    setProjects([]);
                    setTotalPages(0);
                    setTotalProjects(0);
                  } else {
                    void fetchProjects(eventId, 0);
                  }
                }}
                disabled={isLoading || events.length === 0}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 disabled:bg-slate-100"
              >
                <option value="">
                  {events.length === 0 ? "Nenhum evento disponível" : "Selecione um evento"}
                </option>
                {events.map((event) => (
                  <option key={event.id} value={event.id}>
                    {event.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            {/* Filter buttons */}
            <div className="flex flex-wrap items-center gap-2">
              <button
                onClick={() => setStatusFilter("all")}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition cursor-pointer ${
                  statusFilter === "all"
                    ? "bg-slate-800 text-white"
                    : "bg-white border border-slate-200 text-slate-600 hover:bg-slate-50"
                }`}
              >
                Todos ({counts.total})
              </button>
              <button
                onClick={() => setStatusFilter("review")}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition cursor-pointer ${
                  statusFilter === "review"
                    ? "bg-amber-600 text-white"
                    : "bg-white border border-amber-200 text-amber-800 hover:bg-amber-50"
                }`}
              >
                ⚠️ Pendentes de Revisão ({counts.review})
              </button>
              <button
                onClick={() => setStatusFilter("validated")}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition cursor-pointer ${
                  statusFilter === "validated"
                    ? "bg-emerald-600 text-white"
                    : "bg-white border border-emerald-200 text-emerald-800 hover:bg-emerald-50"
                }`}
              >
                ✓ Validados ({counts.validated})
              </button>
              <button
                onClick={() => setStatusFilter("clean")}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition cursor-pointer ${
                  statusFilter === "clean"
                    ? "bg-blue-600 text-white"
                    : "bg-white border border-slate-200 text-slate-600 hover:bg-slate-50"
                }`}
              >
                Sem Pendências ({counts.total - counts.review})
              </button>
            </div>

            {/* Search Input */}
            <div className="w-full md:w-80">
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="🔍 Buscar por projeto, participante, CPF..."
                className="w-full px-3 py-1.5 border border-slate-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500"
              />
            </div>
          </div>

          {/* Table */}
          <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm text-slate-600">
                <thead className="bg-slate-50 text-slate-700 text-xs uppercase font-semibold border-b border-slate-200">
                  <tr>
                    <th scope="col" className="px-4 py-3">
                      Projeto & Nível
                    </th>
                    <th scope="col" className="px-4 py-3">
                      Participante
                    </th>
                    <th scope="col" className="px-4 py-3">
                      Instituição & Contato
                    </th>
                    <th scope="col" className="px-4 py-3 text-center">
                      Links / Arquivos
                    </th>
                    <th scope="col" className="px-4 py-3 text-center">
                      Status de Validação
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {filteredProjects.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="px-4 py-12 text-center text-slate-400">
                        {isLoading
                          ? "Carregando projetos..."
                          : events.length === 0
                            ? "Nenhum evento disponível para listar projetos."
                            : selectedEventId === null
                              ? "Selecione um evento para visualizar os projetos."
                              : searchTerm || statusFilter !== "all"
                                ? "Nenhum projeto corresponde aos filtros nesta página."
                                : "Nenhum projeto encontrado para este evento. Importe um arquivo CSV ou Excel acima para visualizar."}
                      </td>
                    </tr>
                  ) : (
                    filteredProjects.map((p) => (
                      <tr
                        key={p.id}
                        className={`hover:bg-slate-50/70 transition ${
                          p.markedForReview ? "bg-amber-50/30" : ""
                        }`}
                      >
                        <td className="px-4 py-3 align-top">
                          <div className="font-semibold text-slate-900">{p.name}</div>
                          {p.level && (
                            <span className="inline-block mt-1 px-2 py-0.5 bg-slate-100 text-slate-700 text-xs rounded border border-slate-200">
                              Nível: {p.level}
                            </span>
                          )}
                        </td>

                        <td className="px-4 py-3 align-top">
                          <div className="font-medium text-slate-800">
                            {p.participantName || <span className="text-slate-400">-</span>}
                          </div>
                          {p.participantCpf && (
                            <div className="text-xs text-slate-400 mt-0.5">
                              CPF: {p.participantCpf}
                            </div>
                          )}
                        </td>

                        <td className="px-4 py-3 align-top">
                          <div className="text-slate-700">
                            {p.institutionName || <span className="text-slate-400">-</span>}
                          </div>
                          {p.participantEmail && (
                            <div className="text-xs text-slate-400 mt-0.5 truncate max-w-xs">
                              {p.participantEmail}
                            </div>
                          )}
                        </td>

                        <td className="px-4 py-3 align-top text-center">
                          <div className="flex items-center justify-center gap-2">
                            {p.pdfUrl ? (
                              <a
                                href={p.pdfUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="inline-flex items-center gap-1 text-xs px-2.5 py-1 bg-red-50 text-red-700 hover:bg-red-100 rounded border border-red-200 font-medium transition"
                                title="Abrir Artigo / PDF"
                              >
                                📄 PDF
                              </a>
                            ) : (
                              <span className="text-xs text-slate-300">-</span>
                            )}

                            {p.videoUrl ? (
                              <a
                                href={p.videoUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="inline-flex items-center gap-1 text-xs px-2.5 py-1 bg-blue-50 text-blue-700 hover:bg-blue-100 rounded border border-blue-200 font-medium transition"
                                title="Abrir Vídeo"
                              >
                                🎥 Vídeo
                              </a>
                            ) : (
                              <span className="text-xs text-slate-300">-</span>
                            )}
                          </div>
                        </td>

                        <td className="px-4 py-3 align-top text-center">
                          <div className="flex flex-col items-center gap-1">
                            {p.markedForReview ? (
                              <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-800 border border-amber-300">
                                ⚠️ Revisão Necessária
                              </span>
                            ) : (
                              <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 border border-emerald-300">
                                ✓ Sem Pendências
                              </span>
                            )}

                            {p.validated ? (
                              <span className="text-[11px] text-emerald-700 font-medium">
                                Validado
                              </span>
                            ) : (
                              <span className="text-[11px] text-slate-400">
                                Não validado
                              </span>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            <div className="px-4 py-3 bg-slate-50 border-t border-slate-200 text-xs text-slate-500 flex flex-col gap-3 sm:flex-row sm:justify-between sm:items-center">
              <div className="space-y-1">
                <span className="block">
                  Exibindo {filteredProjects.length} de {projects.length} projeto(s) nesta página; {totalProjects} no evento.
                </span>
                <span className="block">
                  Filtros e busca consideram apenas os projetos carregados nesta página.
                </span>
              </div>
              <div className="flex flex-wrap items-center gap-2">
                <button
                  type="button"
                  onClick={() => {
                    if (selectedEventId !== null) void fetchProjects(selectedEventId, 0);
                  }}
                  disabled={isLoading || selectedEventId === null || currentPage === 0 || totalPages === 0}
                  className="px-2.5 py-1 border border-slate-300 rounded bg-white hover:bg-slate-100 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Primeira
                </button>
                <button
                  type="button"
                  onClick={() => {
                    if (selectedEventId !== null) void fetchProjects(selectedEventId, currentPage - 1);
                  }}
                  disabled={isLoading || selectedEventId === null || currentPage === 0 || totalPages === 0}
                  className="px-2.5 py-1 border border-slate-300 rounded bg-white hover:bg-slate-100 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Anterior
                </button>
                <span className="px-1">
                  Página {totalPages === 0 ? 0 : currentPage + 1} de {totalPages}
                </span>
                <button
                  type="button"
                  onClick={() => {
                    if (selectedEventId !== null) void fetchProjects(selectedEventId, currentPage + 1);
                  }}
                  disabled={isLoading || selectedEventId === null || totalPages === 0 || currentPage >= totalPages - 1}
                  className="px-2.5 py-1 border border-slate-300 rounded bg-white hover:bg-slate-100 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Próxima
                </button>
                <button
                  type="button"
                  onClick={() => {
                    if (selectedEventId !== null) void fetchProjects(selectedEventId, totalPages - 1);
                  }}
                  disabled={isLoading || selectedEventId === null || totalPages === 0 || currentPage >= totalPages - 1}
                  className="px-2.5 py-1 border border-slate-300 rounded bg-white hover:bg-slate-100 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Última
                </button>
              </div>
              <span>Backend API: {API_BASE_URL}</span>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}
