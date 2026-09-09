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
            int levelInt = Integer.parseInt(level);
            
            if (levelInt == 0 || levelInt == 1 || levelInt == 2) {
                return pages >= 1 && pages <= 2;
            } else if (levelInt == 3 || levelInt == 4) {
                return pages >= 3 && pages <= 5;
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid level: " + level);
        }
    }

    /* JT:
    To work with PDFs in Java, the tool we will use is the Apache PDFBox
    library. The core of this library is a class called PDDocument,
    which is used to load and represent the PDF file in our system's memory.
    
    SpringBoot will need internet access to download the PDF from the link that came from the CSV.
    Once it is in Java's memory, it performs the count and sends it to the validation service.
    */

    public int getPdfPages(String pdfPath) {
        try {
            // JT: Java 25: URI.create(...).toURL() instead of new URL(pdfPath) (deprecated).
            // JT: openStream() opens the HTTP connection and returns the PDF's InputStream.
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