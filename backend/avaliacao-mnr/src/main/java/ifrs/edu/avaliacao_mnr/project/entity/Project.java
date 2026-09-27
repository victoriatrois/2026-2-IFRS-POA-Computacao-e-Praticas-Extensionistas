package ifrs.edu.avaliacao_mnr.project.entity;

import ifrs.edu.avaliacao_mnr.event.entity.Event;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private String name;

    @Column(name = "pdf_url", columnDefinition = "TEXT")
    private String pdfUrl;

    @Column(length = 50)
    private String level;

    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl;

    @Column(name = "participant_name")
    private String participantName;

    @Column(name = "participant_cpf", length = 20)
    private String participantCpf;

    @Column(name = "participant_email")
    private String participantEmail;

    @Column(name = "institution_name")
    private String institutionName;

    @Column(name = "marked_for_review", nullable = false)
    private boolean markedForReview;

    @Column(nullable = false)
    private boolean validated;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Project() {
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getParticipantCpf() {
        return participantCpf;
    }

    public void setParticipantCpf(String participantCpf) {
        this.participantCpf = participantCpf;
    }

    public String getParticipantEmail() {
        return participantEmail;
    }

    public void setParticipantEmail(String participantEmail) {
        this.participantEmail = participantEmail;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public boolean isMarkedForReview() {
        return markedForReview;
    }

    public void setMarkedForReview(boolean markedForReview) {
        this.markedForReview = markedForReview;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

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
