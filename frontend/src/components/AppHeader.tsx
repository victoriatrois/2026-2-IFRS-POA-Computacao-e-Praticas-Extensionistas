import Image from "next/image";
import Link from "next/link";
import type { ReactNode } from "react";
import styles from "./AppHeader.module.css";

export type AdminHeaderItem = "home" | "panel" | "evaluate" | "mentions" | "feedback" | "export";
export type AppHeaderVariant = "login" | "evaluator" | "admin";

interface AppHeaderProps {
  variant: AppHeaderVariant;
  activeItem?: AdminHeaderItem;
  userName?: string;
}

const adminItems: { id: AdminHeaderItem; label: string; href?: string; icon: string }[] = [
  { id: "home", label: "Home", href: "/", icon: "home" },
  { id: "panel", label: "Painel", href: "/dashboard", icon: "panel" },
  { id: "evaluate", label: "Avaliar", href: "/avaliacao", icon: "evaluate" },
  { id: "mentions", label: "Menções", icon: "mentions" },
  { id: "feedback", label: "Feedback", icon: "feedback" },
  { id: "export", label: "Exportar", icon: "export" },
];

function Icon({ name }: { name: string }) {
  const paths: Record<string, ReactNode> = {
    home: <><path d="m3 7 5-4 5 4v6H9V9H7v4H3z" /><path d="M1.5 7.5 8 2l6.5 5.5" /></>,
    panel: <><rect x="2" y="2" width="5" height="5" rx="1" /><rect x="9" y="2" width="5" height="5" rx="1" /><rect x="2" y="9" width="5" height="5" rx="1" /><rect x="9" y="9" width="5" height="5" rx="1" /></>,
    evaluate: <><rect x="3" y="3" width="10" height="11" rx="1.5" /><path d="M6 2v3m4-3v3M5.5 8h5M5.5 11h3" /></>,
    mentions: <><path d="M8 2 9.8 5.7 14 6.3l-3 2.9.7 4.2L8 11.5l-3.7 1.9.7-4.2-3-2.9 4.2-.6z" /></>,
    feedback: <path d="m2 2 12 5.5-5.2 1.3L7.5 14zM8.8 8.8l3.3 3.3" />,
    export: <><path d="M8 2v8m-3-3 3 3 3-3" /><path d="M3 10v3h10v-3" /></>,
    logout: <><path d="M6 2.5H3.5v11H6M9.5 4.5 13 8l-3.5 3.5M13 8H6" /></>,
  };
  return <svg className={styles.icon} viewBox="0 0 16 16" aria-hidden="true">{paths[name]}</svg>;
}

function Account({ userName }: { userName: string }) {
  const initials = userName.split(/\s+/).map((part) => part[0]).slice(0, 2).join("").toUpperCase();
  return (
    <div className={styles.account}>
      <span className={styles.avatar} aria-hidden="true">{initials}</span>
      <span className={styles.userName}>{userName}</span>
      {/* Sign-out stays presentational until authentication is connected. */}
      <span className={styles.signOut} aria-label="Sair (demonstração)"><Icon name="logout" />Sair</span>
    </div>
  );
}

export default function AppHeader({ variant, activeItem, userName }: AppHeaderProps) {
  const title = variant === "login" ? "Login" : "Gestão de Avaliações";
  const displayedName = userName ?? (variant === "evaluator" ? "Avaliador MNR" : "Ana Costa");

  return (
    <header className={`${styles.header} ${styles[variant]}`}>
      <div className={styles.identity}>
        <Link className={styles.brand} href={variant === "evaluator" ? "/avaliador" : "/"} aria-label="MNR, início">
          <Image src="/avaliador/mnr-logo.png" alt="MNR" width={93} height={34} priority />
        </Link>
        <span className={styles.separator} aria-hidden="true" />
        <span className={styles.productName}>{title}</span>
      </div>

      {variant === "admin" && (
        <nav className={styles.navigation} aria-label="Navegação administrativa">
          {adminItems.map((item) => {
            const isActive = activeItem === item.id;
            const content = <><Icon name={item.icon} /><span>{item.label}</span></>;
            return item.href ? (
              <Link key={item.id} className={`${styles.navItem} ${isActive ? styles.active : ""}`} href={item.href} aria-current={isActive ? "page" : undefined}>
                {content}
              </Link>
            ) : (
              <span key={item.id} className={`${styles.navItem} ${styles.unavailable}`} aria-disabled="true" title="Disponível em uma etapa futura">
                {content}
              </span>
            );
          })}
        </nav>
      )}

      {(variant === "evaluator" || variant === "admin") && <Account userName={displayedName} />}
    </header>
  );
}
