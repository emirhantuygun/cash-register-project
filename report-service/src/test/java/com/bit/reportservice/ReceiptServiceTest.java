package com.bit.reportservice;

import com.bit.reportservice.dto.SaleResponse;
import com.bit.reportservice.exception.ReceiptGenerationException;
import com.bit.reportservice.service.ReceiptService;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReceiptServiceTest {

    @InjectMocks
    private ReceiptService receiptService;

    @Mock
    private PdfWriter pdfWriter;
    @Test
    public void generateReceipt_ShouldThrowReceiptGenerationException_WhenPdfWriterFails() throws Exception {

        try (MockedStatic<PdfWriter> mocked = mockStatic(PdfWriter.class)) {
            // Mocking
            mocked.when(() -> PdfWriter.getInstance(any(Document.class), any(ByteArrayOutputStream.class))).thenThrow(DocumentException.class);
            // Assert
            assertThrows(ReceiptGenerationException.class, () -> receiptService.generateReceipt(new SaleResponse()));
        }
    }
}
