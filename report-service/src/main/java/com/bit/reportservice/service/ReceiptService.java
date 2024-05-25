package com.bit.reportservice.service;

import com.bit.reportservice.dto.ProductResponse;
import com.bit.reportservice.dto.SaleResponse;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class ReceiptService {
    private final String FONT_PATH = "fonts/scoreboard.ttf";
    private final String IMAGE_PATH = "/static/images/32bit.png";
    private final String LOCATION = "KEMALPASA, ESENTEPE CAMPUS, 54050";
    private final String PHONE_NUMBER = "0264 295 54 54";
    private final String CITY = "SERDIVAN/SAKARYA";


    public byte[] generateReceipt(SaleResponse sale) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);

            document.open();

            BaseFont baseFont = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font bold = new Font(baseFont, 20, Font.BOLD);
            Font boldBig = new Font(baseFont, 30, Font.BOLD);
            Font boldSmall = new Font(baseFont, 16, Font.BOLD);
            Font light = new Font(baseFont, 20);
            Font lightBig = new Font(baseFont, 30);
            Font lightSmall = new Font(baseFont, 16);
            Font lightExtraSmall = new Font(baseFont, 14);
            Font hyphen = new Font(baseFont, 15, Font.BOLD);
            Font space = new Font(baseFont, 15, Font.BOLD);

            Paragraph hyphens = new Paragraph("------------------------------------------------------------------", hyphen);
            hyphens.setAlignment(Paragraph.ALIGN_CENTER);

            document.add(new Paragraph("\n", space));

            // IMAGE

            try (InputStream inputStream = getClass().getResourceAsStream(IMAGE_PATH)) {
                if (inputStream != null) {
                    Image image = Image.getInstance(IOUtils.toByteArray(inputStream));

                    image.scaleToFit(150, 150); // Resize the image if necessary
                    image.setAlignment(Image.ALIGN_CENTER);
                    document.add(image);

                    document.add(new Paragraph("\n", space));
                }
            }


            // LOCATION, PHONE NUMBER, CITY

            Paragraph location = new Paragraph(LOCATION, bold);
            location.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(location);

            Paragraph phoneNumber = new Paragraph(PHONE_NUMBER, bold);
            phoneNumber.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(phoneNumber);

            Paragraph city = new Paragraph(CITY, bold);
            city.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(city);

            document.add(new Paragraph("\n", space));
            document.add(new Paragraph("\n", space));

            // SALE NO. CASHIER, PAYMENT. DATE, TIME

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.9f, 1});

            PdfPCell leftCell1 = new PdfPCell();
            Phrase phraseLeft1 = new Phrase();
            phraseLeft1.add(new Chunk("SALE NO: ", bold));
            phraseLeft1.add(new Chunk(String.valueOf(sale.getId()), light));
            leftCell1.addElement(phraseLeft1);
            leftCell1.setHorizontalAlignment(Element.ALIGN_LEFT);
            leftCell1.setBorder(Rectangle.NO_BORDER);
            table.addCell(leftCell1);

            PdfPCell rightCell1 = new PdfPCell();
            rightCell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            rightCell1.setBorder(Rectangle.NO_BORDER);
            table.addCell(rightCell1);

            PdfPCell leftCell2 = new PdfPCell();
            Phrase phraseLeft2 = new Phrase();
            phraseLeft2.add(new Chunk("CASHIER: ", bold));
            phraseLeft2.add(new Chunk(sale.getCashier(), light));
            leftCell2.addElement(phraseLeft2);
            leftCell2.setHorizontalAlignment(Element.ALIGN_LEFT);
            leftCell2.setBorder(Rectangle.NO_BORDER);
            table.addCell(leftCell2);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            PdfPCell rightCell2 = new PdfPCell();
            Phrase phraseRight2 = new Phrase();
            phraseRight2.add(new Chunk("DATE: ", bold));
            phraseRight2.add(new Chunk(dateFormat.format(sale.getDate()), light));
            rightCell2.addElement(phraseRight2);
            rightCell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            rightCell2.setBorder(Rectangle.NO_BORDER);
            table.addCell(rightCell2);

            PdfPCell leftCell3 = new PdfPCell();
            Phrase phraseLeft3 = new Phrase();
            phraseLeft3.add(new Chunk("PAYMENT: ", bold));
            phraseLeft3.add(new Chunk(sale.getPaymentMethod().replace("_", " "), light));
            leftCell3.addElement(phraseLeft3);
            leftCell3.setHorizontalAlignment(Element.ALIGN_LEFT);
            leftCell3.setBorder(Rectangle.NO_BORDER);
            table.addCell(leftCell3);

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            PdfPCell rightCell3 = new PdfPCell();
            Phrase phraseRight3 = new Phrase();
            phraseRight3.add(new Chunk("TIME: ", bold));
            phraseRight3.add(new Chunk(timeFormat.format(sale.getDate()), light));
            rightCell3.addElement(phraseRight3);
            rightCell3.setHorizontalAlignment(Element.ALIGN_RIGHT);
            rightCell3.setBorder(Rectangle.NO_BORDER);
            table.addCell(rightCell3);

            document.add(table);

            document.add(new Paragraph("\n", space));
            document.add(hyphens);

            // PRODUCTS

            PdfPTable headerTable = new PdfPTable(5);
            headerTable.setWidthPercentage(100);
            float[] columnWidths = {3f, 3f, 1f, 2f, 2f};
            headerTable.setWidths(columnWidths);

            PdfPCell cell1 = new PdfPCell(new Phrase("BARCODE NO", boldSmall));
            cell1.setBorderWidth(0);
            cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerTable.addCell(cell1);

            PdfPCell cell2 = new PdfPCell(new Phrase("DESCRIPTION", boldSmall));
            cell2.setBorderWidth(0);
            cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerTable.addCell(cell2);

            PdfPCell cell3 = new PdfPCell(new Phrase("QTY", boldSmall));
            cell3.setBorderWidth(0);
            cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerTable.addCell(cell3);

            PdfPCell cell4 = new PdfPCell(new Phrase("PRICE", boldSmall));
            cell4.setBorderWidth(0);
            cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerTable.addCell(cell4);

            PdfPCell cell5 = new PdfPCell(new Phrase("AMOUNT", boldSmall));
            cell5.setBorderWidth(0);
            cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerTable.addCell(cell5);

            document.add(headerTable);

            document.add(hyphens);

            PdfPTable productTable = new PdfPTable(5);
            productTable.setWidthPercentage(100);
            productTable.setWidths(columnWidths);

            for (ProductResponse product : sale.getProducts()) {
                PdfPCell cell6 = new PdfPCell(new Phrase(product.getBarcodeNumber(), lightSmall));
                cell6.setBorderWidth(0);
                cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                productTable.addCell(cell6);

                PdfPCell cell7 = new PdfPCell(new Phrase(product.getName(), lightSmall));
                cell7.setBorderWidth(0);
                cell7.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell7.setHorizontalAlignment(Element.ALIGN_CENTER);
                productTable.addCell(cell7);

                PdfPCell cell8 = new PdfPCell(new Phrase(String.valueOf(product.getQuantity()), lightSmall));
                cell8.setBorderWidth(0);
                cell8.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell8.setHorizontalAlignment(Element.ALIGN_CENTER);
                productTable.addCell(cell8);

                PdfPCell cell9 = new PdfPCell(new Phrase(String.valueOf(product.getPrice()), lightSmall));
                cell9.setBorderWidth(0);
                cell9.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell9.setHorizontalAlignment(Element.ALIGN_CENTER);
                productTable.addCell(cell9);

                PdfPCell cell10 = new PdfPCell(new Phrase(String.valueOf(product.getTotalPrice()), lightSmall));
                cell10.setBorderWidth(0);
                cell10.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                productTable.addCell(cell10);
            }

            document.add(productTable);

            document.add(hyphens);

            // CAMPAIGNS

            if (sale.getCampaignNames() != null && !sale.getCampaignNames().isEmpty()) {
                Paragraph campaigns = new Paragraph("CAMPAIGNS", bold);
                campaigns.setAlignment(Paragraph.ALIGN_CENTER);
                document.add(campaigns);

                for (String campaignName : sale.getCampaignNames()) {
                    Paragraph campaign = new Paragraph(campaignName, lightExtraSmall);
                    campaign.setAlignment(Paragraph.ALIGN_CENTER);
                    document.add(campaign);
                }
                document.add(hyphens);
            }

            // TOTAL

            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(100);
            totalTable.setWidths(new int[]{1, 1});

            PdfPCell subtotalCellLeft = new PdfPCell(new Phrase("SUBTOTAL: ", bold));
            subtotalCellLeft.setHorizontalAlignment(Element.ALIGN_LEFT);
            subtotalCellLeft.setBorder(Rectangle.NO_BORDER);
            totalTable.addCell(subtotalCellLeft);

            PdfPCell subtotalCellRight = new PdfPCell(new Phrase(String.valueOf(sale.getTotal()), light));
            subtotalCellRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
            subtotalCellRight.setBorder(Rectangle.NO_BORDER);
            totalTable.addCell(subtotalCellRight);

            PdfPCell discountCellLeft = new PdfPCell(new Phrase("DISCOUNT: ", bold));
            discountCellLeft.setHorizontalAlignment(Element.ALIGN_LEFT);
            discountCellLeft.setBorder(Rectangle.NO_BORDER);
            totalTable.addCell(discountCellLeft);

            PdfPCell discountCellRight = new PdfPCell(new Phrase("-" + sale.getTotal().subtract(sale.getTotalWithCampaign()), light));
            discountCellRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
            discountCellRight.setBorder(Rectangle.NO_BORDER);
            totalTable.addCell(discountCellRight);

            PdfPCell totalCellLeft = new PdfPCell(new Phrase("TOTAL: ", boldBig));
            totalCellLeft.setHorizontalAlignment(Element.ALIGN_LEFT);
            totalCellLeft.setBorder(Rectangle.NO_BORDER);
            totalTable.addCell(totalCellLeft);

            PdfPCell totalCellRight = new PdfPCell(new Phrase(String.valueOf(sale.getTotalWithCampaign()), lightBig));
            totalCellRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalCellRight.setBorder(Rectangle.NO_BORDER);
            totalTable.addCell(totalCellRight);

            if (sale.getPaymentMethod().equals("CASH")) {
                PdfPCell cashCellLeft = new PdfPCell(new Phrase("CASH: ", bold));
                cashCellLeft.setHorizontalAlignment(Element.ALIGN_LEFT);
                cashCellLeft.setBorder(Rectangle.NO_BORDER);
                totalTable.addCell(cashCellLeft);

                PdfPCell cashCellRight = new PdfPCell(new Phrase(String.valueOf(sale.getCash()), light));
                cashCellRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cashCellRight.setBorder(Rectangle.NO_BORDER);
                totalTable.addCell(cashCellRight);

                PdfPCell changeCellLeft = new PdfPCell(new Phrase("CHANGE: ", bold));
                changeCellLeft.setHorizontalAlignment(Element.ALIGN_LEFT);
                changeCellLeft.setBorder(Rectangle.NO_BORDER);
                totalTable.addCell(changeCellLeft);

                PdfPCell changeCellRight = new PdfPCell(new Phrase(String.valueOf(sale.getChange()), light));
                changeCellRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
                changeCellRight.setBorder(Rectangle.NO_BORDER);
                totalTable.addCell(changeCellRight);
            }

            if (sale.getPaymentMethod().equals("MIXED")) {
                PdfPCell creditCardCellLeft = new PdfPCell(new Phrase("CREDIT CARD: ", bold));
                creditCardCellLeft.setHorizontalAlignment(Element.ALIGN_LEFT);
                creditCardCellLeft.setBorder(Rectangle.NO_BORDER);
                totalTable.addCell(creditCardCellLeft);

                PdfPCell creditCardCellRight = new PdfPCell(new Phrase(String.valueOf(sale.getMixedPayment().getCreditCardAmount()), light));
                creditCardCellRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
                creditCardCellRight.setBorder(Rectangle.NO_BORDER);
                totalTable.addCell(creditCardCellRight);

                PdfPCell cashCellLeft = new PdfPCell(new Phrase("CASH: ", bold));
                cashCellLeft.setHorizontalAlignment(Element.ALIGN_LEFT);
                cashCellLeft.setBorder(Rectangle.NO_BORDER);
                totalTable.addCell(cashCellLeft);

                PdfPCell cashCellRight = new PdfPCell(new Phrase(String.valueOf(sale.getMixedPayment().getCashAmount()), light));
                cashCellRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cashCellRight.setBorder(Rectangle.NO_BORDER);
                totalTable.addCell(cashCellRight);

                PdfPCell changeCellLeft = new PdfPCell(new Phrase("CHANGE: ", bold));
                changeCellLeft.setHorizontalAlignment(Element.ALIGN_LEFT);
                changeCellLeft.setBorder(Rectangle.NO_BORDER);
                totalTable.addCell(changeCellLeft);

                PdfPCell changeCellRight = new PdfPCell(new Phrase(String.valueOf(sale.getChange()), light));
                changeCellRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
                changeCellRight.setBorder(Rectangle.NO_BORDER);
                totalTable.addCell(changeCellRight);
            }


            document.add(totalTable);

            document.add(hyphens);

            Paragraph subtext = new Paragraph("HAVE A NICE DAY", boldBig);
            subtext.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(subtext);
            document.close();

            return baos.toByteArray();

        } catch (DocumentException | IOException e) {
            System.err.println("Error generating receipt: " + e.getMessage());
            return null;
        }
    }
}
