package ifrs.edu.avaliacao_mnr.service;

import org.junit.jupiter.api.Test;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class PdfPageValidationServiceTest {

    private final PdfPageValidationService service = new PdfPageValidationService();

    @Test
    void mustAcceptLevel1With2Pages() {

        boolean result = service.validatePdfPages("1", 2);

        assertTrue(result);
    }


    @Test
    void mustRejectLevel1With0Pages() {

        boolean result = service.validatePdfPages("1", 0);

        assertFalse(result);
    }

    @Test
    void mustRejectLevel1With3Pages() {

        boolean result = service.validatePdfPages("1", 3);

        assertFalse(result);
    }


    @Test
    void mustAcceptLevel3With3Pages() {

        boolean result = service.validatePdfPages("3", 3);

        assertTrue(result);
    }

    @Test
    void mustAcceptLevel3With5Pages() {

        boolean result = service.validatePdfPages("3", 5);

        assertTrue(result);
    }


    @Test
    void mustRejectLevel3With6Pages() {

        boolean result = service.validatePdfPages("3", 6);

        assertFalse(result);
    }

    @Test
    void mustRejectInvalidLevel() {
    
        boolean result = service.validatePdfPages("5", 3);
    
        assertFalse(result);
    }

    @Test 
    void mustThrowExceptionForInvalidLevel() {

        assertThrows(
            IllegalArgumentException.class,
            () -> service.validatePdfPages("abc", 2)
        );
    }


    @Test
    void mustReturnCorrectNumberOfPdfPages() throws IOException {

        PDDocument document = new PDDocument();
        document.addPage(new PDPage());
        document.addPage(new PDPage());

        Path pdfPath = Files.createTempFile("test-", ".pdf");
        document.save(pdfPath.toFile());
        document.close();

        int result = service.getPdfPages(pdfPath.toUri().toString());

        assertEquals(2, result);

        Files.deleteIfExists(pdfPath);
    }

    @Test
    void mustReturnOneForSinglePagePdf() throws IOException {

        PDDocument document = new PDDocument();
        document.addPage(new PDPage());

        Path pdfPath = Files.createTempFile("test-", ".pdf");
        document.save(pdfPath.toFile());
        document.close();

        int result = service.getPdfPages(pdfPath.toUri().toString());

        assertEquals(1, result);

        Files.deleteIfExists(pdfPath);
    }

    @Test
    void mustThrowExceptionForInvalidPdf() {

        assertThrows(
            IllegalArgumentException.class,
            () -> service.getPdfPages("invalid-pdf")
        );
    }

    @Test
    void mustThrowExceptionForInaccessiblePdf() {

        assertThrows(
            IllegalArgumentException.class,
            () -> service.getPdfPages("file:///nonexistent/test.pdf")
        );
    }
}