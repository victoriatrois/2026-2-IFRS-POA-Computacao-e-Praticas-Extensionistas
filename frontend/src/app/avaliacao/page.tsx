"use client";

import { useMemo, useState } from "react";
import { evaluationCriteria, mockProjects, type DemoProject } from "./mockData";
import styles from "./page.module.css";

type Filter = "todos" | "pendentes" | "concluidos";

export default function AvaliacaoPage() {
  const [selectedId, setSelectedId] = useState<number | null>(mockProjects[0]?.id ?? null);
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState<Filter>("todos");
  const [scores, setScores] = useState<Record<number, Record<string, number>>>({});
  const [activeGuidance, setActiveGuidance] = useState<Record<string, number>>({});
  const [materialAnswers, setMaterialAnswers] = useState<Record<number, { article: boolean | null; video: boolean | null }>>({});
  const [honorableMention, setHonorableMention] = useState<Record<number, boolean | null>>({});
  const [participantFeedback, setParticipantFeedback] = useState<Record<number, string>>({});
  const [organizationNotes, setOrganizationNotes] = useState<Record<number, string>>({});
  const [submitted, setSubmitted] = useState<Set<number>>(new Set());
  const [notice, setNotice] = useState("");

  const selected = mockProjects.find((project) => project.id === selectedId) ?? null;
  const isSubmitted = (project: DemoProject) => submitted.has(project.id) || project.completed;
  const answeredMaterials = selected
    ? materialAnswers[selected.id] ?? { article: selected.materials.article, video: selected.materials.video }
    : { article: null, video: null };
  const criteriaEnabled = answeredMaterials.article === true && answeredMaterials.video === true;
  const selectedScores = selected ? scores[selected.id] ?? {} : {};
  const answeredCount = Object.keys(selectedScores).length;
  const average = answeredCount
    ? (Object.values(selectedScores).reduce((sum, score) => sum + score, 0) / answeredCount).toFixed(1)
    : "—";

  const filteredProjects = useMemo(() => mockProjects.filter((project) => {
    const complete = submitted.has(project.id) || project.completed;
    if (filter === "pendentes" && complete) return false;
    if (filter === "concluidos" && !complete) return false;
    const term = search.trim().toLocaleLowerCase("pt-BR");
    return !term || `${project.name} ${project.team} ${project.stand} ${project.institution}`.toLocaleLowerCase("pt-BR").includes(term);
  }), [filter, search, submitted]);

  function setMaterialAnswer(key: "article" | "video", value: boolean) {
    if (!selected) return;
    setMaterialAnswers((current) => ({
      ...current,
      [selected.id]: { ...answeredMaterials, [key]: value },
    }));
    setNotice("");
  }

  function submitEvaluation() {
    if (!selected) return;
    if (!criteriaEnabled) {
      setNotice("A ficha informa que os critérios só devem ser avaliados quando os dois materiais forem apresentados.");
      return;
    }
    if (answeredCount !== evaluationCriteria.length) {
      setNotice("Selecione um nível para cada um dos 13 critérios antes de registrar a avaliação.");
      return;
    }
    if (honorableMention[selected.id] === undefined || honorableMention[selected.id] === null) {
      setNotice("Responda se o projeto tem potencial para receber Menção Honrosa.");
      return;
    }
    setSubmitted((current) => new Set(current).add(selected.id));
    setNotice("Avaliação registrada nesta demonstração. Os dados ainda não foram enviados para o backend.");
  }

  return (
    <main className={styles.page}>
      <header className={styles.header}>
        <a className={styles.brand} href="/" aria-label="Voltar à página inicial">
          <span className={styles.logoMark}>MNR</span>
          <span className={styles.brandText}>GESTÃO DE<br />AVALIAÇÕES</span>
        </a>
        <nav className={styles.navigation} aria-label="Navegação principal">
          <a href="/dashboard">▦ <span>Painel</span></a>
          <a className={styles.activeNav} href="/avaliacao" aria-current="page">▣ <span>Avaliar</span></a>
          <span aria-disabled="true" title="Disponível em outra etapa">♧ <span>Menções</span></span>
          <span aria-disabled="true" title="Disponível em outra etapa">➤ <span>Feedback</span></span>
          <span aria-disabled="true" title="Disponível em outra etapa">⇩ <span>Exportar</span></span>
        </nav>
        <div className={styles.userArea}>
          <span className={styles.online}><span /> Demonstração</span>
          <span className={styles.user}><b>AV</b> Avaliador MNR</span>
        </div>
      </header>

      <div className={styles.workspace}>
        <aside className={styles.sidebar} aria-label="Projetos para avaliar">
          <div className={styles.sidebarControls}>
            <label className={styles.searchBox}>
              <span aria-hidden="true">⌕</span>
              <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Título ou estande..." />
            </label>
            <div className={styles.filters} role="group" aria-label="Filtrar projetos">
              {(["todos", "pendentes", "concluidos"] as const).map((value) => (
                <button key={value} className={filter === value ? styles.filterActive : ""} onClick={() => setFilter(value)}>
                  {value === "concluidos" ? "Concluídos" : value[0].toUpperCase() + value.slice(1)}
                </button>
              ))}
            </div>
            <p className={styles.progressCount}>{submitted.size}/{mockProjects.length} avaliados nesta sessão</p>
          </div>
          <div className={styles.projectList}>
            {filteredProjects.map((project) => {
              const complete = isSubmitted(project);
              const scoreValues = scores[project.id] ?? {};
              const projectAverage = Object.values(scoreValues).length
                ? (Object.values(scoreValues).reduce((sum, value) => sum + value, 0) / Object.values(scoreValues).length).toFixed(1)
                : null;
              return (
                <button key={project.id} className={`${styles.projectItem} ${selectedId === project.id ? styles.selectedProject : ""}`} onClick={() => { setSelectedId(project.id); setNotice(""); }}>
                  <span className={styles.projectItemTop}><span>{project.stand} {complete && <span className={styles.check}>✓</span>}</span><span className={styles.projectAverage}>{projectAverage ?? "›"}</span></span>
                  <strong>{project.name}</strong>
                  <small>{project.team} · {project.location}</small>
                </button>
              );
            })}
            {filteredProjects.length === 0 && <p className={styles.emptyList}>Nenhum projeto corresponde à busca.</p>}
          </div>
        </aside>

        <section className={styles.content}>
          {selected ? (
            <div className={styles.formColumn}>
              <div className={styles.demoBanner} role="note">
                <span aria-hidden="true">◈</span>
                Demonstração com projetos fictícios. As alterações ficam apenas nesta sessão; não são salvas no backend.
              </div>

              <section className={styles.card}>
                <div className={styles.projectHeader}>
                  <div>
                    <p className={styles.eyebrow}>{selected.stand} · {selected.category}</p>
                    <h1>{selected.name}</h1>
                    <p className={styles.projectSubtitle}>{selected.team} — {selected.institution}</p>
                    <p className={styles.projectLocation}>{selected.location}</p>
                  </div>
                  {isSubmitted(selected) && <span className={styles.completedBadge}>✓ Avaliado</span>}
                </div>
                <div className={styles.materialLinks}>
                  <a href={selected.videoUrl} target="_blank" rel="noreferrer">▷ Ver vídeo</a>
                  <a href={selected.pdfUrl} target="_blank" rel="noreferrer">▤ Ver resumo/artigo</a>
                </div>
              </section>

              <section className={styles.card} aria-labelledby="materials-title">
                <div className={styles.sectionHeading}>
                  <span className={styles.headingIcon}>1</span>
                  <div><h2 id="materials-title">Conferência inicial dos materiais</h2><p>O trabalho submetido apresentou os materiais previstos no Manual de Inscrição da MNR 2026?</p></div>
                </div>
                <div className={styles.materialQuestions}>
                  <MaterialQuestion label="Resumo/Artigo" value={answeredMaterials.article} onChange={(value) => setMaterialAnswer("article", value)} />
                  <MaterialQuestion label="Vídeo" value={answeredMaterials.video} onChange={(value) => setMaterialAnswer("video", value)} />
                </div>
                {!criteriaEnabled && <p className={styles.gateMessage}>A ficha determina que os critérios seguintes só sejam avaliados quando Resumo/Artigo e Vídeo estiverem marcados como “Sim”.</p>}
              </section>

              <div className={`${styles.criteriaHeader} ${!criteriaEnabled ? styles.disabledHeading : ""}`}>
                <div><span className={styles.headingIcon}>★</span><div><h2>Critérios de avaliação</h2><p>Questões 1–13 · escolha um nível de 1 a 5 em cada critério</p><p className={styles.criteriaTip}>Dica: passe o mouse, toque ou navegue até um nível para consultar a descrição.</p></div></div>
                <span className={styles.scoreSummary}>{answeredCount}/{evaluationCriteria.length} respondidos</span>
              </div>

              {evaluationCriteria.map((criterion) => (
                <section key={criterion.id} className={`${styles.card} ${styles.criterionCard} ${!criteriaEnabled ? styles.disabledCard : ""}`} aria-labelledby={`criterion-${criterion.id}`}>
                  <div className={styles.criterionTitleRow}>
                    <span className={styles.questionNumber}>Q{criterion.question}</span>
                    <div><h3 id={`criterion-${criterion.id}`}>{criterion.title}</h3><p>{criterion.prompt}</p></div>
                  </div>
                  {(() => {
                    const guidanceLevel = criterion.levels.find((level) => level.value === (activeGuidance[criterion.id] ?? selectedScores[criterion.id]));
                    return (
                      <div
                        className={styles.rubricArea}
                        onMouseLeave={() => setActiveGuidance((current) => {
                          const next = { ...current };
                          delete next[criterion.id];
                          return next;
                        })}
                      >
                        <div className={styles.levelGrid} role="radiogroup" aria-label={`Nível para ${criterion.title}`}>
                          {criterion.levels.map((level) => (
                            <button
                              key={level.value}
                              type="button"
                              role="radio"
                              aria-checked={selectedScores[criterion.id] === level.value}
                              aria-disabled={!criteriaEnabled}
                              aria-label={`${level.value} – ${level.label}`}
                              aria-describedby={guidanceLevel ? `guidance-${criterion.id}` : undefined}
                              disabled={isSubmitted(selected)}
                              onMouseEnter={() => setActiveGuidance((current) => ({ ...current, [criterion.id]: level.value }))}
                              onFocus={() => setActiveGuidance((current) => ({ ...current, [criterion.id]: level.value }))}
                              onBlur={() => setActiveGuidance((current) => {
                                const next = { ...current };
                                delete next[criterion.id];
                                return next;
                              })}
                              onClick={() => {
                                if (!criteriaEnabled || isSubmitted(selected)) return;
                                setScores((current) => ({ ...current, [selected.id]: { ...(current[selected.id] ?? {}), [criterion.id]: level.value } }));
                                setNotice("");
                              }}
                              className={`${styles.levelOption} ${selectedScores[criterion.id] === level.value ? styles.levelSelected : ""}`}
                            >
                              <span className={styles.levelLabel}><b>{level.value}</b><span>{level.label}</span></span>
                            </button>
                          ))}
                        </div>
                        <div className={`${styles.guidanceDisclosure} ${guidanceLevel ? styles.guidanceDisclosureOpen : ""}`}>
                          <div
                            id={`guidance-${criterion.id}`}
                            className={`${styles.guidancePanel} ${guidanceLevel ? styles.guidancePanelOpen : ""}`}
                            aria-hidden={!guidanceLevel}
                          >
                            {guidanceLevel && <><strong>{guidanceLevel.value} · {guidanceLevel.label}</strong><p>{guidanceLevel.description}</p></>}
                          </div>
                        </div>
                      </div>
                    );
                  })()}
                </section>
              ))}

              <section className={`${styles.card} ${!criteriaEnabled ? styles.disabledCard : ""}`}>
                <div className={styles.criterionTitleRow}>
                  <span className={styles.questionNumber}>Q14</span>
                  <div><h3>Menção Honrosa</h3><p>O projeto tem potencial para receber Menção Honrosa?</p></div>
                </div>
                <YesNo value={selected ? honorableMention[selected.id] ?? null : null} disabled={!criteriaEnabled || isSubmitted(selected)} onChange={(value) => selected && setHonorableMention((current) => ({ ...current, [selected.id]: value }))} />
              </section>

              <section className={styles.card}>
                <div className={styles.criterionTitleRow}>
                  <span className={styles.questionNumber}>Q15</span>
                  <div><h3>Comentários para a organização</h3><p>Campo sigiloso, destinado exclusivamente à organização.</p></div>
                </div>
                <textarea className={styles.textArea} value={selected ? organizationNotes[selected.id] ?? "" : ""} onChange={(event) => selected && setOrganizationNotes((current) => ({ ...current, [selected.id]: event.target.value }))} placeholder="Comentários sigilosos para a organização..." disabled={isSubmitted(selected)} />
              </section>

              <section className={styles.card}>
                <label className={styles.feedbackLabel} htmlFor="participant-feedback">Feedback para o participante <span>(opcional)</span></label>
                <textarea id="participant-feedback" className={styles.textArea} value={selectedFeedback(participantFeedback, selected)} onChange={(event) => selected && setParticipantFeedback((current) => ({ ...current, [selected.id]: event.target.value }))} placeholder="Registre pontos fortes, oportunidades de melhoria e sugestões para a equipe." disabled={isSubmitted(selected)} />
              </section>

              <div className={styles.summaryBar}>
                <span>Média simples dos critérios respondidos</span>
                <strong>{average}</strong>
              </div>

              {notice && <p className={notice.startsWith("Avaliação") ? styles.successNotice : styles.formNotice} role="status">{notice}</p>}
              <button className={styles.submitButton} onClick={submitEvaluation} disabled={isSubmitted(selected)}>
                {isSubmitted(selected) ? "✓ Avaliação registrada nesta sessão" : "✓ Registrar avaliação (demonstração)"}
              </button>
              <p className={styles.apiNote}>Nesta entrega, a interface usa estado local e dados de demonstração. O envio para API será integrado quando os endpoints de avaliação estiverem disponíveis.</p>
            </div>
          ) : (
            <div className={styles.noSelection}>Selecione um projeto na lista para iniciar a avaliação.</div>
          )}
        </section>
      </div>
    </main>
  );
}

function selectedFeedback(feedback: Record<number, string>, project: DemoProject | null) {
  return project ? feedback[project.id] ?? "" : "";
}

function MaterialQuestion({ label, value, onChange }: { label: string; value: boolean | null; onChange: (answer: boolean) => void }) {
  return (
    <div className={styles.materialQuestion}>
      <span>{label} apresentado?</span>
      <YesNo value={value} onChange={onChange} />
    </div>
  );
}

function YesNo({ value, onChange, disabled = false }: { value: boolean | null; onChange: (answer: boolean) => void; disabled?: boolean }) {
  return (
    <div className={styles.yesNo} role="radiogroup" aria-label="Resposta sim ou não">
      {[true, false].map((answer) => (
        <button key={String(answer)} type="button" role="radio" aria-checked={value === answer} className={value === answer ? styles.yesNoSelected : ""} disabled={disabled} onClick={() => onChange(answer)}>{answer ? "Sim" : "Não"}</button>
      ))}
    </div>
  );
}
