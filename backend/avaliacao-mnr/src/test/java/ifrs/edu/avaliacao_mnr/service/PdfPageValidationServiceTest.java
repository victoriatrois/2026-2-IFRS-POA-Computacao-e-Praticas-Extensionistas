package ifrs.edu.avaliacao_mnr.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class PdfPageValidationServiceTest {

    private final PdfPageValidationService service = new PdfPageValidationService();

    @Test
    void mustAcceptLevel1With2Pages() {
        assertTrue(service.validatePdfPages("1", 2));
    }

    @Test
    void mustAcceptPrefixLevelWithValidPages() {
        assertTrue(service.validatePdfPages("n1", 2));
        assertTrue(service.validatePdfPages("n2", 1));
        assertTrue(service.validatePdfPages("N3", 4));
        assertTrue(service.validatePdfPages("n4", 5));
    }

    @Test
    void mustRejectLevel1With0Pages() {
        assertFalse(service.validatePdfPages("1", 0));
    }

    @Test
    void mustRejectLevel1With3Pages() {
        assertFalse(service.validatePdfPages("1", 3));
    }

    @Test
    void mustAcceptLevel3With3Pages() {
        assertTrue(service.validatePdfPages("3", 3));
    }

    @Test
    void mustAcceptLevel3With5Pages() {
        assertTrue(service.validatePdfPages("3", 5));
    }

    @Test
    void mustRejectLevel3With6Pages() {
        assertFalse(service.validatePdfPages("3", 6));
    }

    @Test
    void mustRejectInvalidLevel() {
        assertFalse(service.validatePdfPages("5", 3));
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
    void mustThrowExceptionForInvalidLevel() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.validatePdfPages("abc", 2)
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
