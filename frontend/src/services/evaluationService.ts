import { apiFetch } from "@/lib/auth";

export interface EvaluationSubmitData {
  assignmentId?: number;
  articlePresent: boolean;
  videoPresent: boolean;
  honorableMention: boolean;
  organizationNotes?: string;
  participantFeedback?: string;
  scores: Record<number, number>;
}

export async function submitEvaluationRequest(data: EvaluationSubmitData): Promise<Response> {
  const body = {
    assignment: data.assignmentId ? { id: data.assignmentId } : undefined,
    status: "IN_PROGRESS",
    articlePresent: data.articlePresent,
    videoPresent: data.videoPresent,
    honorableMention: data.honorableMention,
    organizationNotes: data.organizationNotes?.trim() || null,
    participantFeedback: data.participantFeedback?.trim() || null,
  };

  return apiFetch("/api/evaluations", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });
}
