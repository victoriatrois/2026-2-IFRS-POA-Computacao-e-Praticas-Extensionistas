package ifrs.edu.avaliacao_mnr.project.service;

import ifrs.edu.avaliacao_mnr.project.dto.ProjectImportDTO;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/*
T: O que esta classe faz?
Utilitário que centraliza o mapeamento das colunas da planilha.
Responsável por normalizar os nomes dos cabeçalhos, tolerar variações de escrita (aliases/substrings) e garantir que tanto a leitura de CSV quanto a de Excel sigam as mesmas regras de extração.
*/
final class ProjectRowMapper {
    /*
     DICIONÁRIO DE MAPEAMENTO DE COLUNAS (De -> Para)

     As constantes abaixo representam os campos internos do nosso sistema (DTO). O bloco estático inicializa as regras de como encontrar essas colunas na planilha:

     1. EXACT_ALIASES (Busca Exata): Lista de possíveis nomes exatos para a coluna.
     - O primeiro item da lista é sempre o nome real usado na exportação atual do sistema.
     - Os demais são variações seguras para evitar quebra caso a planilha mude.

     2. CONTAINS_FALLBACK (Busca Parcial): Usado caso a busca exata falhe.
     - Procura por palavras-chave (ex: "pdf", "cpf") dentro do cabeçalho.
     - Aplicado apenas em campos exclusivos. Não é usado para campos como "nome", pois a palavra aparece em muitas colunas (participante, projeto, instituição), o que causaria falsos positivos.

     NOTA: Atualmente a coluna de CPF ("person.cpf") vem vazio na exportação. Por isso, é melhor avaliarmos o uso de um e-mail ou UUID como identificador principal no futuro.
     */

    static final String PROJECT_NAME = "projectName";
    static final String PDF_URL = "pdfUrl";
    static final String LEVEL = "level";
    static final String VIDEO_URL = "videoUrl";
    static final String PARTICIPANT_NAME = "participantName";
    static final String PARTICIPANT_CPF = "participantCpf";
    static final String PARTICIPANT_EMAIL = "participantEmail";
    static final String INSTITUTION_NAME = "institutionName";

    private static final Map<String, List<String>> EXACT_ALIASES = new LinkedHashMap<>();
    private static final Map<String, List<String>> CONTAINS_FALLBACK = new LinkedHashMap<>();

    static {
        EXACT_ALIASES.put(PROJECT_NAME, List.of(
                "name", "titulo", "nome_do_projeto", "project_name", "nome_projeto"));

        EXACT_ALIASES.put(PDF_URL, List.of(
                "resumo_artigo_pdf_obrigatorio", "pdf_url", "resumo_pdf", "artigo_pdf"));

        EXACT_ALIASES.put(LEVEL, List.of(
                "qual_e_o_nivel_do_trabalho_inscrito_obrigatorio", "level", "nivel"));

        EXACT_ALIASES.put(VIDEO_URL, List.of(
                "endereco_youtube_link_para_o_video_de_apresentacao_obrigatorio",
                "video_url", "youtube", "video"));

        EXACT_ALIASES.put(PARTICIPANT_NAME, List.of(
                "person_name", "participant_name", "nome_do_participante", "responsavel", "team", "equipe"));

        EXACT_ALIASES.put(PARTICIPANT_CPF, List.of(
                "person_cpf", "cpf", "participant_cpf", "registration_number", "id"));

        EXACT_ALIASES.put(PARTICIPANT_EMAIL, List.of(
                "person_email", "email", "participant_email", "e_mail"));

        EXACT_ALIASES.put(INSTITUTION_NAME, List.of(
                "institution_name", "institution", "instituicao", "escola"));

        CONTAINS_FALLBACK.put(PDF_URL, List.of("pdf"));
        CONTAINS_FALLBACK.put(LEVEL, List.of("nivel"));
        CONTAINS_FALLBACK.put(VIDEO_URL, List.of("video", "youtube"));
        CONTAINS_FALLBACK.put(PARTICIPANT_CPF, List.of("cpf"));
        CONTAINS_FALLBACK.put(PARTICIPANT_EMAIL, List.of("email"));
        CONTAINS_FALLBACK.put(INSTITUTION_NAME, List.of("instit"));
    }

    private ProjectRowMapper() {
    }

