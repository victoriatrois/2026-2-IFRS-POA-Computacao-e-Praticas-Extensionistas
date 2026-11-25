"use client";

import styles from "./DashboardRefined.module.css";

/**
 * ============================================================================
 * ATENÇÃO TAYLAN (PR / Issue #91):
 * Utilize ESTE componente e seus estilos para os níveis N1, N2, N3 e N4 na tela
 * de Diagnóstico de Pré-Avaliação que você está desenvolvendo na Issue #91.
 * 
 * Os prints anexados na Issue #91 estão desatualizados. As cores e formatação
 * oficiais aprovadas no Penpot para a Issue #90 e #91 são:
 * - N1: Fundo #dbeafe (azul claro), Texto #0284c7 (font-weight 800)
 * - N2: Fundo #fef3c7 (amarelo claro), Texto #b45309 (font-weight 800)
 * - N3: Fundo #fee2e2 (vermelho claro), Texto #b91c1c (font-weight 800)
 * - N4: Fundo #ede9fe (roxo claro), Texto #6d28d9 (font-weight 800)
 * ============================================================================
 */

export type ProjectLevel = "N1" | "N2" | "N3" | "N4" | string;

interface LevelBadgeProps {
  level: ProjectLevel | null | undefined;
}

export default function LevelBadge({ level }: LevelBadgeProps) {
  const normalizedLevel = (level || "").trim().toUpperCase();

  let badgeClass = styles.levelBadgeN1;
  let label = normalizedLevel || "N1";

  if (normalizedLevel.includes("N1") || normalizedLevel.includes("FUNDAMENTAL")) {
    badgeClass = styles.levelBadgeN1;
    label = "N1";
  } else if (normalizedLevel.includes("N2") || normalizedLevel.includes("MÉDIO") || normalizedLevel.includes("MEDIO")) {
    badgeClass = styles.levelBadgeN2;
    label = "N2";
  } else if (normalizedLevel.includes("N3") || normalizedLevel.includes("TÉCNICO") || normalizedLevel.includes("TECNICO")) {
    badgeClass = styles.levelBadgeN3;
    label = "N3";
  } else if (normalizedLevel.includes("N4") || normalizedLevel.includes("SUPERIOR")) {
    badgeClass = styles.levelBadgeN4;
    label = "N4";
  }

  return (
    <span className={`${styles.levelBadge} ${badgeClass}`} aria-label={`Nível ${label}`}>
      {label}
    </span>
  );
}
