package ifrs.edu.avaliacao_mnr.project.dto;

/* T: Optei por RECORD, porque estava relendo algumas anotações
e vi que é um tipo especial de classe, usada para representar dados imutáveis.
Fica mais enxuto e abstraído, ideal para classes que só carregam dados, mas
não tem lógica complexa. O construtor, getters e setters, equals, hashcode e toString são gerados automaticamente.
*/
public record ProjectImportDTO(
        String registrationNumber,
        String name,
        String team,
        String level,
        String videoUrl,
        String institution
) {
}
