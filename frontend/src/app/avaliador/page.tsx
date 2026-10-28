import Image from "next/image";
import type { Metadata } from "next";
import AppHeader from "@/components/AppHeader";
import WorkSearch from "./WorkSearch";
import styles from "./page.module.css";
import { evaluatorDashboardMock as dashboard } from "./mockData";

export const metadata: Metadata = {
  title: "Início do avaliador | MNR",
  description: "Resumo das avaliações atribuídas ao avaliador da MNR.",
};

const completionPercentage = Math.round(
  (dashboard.completedCount / dashboard.assignedCount) * 100,
);

export default function EvaluatorHomePage() {
  const initials = dashboard.evaluatorName
    .split(" ")
    .map((part) => part[0])
    .slice(0, 2)
    .join("")
    .toUpperCase();

  return (
    <div className={styles.page}>
      <AppHeader variant="evaluator" userName={dashboard.evaluatorName} />

      <main className={styles.main}>
        <section className={styles.heroCard} aria-labelledby="welcome-title">
          <div className={styles.heroIntro}>
            <h1 id="welcome-title">Olá, {dashboard.evaluatorName}</h1>
            <p>
              Você está na {dashboard.eventStage}. Escolha um trabalho para iniciar sua avaliação.
            </p>
          </div>

          <div className={styles.stats} aria-label="Resumo das avaliações">
            <article className={`${styles.statCard} ${styles.assignedCard}`}>
              <strong className={styles.statNumber}>{dashboard.assignedCount}</strong>
              <div className={styles.statHeading}>
                <h2>Trabalhos atribuídos</h2>
                <span className={styles.smallAvatar} aria-hidden="true">{initials}</span>
              </div>
              <p className={styles.statCaption}>a você</p>
            </article>

            <article className={styles.statCard}>
              <strong className={styles.statNumber}>{dashboard.pendingCount}</strong>
              <div className={styles.statHeading}>
                <h2>Pendentes de avaliação</h2>
                <Image src="/avaliador/clock.svg" alt="" width={18} height={18} />
              </div>
              <p className={styles.statCaption}>{dashboard.completedCount} concluídos</p>
            </article>

            <article className={styles.statCard}>
              <strong className={`${styles.statNumber} ${styles.dateNumber}`}>
                {dashboard.dueDate}
              </strong>
              <div className={styles.statHeading}>
                <h2>Prazo final</h2>
                <Image src="/avaliador/clock.svg" alt="" width={18} height={18} />
              </div>
              <p className={styles.statCaption}>{dashboard.deadlineTime}</p>
            </article>
          </div>

          <div className={styles.progressSection}>
            <div
              className={styles.progressTrack}
              role="progressbar"
              aria-label="Trabalhos concluídos"
              aria-valuemin={0}
              aria-valuemax={dashboard.assignedCount}
              aria-valuenow={dashboard.completedCount}
            >
              <span style={{ width: `${completionPercentage}%` }} />
            </div>
            <p>{dashboard.completedCount} de {dashboard.assignedCount} concluídos</p>
            <WorkSearch />
          </div>
        </section>

        <section className={styles.guidanceCard} aria-labelledby="guidance-title">
          <h2 id="guidance-title">Como começar</h2>
          <ul>
            <li>
              <Image src="/avaliador/check.svg" alt="" width={16} height={16} />
              <span>Acesse a lista completa de trabalhos atribuídos para selecionar o primeiro projeto.</span>
            </li>
            <li>
              <Image src="/avaliador/check.svg" alt="" width={16} height={16} />
              <span>Utilize os filtros para priorizar os pendentes e organizar sua sessão de avaliação.</span>
            </li>
            <li>
              <Image src="/avaliador/check.svg" alt="" width={16} height={16} />
              <span>Escolha um trabalho na lista lateral para abrir seu formulário de avaliação.</span>
            </li>
          </ul>
        </section>
      </main>
    </div>
  );
}