    /*
    # T: Normaliza um cabeçalho para comparação
    - Remove acentos, deixa minúsculo e tranformando caracteres não alfanuméricos em underline
        ex: "Qual é o Nível?" vira "qual_e_o_nivel"
     */
    static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String semAcentos = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String minusculo = semAcentos.toLowerCase(Locale.ROOT);
        String comUnderscore = minusculo.replaceAll("[^a-z0-9]+", "_");
        return comUnderscore.replaceAll("^_+", "").replaceAll("_+$", "");
    }

    /*
     T: # Mapeia os cabeçalhos originais do arquivo para os campos internos do sistema (DTO).
     - Para garantir performance na leitura de planilhas com milhares de registros, o método é executado uma única vez por arquivo (na leitura da 1ª linha) e não a cada iteração de linha.

     ## Fluxo de resolução:
        - Normaliza os cabeçalhos do arquivo recebido (remove acentos, espaços, etc.)
        - Para cada campo necessário no sistema, tenta encontrar a coluna ideal usando busca exata.
        - Se a busca exata falhar, tenta encontrar por palavra-chave (busca parcial/substring).

     Notas:
     @param originalHeaders -> Lista com os nomes dos cabeçalhos exatamente como aparecem no arquivo.
     @return -> Um mapa onde a Chave é o campo do sistema (ex: "projectName") e o Valor é o nome real da coluna lida no arquivo (ex: "Nome do Projeto").
            OBS: O valor será NULL caso a coluna correspondente não seja encontrada na planilha.
     */
    static Map<String, String> resolveHeaderMap(Collection<String> originalHeaders) {
        Map<String, String> normalizadoParaOriginal = new LinkedHashMap<>();
        for (String original : originalHeaders) {
            normalizadoParaOriginal.putIfAbsent(normalize(original), original);
        }

        Map<String, String> campoParaCabecalho = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : EXACT_ALIASES.entrySet()) {
            String campo = entry.getKey();
            String encontrado = matchExato(entry.getValue(), normalizadoParaOriginal);
            if (encontrado == null) {
                encontrado = matchPorSubstring(CONTAINS_FALLBACK.get(campo), normalizadoParaOriginal);
            }
            campoParaCabecalho.put(campo, encontrado);
        }
        return campoParaCabecalho;
    }

    /*
     T: # Converte os dados de uma única linha da planilha em um objeto ProjectImportDTO.
     - A grande vantagem deste método é que ele serve tanto para arquivos CSV quanto para planilhas do Excel. Ele não precisa saber qual é o formato do arquivo: ele apenas recebe uma regra pronta (o métodd valorPorCabecalho) que já sabe como pegar a info correta da coluna na linha que está sendo lida no momento.

     Regra de Validação: Linhas que não possuam o título do projeto preenchido são consideradas dados "sujos" ou vazios. Nesses casos, a conversão é abortada para não gerar lixo no banco de dados.

     Notas:
      @param campoParaCabecalho -> O mapa resolvido pelo {@link #resolveHeaderMap}, ligando os campos do sistema aos nomes reais das colunas da planilha.
      @param valorPorCabecalho -> Uma função lambda que recebe o nome de um cabeçalho e devolve o valor contido na célula da linha atual.
     @return -> O objeto ProjectImportDTO preenchido e com dados formatados ou NULL.
     */
    static ProjectImportDTO buildDto(Map<String, String> campoParaCabecalho, Function<String, String> valorPorCabecalho) {
        String projectName = limpar(resolver(campoParaCabecalho, valorPorCabecalho, PROJECT_NAME));
        if (projectName == null) {
            return null; // sem título -> linha "suja", deve ser ignorada
        }

        return new ProjectImportDTO(
                projectName,
                limpar(resolver(campoParaCabecalho, valorPorCabecalho, PDF_URL)),
                limpar(resolver(campoParaCabecalho, valorPorCabecalho, LEVEL)),
                limpar(resolver(campoParaCabecalho, valorPorCabecalho, VIDEO_URL)),
                limpar(resolver(campoParaCabecalho, valorPorCabecalho, PARTICIPANT_NAME)),
                limparCpf(resolver(campoParaCabecalho, valorPorCabecalho, PARTICIPANT_CPF)),
                limpar(resolver(campoParaCabecalho, valorPorCabecalho, PARTICIPANT_EMAIL)),
                limpar(resolver(campoParaCabecalho, valorPorCabecalho, INSTITUTION_NAME))
        );
    }

    // Lista os campos do DTO para os quais nenhuma coluna foi encontrada no arquivo
    static List<String> missingFields(Map<String, String> campoParaCabecalho) {
        List<String> faltando = new ArrayList<>();
        campoParaCabecalho.forEach((campo, cabecalho) -> {
            if (cabecalho == null) {
                faltando.add(campo);
            }
        });
        return faltando;
    }

    private static String matchExato(List<String> apelidos, Map<String, String> normalizadoParaOriginal) {
        for (String apelido : apelidos) {
            String original = normalizadoParaOriginal.get(normalize(apelido));
            if (original != null) {
                return original;
            }
        }
        return null;
    }

    private static String matchPorSubstring(List<String> tokens, Map<String, String> normalizadoParaOriginal) {
        if (tokens == null) {
            return null;
        }
        for (String token : tokens) {
            for (Map.Entry<String, String> entry : normalizadoParaOriginal.entrySet()) {
                if (entry.getKey().contains(token)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private static String resolver(Map<String, String> campoParaCabecalho, Function<String, String> valorPorCabecalho, String campo) {
        String cabecalho = campoParaCabecalho.get(campo);
        return cabecalho == null ? null : valorPorCabecalho.apply(cabecalho);
    }

    private static String limpar(String valor) {
        if (valor == null) {
            return null;
        }
        String semEspacos = valor.trim();
        return semEspacos.isEmpty() ? null : semEspacos;
    }

    // Remove tudo que não for dígito (pontos, traços, espaços) de um CPF.
    private static String limparCpf(String valor) {
        String limpo = limpar(valor);
        if (limpo == null) {
            return null;
        }
        String apenasDigitos = limpo.replaceAll("\\D", "");
        return apenasDigitos.isEmpty() ? null : apenasDigitos;
    }
}
