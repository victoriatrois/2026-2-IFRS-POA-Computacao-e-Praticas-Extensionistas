import type { Metadata } from "next";
import AppHeader from "@/components/AppHeader";
import LoginForm from "./LoginForm";
import styles from "./page.module.css";

export const metadata: Metadata = {
  title: "Login | Avaliação MNR",
  description: "Entre na plataforma de avaliação da Mostra Nacional de Robótica.",
};

export default function LoginPage() {
  return (
    <div className={styles.page}>
      <AppHeader variant="login" />
      <main className={styles.main}>
        <section className={styles.card} aria-labelledby="login-title">
          <h1 id="login-title">Avaliação MNR</h1>
          <p className={styles.description}>Insira suas credenciais para entrar</p>
          <LoginForm />
        </section>
      </main>
    </div>
  );
}
