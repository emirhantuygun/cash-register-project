package com.bit.reportservice.service;

import com.bit.reportservice.exception.ChartGenerationException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Font;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChartService {

    @Value("${chart.font-path.light}")
    private String LIGHT_FONT_PATH;

    @Value("${chart.font-path.regular}")
    private String REGULAR_FONT_PATH;

    @Value("${chart.image-path.gemini}")
    private String GEMINI_IMAGE_PATH;

    @Value("${chart.image-path.symbol}")
    private String SYMBOL_IMAGE_PATH;

    private final GeminiService geminiService;

    protected byte[] generateChart(Map<String, Integer> productQuantityMap) {

        try {
            BaseFont baseFontLight = BaseFont.createFont(LIGHT_FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            BaseFont baseFontRegular = BaseFont.createFont(REGULAR_FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            com.itextpdf.text.Font bold = new com.itextpdf.text.Font(baseFontRegular, 20, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font boldBig = new com.itextpdf.text.Font(baseFontRegular, 30, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font boldSmall = new com.itextpdf.text.Font(baseFontRegular, 14, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font lightSmall = new com.itextpdf.text.Font(baseFontLight, 12);
            com.itextpdf.text.Font space = new com.itextpdf.text.Font(baseFontLight, 20, com.itextpdf.text.Font.BOLD);

            // Create a pie chart with percentages
            @SuppressWarnings("rawtypes") DefaultPieDataset dataset = new DefaultPieDataset<>();
            productQuantityMap.forEach(dataset::setValue);

            JFreeChart chart = ChartFactory.createPieChart("", dataset, true, false, false);
            @SuppressWarnings("rawtypes") PiePlot plot = (PiePlot) chart.getPlot();
            plot.setLabelFont(new Font("Arial", Font.BOLD, 12));  // Using a monospaced font
            plot.setLabelGap(0.02);
            plot.setBackgroundPaint(Color.white);
            plot.setOutlineVisible(false);
            plot.setShadowPaint(null);
            plot.setLabelGenerator(new StandardPieSectionLabelGenerator(" {0}:  {2} ", new DecimalFormat("#"), new DecimalFormat("0.00%")));
            plot.setSimpleLabels(true);
            plot.setInsets(new RectangleInsets(10, 10, 10, 10));
            chart.getLegend().setItemFont(new Font("Arial", Font.BOLD, 12));
            chart.setBackgroundPaint(Color.white);

            ByteArrayOutputStream chartOut = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(chartOut, chart, 600, 600);

            // Create PDF document
            ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, pdfOut);
            document.open();

            // Add title
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Paragraph date = new Paragraph(dateFormat.format(new Date()), lightSmall);
            date.setAlignment(Paragraph.ALIGN_RIGHT);
            document.add(date);
            document.add(new Paragraph("\n"));
            Paragraph title = new Paragraph("SALES REPORT", boldBig);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);

            // Add chart image to PDF
            Image chartImage = Image.getInstance(chartOut.toByteArray());
            chartImage.setAlignment(Image.ALIGN_CENTER);
            document.add(chartImage);

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));


            // Statistics table
            Paragraph statisticsTitle = new Paragraph("Sales Statistics", bold);
            statisticsTitle.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(statisticsTitle);
            document.add(new Paragraph("\n", space));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(90);
            table.setSpacingBefore(0f);
            table.setSpacingAfter(10f);

            // Add table headers
            PdfPCell cell1 = new PdfPCell(new Paragraph("Product", boldSmall));
            PdfPCell cell2 = new PdfPCell(new Paragraph("Sales Count", boldSmall));
            PdfPCell cell3 = new PdfPCell(new Paragraph("Percentage", boldSmall));
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);

            // Add sales data to table
            int totalSales = productQuantityMap.values().stream().mapToInt(Integer::intValue).sum();
            for (Map.Entry<String, Integer> entry : productQuantityMap.entrySet()) {
                PdfPCell nameCell = new PdfPCell(new Paragraph(entry.getKey(), lightSmall));
                nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                nameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(nameCell);

                PdfPCell unitCell = new PdfPCell(new Paragraph(String.valueOf(entry.getValue()), lightSmall));
                unitCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                unitCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(unitCell);

                PdfPCell percentageCell = new PdfPCell(new Paragraph(String.format("%.2f%%", (entry.getValue() / (double) totalSales) * 100), lightSmall));
                percentageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                percentageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(percentageCell);
            }

            document.add(table);

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));


            // GEMINI

            try (InputStream inputStream = getClass().getResourceAsStream(GEMINI_IMAGE_PATH)) {
                if (inputStream != null) {
                    Image image = Image.getInstance(IOUtils.toByteArray(inputStream));

                    image.scaleToFit(75, 75); // Resize the image if necessary
                    image.setAlignment(Image.ALIGN_CENTER);
                    document.add(image);
                }
            }

            document.add(new Paragraph("\n"));
            String saleFigures = convertSalesDataToText(productQuantityMap);
            String saleAnalysis = geminiService.getInsight(saleFigures);
            System.out.println(saleAnalysis);
            if (saleAnalysis != null) {

                saleAnalysis = saleAnalysis.replaceAll("\\*\\*", "");
                saleAnalysis = saleAnalysis.replaceAll("##", "");

                String[] sentencesArray = saleAnalysis.split("(?<=[.!?])\\s*");

                // Convert the array to a list for easier processing
                List<String> sentences = new ArrayList<>();
                for (String sentence : sentencesArray) {
                    sentences.add(sentence.trim());
                }
                PdfPTable aiTable = new PdfPTable(2);
                aiTable.setWidthPercentage(90);
                aiTable.setWidths(new float[]{0.05f, 0.95f});

                for (String sentence : sentences) {
                    System.out.println(sentence);
                    InputStream inputStream = getClass().getResourceAsStream(SYMBOL_IMAGE_PATH);
                    if (inputStream != null) {
                        Image image = Image.getInstance(IOUtils.toByteArray(inputStream));

                        image.scaleToFit(10, 10); // Resize the image if necessary
                        image.setSpacingBefore(10f);

                        PdfPCell leftCell1 = new PdfPCell(image);

                        leftCell1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        leftCell1.setVerticalAlignment(Element.ALIGN_MIDDLE);

                        leftCell1.setBorder(Rectangle.NO_BORDER);

                        aiTable.addCell(leftCell1);

                    }

                    PdfPCell rightCell1 = new PdfPCell();
                    Phrase phrase = new Phrase(sentence);
                    rightCell1.addElement(phrase);
                    rightCell1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    rightCell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    rightCell1.setBorder(Rectangle.NO_BORDER);
                    aiTable.addCell(rightCell1);
                }
                document.add(aiTable);
            }

            document.close();

            return pdfOut.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new ChartGenerationException("An error occurred while generating the chart");
        }
    }

    private String convertSalesDataToText(Map<String, Integer> salesData) {
        StringBuilder salesDataText = new StringBuilder("Sales Data:\n");
        for (Map.Entry<String, Integer> entry : salesData.entrySet()) {
            salesDataText.append("- ").append(entry.getKey()).append(": ")
                    .append(entry.getValue()).append(" units\n");
        }
        return salesDataText.toString();
    }
}
