package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.model.EvaluationCriteria;
import ifrs.edu.avaliacao_mnr.model.Event;
import ifrs.edu.avaliacao_mnr.repository.EvaluationCriteriaRepository;
import ifrs.edu.avaliacao_mnr.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EvaluationCriteriaService {

    private static final List<String> DEFAULT_CRITERIA = List.of(
            "Resumo/Artigo: clareza e rigor científico", "Resumo/Artigo: adequação ao modelo oficial",
            "Objetivo", "Fase do projeto", "Protótipo/experimento", "Programação/desenvolvimento",
            "Resultados e conclusões", "Pilares da robótica", "Inovação", "Apresentação", "Professor",
            "Relevância ambiental e social", "Interdisciplinaridade");

    private final EvaluationCriteriaRepository criteriaRepository;
    private final EventRepository eventRepository;

    public EvaluationCriteriaService(EvaluationCriteriaRepository criteriaRepository, EventRepository eventRepository) {
        this.criteriaRepository = criteriaRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public List<EvaluationCriteria> ensureDefaults(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + eventId));
        List<EvaluationCriteria> existing = criteriaRepository.findByEventIdOrderByOrderAsc(eventId);
        for (int index = 0; index < DEFAULT_CRITERIA.size(); index++) {
            int order = index + 1;
            if (existing.stream().noneMatch(criterion -> criterion.getOrder() == order)) {
                EvaluationCriteria criterion = new EvaluationCriteria();
                criterion.setEvent(event);
                criterion.setName(DEFAULT_CRITERIA.get(index));
                criterion.setMaxScore(5.0);
                criterion.setWeight(1.0);
                criterion.setOrder(order);
                criteriaRepository.save(criterion);
            }
        }
        return criteriaRepository.findByEventIdOrderByOrderAsc(eventId);
    }
}
