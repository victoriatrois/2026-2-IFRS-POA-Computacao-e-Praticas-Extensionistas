package ifrs.edu.avaliacao_mnr.project.service;

import ifrs.edu.avaliacao_mnr.project.dto.ProjectImportDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 T: O que esta classe faz?
 Classe equivalente ao CsvParserService, mas para o formato que o sistema de inscrições realmente exporta (planilhas .xlsx). Reaproveita exatamente as mesmas regras de mapeamento de coluna do parser de CSV, via ProjectRowMapper gerando o mesmo resultado, seja ele enviado como .csv ou como .xlsx.
 */

@Service
public class ExcelParserService {

    private static final Logger log = LoggerFactory.getLogger(ExcelParserService.class);

     // Processa um arquivo .xlsx/.xls enviado via upload e converte suas linhas em DTOs
    public List<ProjectImportDTO> parseExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo enviado está vazio.");
        }

        List<ProjectImportDTO> projetos = new ArrayList<>();
        int totalLinhas = 0;
        int ignoradas = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new IllegalArgumentException("A planilha está vazia (sem cabeçalho).");
            }

            Map<String, Integer> cabecalhoParaColuna = new LinkedHashMap<>();
            List<String> cabecalhosOriginais = new ArrayList<>();
            for (Cell cell : headerRow) {
                String nomeCabecalho = formatter.formatCellValue(cell).trim();
                if (!nomeCabecalho.isEmpty()) {
                    cabecalhoParaColuna.putIfAbsent(nomeCabecalho, cell.getColumnIndex());
                    cabecalhosOriginais.add(nomeCabecalho);
                }
            }

            Map<String, String> campoParaCabecalho = ProjectRowMapper.resolveHeaderMap(cabecalhosOriginais);
            avisarColunasNaoEncontradas(campoParaCabecalho);

            for (int numeroLinha = headerRow.getRowNum() + 1; numeroLinha <= sheet.getLastRowNum(); numeroLinha++) {
                Row linhaAtual = sheet.getRow(numeroLinha);
                if (linhaAtual == null) {
                    continue; // linha totalmente em branco
                }
                totalLinhas++;

                ProjectImportDTO dto = ProjectRowMapper.buildDto(
                        campoParaCabecalho,
                        cabecalho -> {
                            Integer indiceColuna = cabecalhoParaColuna.get(cabecalho);
                            if (indiceColuna == null) {
                                return null;
                            }
                            Cell cell = linhaAtual.getCell(indiceColuna);
                            return cell == null ? null : formatter.formatCellValue(cell);
                        }
                );

                if (dto == null) {
                    ignoradas++;
                    log.debug("Linha {} ignorada: sem título de projeto preenchido.", numeroLinha + 1);
                    continue;
                }
                projetos.add(dto);
            }

        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o arquivo Excel: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Falha ao processar o arquivo Excel: " + e.getMessage(), e);
        }

        log.info("Importação Excel concluída: {} linha(s) lida(s), {} projeto(s) importado(s), {} ignorada(s).",
                totalLinhas, projetos.size(), ignoradas);
        return projetos;
    }

    private void avisarColunasNaoEncontradas(Map<String, String> campoParaCabecalho) {
        List<String> faltando = ProjectRowMapper.missingFields(campoParaCabecalho);
        if (!faltando.isEmpty()) {
            log.warn("Não foi possível localizar coluna na planilha para os campos {}. " +
                    "Eles ficarão nulos em todos os projetos importados.", faltando);
        }
    }
}
