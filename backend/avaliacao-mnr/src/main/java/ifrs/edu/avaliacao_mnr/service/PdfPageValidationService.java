package ifrs.edu.avaliacao_mnr.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

@Service
public class PdfPageValidationService {

    public boolean validatePdfPages(String level, int pages) {

        try {
            String cleanedLevel = level != null ? level.trim().replaceAll("(?i)^[nN]\\s*", "") : "";
            int levelInt = Integer.parseInt(cleanedLevel);

            if (levelInt == 0 || levelInt == 1 || levelInt == 2) {
                return pages >= 1 && pages <= 2;
            } else if (levelInt == 3 || levelInt == 4) {
                return pages >= 3 && pages <= 5;
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid level: " + level, e);
        }
    }

    public int getPdfPages(String pdfPath) {
        try {
            URL url = URI.create(pdfPath).toURL();
            try (InputStream inputStream = url.openStream();
                 PDDocument document = PDDocument.load(inputStream)) {
                return document.getNumberOfPages();
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Error loading PDF file: " + pdfPath, e);
        }
    }
}
