package ifrs.edu.avaliacao_mnr.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_evaluator_assignments",
        uniqueConstraints = @UniqueConstraint(name = "uk_project_evaluator_assignment",
                columnNames = {"project_id", "evaluator_id"}))
public class ProjectEvaluatorAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @OneToOne(mappedBy = "assignment", fetch = FetchType.LAZY)
    private Evaluation evaluation;

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(User evaluator) {
        this.evaluator = evaluator;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }
}
