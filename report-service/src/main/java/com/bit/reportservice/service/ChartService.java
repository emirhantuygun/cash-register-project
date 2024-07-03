package com.bit.reportservice.service;

import com.bit.reportservice.exception.ChartGenerationException;
import com.bit.reportservice.exception.InvalidTimeUnitException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
import java.util.*;
import java.util.List;

/**
 * This class is responsible for generating a PDF report based on sales data.
 * It uses iText library for PDF generation and JFreeChart for creating charts.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
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

    @Value("${gemini.active}")
    private String USE_GEMINI;

    private final GeminiService geminiService;

    /**
     * Generates a PDF report based on the given sales data and time unit.
     *
     * @param productQuantityMap Map of product names and their respective sales quantities.
     * @param unit                Time unit for the sales data (e.g., "day", "week", "month", "year").
     * @return Byte array containing the generated PDF report.
     * @throws ChartGenerationException If an error occurs while generating the chart.
     */
    protected byte[] generateChart(Map<String, Integer> productQuantityMap, String unit) {
        log.trace("Entering generateChart method in ChartService");

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
            plot.setMaximumLabelWidth(0.20);  // Limit the maximum label width (20% of the plot area)
            plot.setLabelGap(0.05);
            plot.setSimpleLabels(true);
            plot.setLabelBackgroundPaint(new Color(255, 255, 255));
            plot.setLabelOutlinePaint(Color.black);
            plot.setLabelOutlineStroke(new BasicStroke(1.0f));

            plot.setLabelFont(new Font("Arial", Font.BOLD, 10));  // Using a monospaced font
            plot.setBackgroundPaint(Color.white);
            plot.setOutlineVisible(false);
            plot.setShadowPaint(null);
            plot.setLabelGenerator(new StandardPieSectionLabelGenerator(" {0}:  {2} ", new DecimalFormat("#"), new DecimalFormat("0.00%")));
            plot.setInsets(new RectangleInsets(10, 10, 10, 10));
            chart.getLegend().setItemFont(new Font("Arial", Font.BOLD, 12));
            chart.setBackgroundPaint(Color.white);

            ByteArrayOutputStream chartOut = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(chartOut, chart, 550, 550);

            // Create PDF document
            ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, pdfOut);
            document.open();


            // Add Date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Paragraph date = new Paragraph(dateFormat.format(new Date()), lightSmall);
            date.setAlignment(Paragraph.ALIGN_RIGHT);
            document.add(date);
            document.add(new Paragraph("\n"));


            // Add Title
            Paragraph title = getTitle(unit, boldBig);
            document.add(title);

            // CHART IMAGE
            Image chartImage = Image.getInstance(chartOut.toByteArray());
            chartImage.setAlignment(Image.ALIGN_CENTER);
            document.add(chartImage);

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));


            // STATISTICS TABLE
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

            if (Boolean.parseBoolean(USE_GEMINI)) {
                log.info("Gemini is active");
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

                log.info("Calling getInsight method in GeminiService");
                String saleAnalysis = geminiService.getInsight(saleFigures);

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
            }
            document.close();

            log.trace("Exiting generateChart method in ChartService");
            return pdfOut.toByteArray();

        } catch (DocumentException | IOException e) {
            log.error("Error occurred while generating the the chart", e);
            throw new ChartGenerationException("An error occurred while generating the chart");
        }
    }

    /**
     * This method generates a title for the PDF report based on the given time unit.
     *
     * @param unit The time unit for the sales data (e.g., "day", "week", "month", "year").
     * @param boldBig The font style for the title.
     * @return A Paragraph object containing the generated title.
     * @throws InvalidTimeUnitException If the given time unit is not valid.
     */
    private Paragraph getTitle(String unit, com.itextpdf.text.Font boldBig) {
        log.trace("Entering getTitle method in ChartService");

        String unitIdentifier;
        unitIdentifier = switch (unit.toLowerCase()) {
            case "day" -> "DAILY";
            case "week" -> "WEEKLY";
            case "month" -> "MONTHLY";
            case "year" -> "YEARLY";
            default -> {
                log.error("Invalid time unit parameter: {}", unit);
                throw new InvalidTimeUnitException("Invalid time unit parameter");
            }
        };

        Paragraph title = new Paragraph(unitIdentifier + " SALES REPORT", boldBig);
        title.setAlignment(Paragraph.ALIGN_CENTER);

        log.trace("Exiting getTitle method in ChartService");
        return title;
    }

    /**
     * This method converts the sales data map into a text format for further processing.
     *
     * @param salesData A map containing product names as keys and their respective sales quantities as values.
     * @return A string representation of the sales data in a formatted text.
     */
    private String convertSalesDataToText(Map<String, Integer> salesData) {
        log.trace("Entering convertSalesDataToText method in ChartService");

        StringBuilder salesDataText = new StringBuilder("Sales Data:\n");
        for (Map.Entry<String, Integer> entry : salesData.entrySet()) {
            salesDataText.append("- ").append(entry.getKey()).append(": ")
                    .append(entry.getValue()).append(" units\n");
        }
        log.debug("Sales Data String: {}", salesDataText.toString());

        log.trace("Exiting convertSalesDataToText method in ChartService");
        return salesDataText.toString();
    }
}
