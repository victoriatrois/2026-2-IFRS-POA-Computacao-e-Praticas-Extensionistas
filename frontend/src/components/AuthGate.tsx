"use client";

import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { getHomeForRole } from "@/lib/auth";
import { useAuth } from "./AuthProvider";

export default function AuthGate({ children }: { children: React.ReactNode }) {
  const { user, isReady } = useAuth();
  const pathname = usePathname();
  const router = useRouter();
  const isLoginPage = pathname === "/login";

  useEffect(() => {
    if (!isReady) return;
    if (isLoginPage && user) {
      router.replace(getHomeForRole(user.role));
    } else if (!isLoginPage && !user) {
      router.replace("/login");
    }
  }, [isLoginPage, isReady, router, user]);

  if (!isReady || (isLoginPage && user) || (!isLoginPage && !user)) {
    return (
      <main className="auth-loading" aria-busy="true" aria-label="Carregando sessão">
        <span />
      </main>
    );
  }

  return children;
}
