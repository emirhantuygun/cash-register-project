package com.bit.reportservice.service;

import com.bit.reportservice.dto.SaleResponse;
import com.bit.reportservice.exception.ReceiptGenerationException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class ReceiptServiceTest {

    @InjectMocks
    private ReceiptService receiptService;

    @Test
    public void generateReceipt_ShouldThrowReceiptGenerationException_WhenPdfWriterFails() {

        try (MockedStatic<PdfWriter> mocked = mockStatic(PdfWriter.class)) {
            // Arrange
            mocked.when(() -> PdfWriter.getInstance(any(Document.class), any(ByteArrayOutputStream.class))).thenThrow(DocumentException.class);

            // Act & Assert
            assertThrows(ReceiptGenerationException.class, () -> receiptService.generateReceipt(new SaleResponse()));
        }
    }
}
