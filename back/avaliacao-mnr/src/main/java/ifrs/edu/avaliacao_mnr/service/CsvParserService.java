package ifrs.edu.avaliacao_mnr.project.service;

import org.springframework.stereotype.Service;
import ifrs.edu.avaliacao_mnr.project.dto.ProjectImportDTO;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CsvParserService {
    /*
    T: O que essa classe faz?
    Processa um arquivo CSV enviado via upload e converte suas linhas em DTOs.
    Detecta automaticamente o delimitador, barra o envio acidental de arquivos binários (como .xlsx) e converte as linhas válidas em objetos ProjectImportDTO.
  */
    private static final Logger log = LoggerFactory.getLogger(CsvParserService.class);

    // T: Assinatura de arquivos ZIP — .xlsx, .docx e .pptx são todos ZIPs por baixo.
    private static final byte[] ZIP_SIGNATURE = {0x50, 0x4B}; // "PK"

    public List<ProjectImportDTO> parseCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo enviado está vazio.");
        }

        String conteudo = lerComoTexto(file);
        char delimitador = detectarDelimitador(conteudo);

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(delimitador)
                .setHeader() // detecta o cabeçalho na 1ª linha
                .setSkipHeaderRecord(true) // não trata o cabeçalho como dado
                .setIgnoreHeaderCase(true) // "Name", "NAME" e "name" são equivalentes
                .setIgnoreEmptyLines(true) // pula linhas totalmente em branco
                .setAllowDuplicateHeaderNames(true) // um cabeçalho repetido não derruba tudo
                .setTrim(true) // remove espaços extras no início/fim de cada valor
                .build();

        List<ProjectImportDTO> projetos = new ArrayList<>();
        int totalLinhas = 0;
        int ignoradas = 0;

        try (CSVParser csvParser = new CSVParser(new StringReader(conteudo), format)) {

            Map<String, String> campoParaCabecalho = ProjectRowMapper.resolveHeaderMap(csvParser.getHeaderNames());
            avisarColunasNaoEncontradas(campoParaCabecalho);

            for (CSVRecord record : csvParser) {
                totalLinhas++;

                ProjectImportDTO dto = ProjectRowMapper.buildDto(
                        campoParaCabecalho,
                        cabecalho -> record.isMapped(cabecalho) ? record.get(cabecalho) : null
                );

                if (dto == null) {
                    ignoradas++;
                    log.debug("Linha {} ignorada: sem título de projeto preenchido.", record.getRecordNumber());
                    continue;
                }
                projetos.add(dto);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o arquivo CSV: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Falha ao processar o arquivo CSV: " + e.getMessage(), e);
        }

        log.info("Importação CSV concluída: {} linha(s) lida(s), {} projeto(s) importado(s), {} ignorada(s).", totalLinhas, projetos.size(), ignoradas);

        return projetos;
    }

    /* T:
    - Lê o arquivo inteiro como texto UTF-8, rejeita arquivos binários -> rejeitarSeForBinario(bytes)
    - remove o BOM (byte order mark): exportações feitas pelo Excel no Windows costumam inserir no início do arquivo, se não for removido, corrompe o nome da primeira coluna do cabeçalho.
     */
    private String lerComoTexto(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            rejeitarSeForBinario(bytes);

            String texto = new String(bytes, StandardCharsets.UTF_8);
            if (!texto.isEmpty() && texto.charAt(0) == '\uFEFF') {
                texto = texto.substring(1);
            }
            return texto;
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler o arquivo enviado.", e);
        }
    }

    /*
    # T: Barreira defensiva
    - Verifica a assinatura ZIP de todo arquivo .xlsx/.docx/.pptx (2 primeiors bytes), em vez de confiar só na extensão do arquivo.
     */
    private void rejeitarSeForBinario(byte[] bytes) {
        if (bytes.length >= ZIP_SIGNATURE.length
                && bytes[0] == ZIP_SIGNATURE[0]
                && bytes[1] == ZIP_SIGNATURE[1]) {
            throw new IllegalArgumentException("" +
                    "O arquivo enviado parece ser um .xlsx, não um CSV de texto. " +
                    "Use o ExcelParserService para arquivos .xlsx.");
        }
    }

    /*
    # T: Escolha entre VIRGULA ou PONTO E VIRGULA
    - Planilhas exportadas com o Excel configurado em pt-BR costumam salvar CSV separado por ";"
    ("," é o separador decimal). Aqui analisa só a primeira linha (cabeçalho) e escolhe o separador mais frequente entre "," e ";".
     */
    private char detectarDelimitador(String conteudo) {
        int fimPrimeiraLinha = conteudo.indexOf('\n');
        String primeiraLinha = fimPrimeiraLinha >= 0 ? conteudo.substring(0, fimPrimeiraLinha) : conteudo;

        long virgulas = primeiraLinha.chars().filter(c -> c == ',').count();
        long pontoEVirgulas = primeiraLinha.chars().filter(c -> c == ';').count();

        return pontoEVirgulas > virgulas ? ';' : ',';
    }

    /*
    # T: Alerta configurado
    - Avisa caso a planinha esteja incompleta.
    - Ele usa o ProjectRowMapper para verificar quais campos não consegiui achar e guarda na lista "faltando", se a lista não esiver vazia é enviado o aviso.
    */
    private void avisarColunasNaoEncontradas(Map<String, String> campoParaCabecalho) {
        List<String> faltando = ProjectRowMapper.missingFields(campoParaCabecalho);
        if (!faltando.isEmpty()) {
            log.warn("Não foi possível localizar coluna no CSV para os campos {}. " +
                    "Eles ficarão nulos em todos os projetos importados.", faltando);
        }
    }
}