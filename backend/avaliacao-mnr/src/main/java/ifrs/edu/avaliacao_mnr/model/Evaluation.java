package ifrs.edu.avaliacao_mnr.model;

import ifrs.edu.avaliacao_mnr.enums.EvaluationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "evaluations")
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", unique = true)
    private ProjectEvaluatorAssignment assignment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvaluationStatus status;

    @Column(name = "article_present")
    private Boolean articlePresent;

    @Column(name = "video_present")
    private Boolean videoPresent;

    @Column(name = "honorable_mention")
    private Boolean honorableMention;

    @Column(name = "organization_notes", columnDefinition = "TEXT")
    private String organizationNotes;

    @Column(name = "participant_feedback", columnDefinition = "TEXT")
    private String participantFeedback;

    @OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<EvaluationScore> scores = new java.util.ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Evaluation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(User evaluator) {
        this.evaluator = evaluator;
    }

    public ProjectEvaluatorAssignment getAssignment() {
        return assignment;
    }

    public void setAssignment(ProjectEvaluatorAssignment assignment) {
        this.assignment = assignment;
    }

    public EvaluationStatus getStatus() {
        return status;
    }

    public void setStatus(EvaluationStatus status) {
        this.status = status;
    }

    public Boolean getArticlePresent() { return articlePresent; }
    public void setArticlePresent(Boolean articlePresent) { this.articlePresent = articlePresent; }
    public Boolean getVideoPresent() { return videoPresent; }
    public void setVideoPresent(Boolean videoPresent) { this.videoPresent = videoPresent; }
    public Boolean getHonorableMention() { return honorableMention; }
    public void setHonorableMention(Boolean honorableMention) { this.honorableMention = honorableMention; }
    public String getOrganizationNotes() { return organizationNotes; }
    public void setOrganizationNotes(String organizationNotes) { this.organizationNotes = organizationNotes; }
    public String getParticipantFeedback() { return participantFeedback; }
    public void setParticipantFeedback(String participantFeedback) { this.participantFeedback = participantFeedback; }
    public java.util.List<EvaluationScore> getScores() { return scores; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
