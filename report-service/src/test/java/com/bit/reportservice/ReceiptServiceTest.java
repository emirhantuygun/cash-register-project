package com.bit.reportservice;

import com.bit.reportservice.dto.ProductResponse;
import com.bit.reportservice.dto.SaleResponse;
import com.bit.reportservice.exception.ReceiptGenerationException;
import com.bit.reportservice.service.ReceiptService;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ReceiptServiceTest {

    @InjectMocks
    private ReceiptService receiptService;

//    @Test
//    public void generateReceipt_ShouldReturnByteArray() throws ReceiptGenerationException {
//        // Arrange
//        ReflectionTestUtils.setField(receiptService, "FONT_PATH", "fonts/scoreboard.ttf");
//        ReflectionTestUtils.setField(receiptService, "IMAGE_PATH", "/static/images/32bit.png");
//        ReflectionTestUtils.setField(receiptService, "LOCATION", "some_location");
//        ReflectionTestUtils.setField(receiptService, "CITY", "some_city");
//        ReflectionTestUtils.setField(receiptService, "PHONE_NUMBER", "some_phone_number");
//
//        byte[] bytes = new byte[0];
//
//        ByteArrayOutputStream baos = mock();
//        when(baos.toByteArray()).thenReturn(bytes);
//
//        List<ProductResponse> productResponses = List.of(new ProductResponse(1L, "Product", "Barcode", 1, BigDecimal.valueOf(1), BigDecimal.valueOf(1), 1L));
//        assertTrue(bytes.getClass() == receiptService.generateReceipt(new SaleResponse(1L, "string", new Date(), "string", List.of("string"), productResponses, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, null)).getClass());
//    }

    @Test
    public void generateReceipt_ShouldThrowReceiptGenerationException_WhenPdfWriterFails() {

        try (MockedStatic<PdfWriter> mocked = mockStatic(PdfWriter.class)) {
            // Mocking
            mocked.when(() -> PdfWriter.getInstance(any(Document.class), any(ByteArrayOutputStream.class))).thenThrow(DocumentException.class);
            // Assert
            assertThrows(ReceiptGenerationException.class, () -> receiptService.generateReceipt(new SaleResponse()));
        }
    }
}
