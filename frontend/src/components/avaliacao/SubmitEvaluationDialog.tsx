"use client";

import { useEffect, useRef } from "react";
import styles from "./SubmitEvaluationDialog.module.css";

export interface SubmitEvaluationDialogProps {
  isOpen: boolean;
  hasComment: boolean;
  isSubmitting?: boolean;
  onClose: () => void;
  onReturnToFeedback: () => void;
  onConfirm: () => void;
}

export default function SubmitEvaluationDialog({
  isOpen,
  hasComment,
  isSubmitting = false,
  onClose,
  onReturnToFeedback,
  onConfirm,
}: SubmitEvaluationDialogProps) {
  const modalRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isOpen) return;

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape" && !isSubmitting) {
        onClose();
      }
    }

    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, isSubmitting, onClose]);

  if (!isOpen) return null;

  const title = hasComment
    ? "Enviar avaliação"
    : "Enviar avaliação sem comentário";

  const description = hasComment
    ? "Após enviar a avaliação você não poderá alterá-la."
    : "Após enviar a avaliação sem comentário não será possível adicioná-lo.";

  const confirmLabel = hasComment ? "Enviar" : "Enviar sem comentário";

  function handleBack() {
    if (isSubmitting) return;
    if (hasComment) {
      onClose();
    } else {
      onReturnToFeedback();
    }
  }

  return (
    <div
      className={styles.overlay}
      onClick={(e) => {
        if (e.target === e.currentTarget && !isSubmitting) onClose();
      }}
      role="dialog"
      aria-modal="true"
      aria-labelledby="submit-dialog-title"
      aria-describedby="submit-dialog-description"
    >
      <div className={styles.modal} ref={modalRef}>
        <button
          type="button"
          className={styles.closeButton}
          onClick={onClose}
          disabled={isSubmitting}
          aria-label="Fechar diálogo"
        >
          <svg
            width="18"
            height="18"
            viewBox="0 0 18 18"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            <line x1="4" y1="4" x2="14" y2="14" />
            <line x1="14" y1="4" x2="4" y2="14" />
          </svg>
        </button>

        <h2 id="submit-dialog-title" className={styles.title}>
          {title}
        </h2>
        <p id="submit-dialog-description" className={styles.description}>
          {description}
        </p>

        <div className={styles.actions}>
          <button
            type="button"
            className={styles.backButton}
            onClick={handleBack}
            disabled={isSubmitting}
          >
            Voltar
          </button>
          <button
            type="button"
            className={styles.confirmButton}
            onClick={onConfirm}
            disabled={isSubmitting}
          >
            {isSubmitting ? "Enviando..." : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
