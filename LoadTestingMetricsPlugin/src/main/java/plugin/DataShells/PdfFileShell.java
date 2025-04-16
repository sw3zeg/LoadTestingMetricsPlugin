package plugin.DataShells;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import plugin.Abstractions.DataShell;
import plugin.DTO.Metrics;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class PdfFileShell implements DataShell {

    @Override
    public InputStream GetCurrentReport(Metrics metrics) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.setLeading(18f);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 750);

                    writeLine(contentStream, "Current Test Report");
                    writeLine(contentStream, "------------------------");
                    writeLine(contentStream, String.format("Avg Duration: %.2f ms", metrics.get_httpReqDurationAvg()));
                    writeLine(contentStream, String.format("P95 Duration: %.2f ms", metrics.get_httpReqDurationP95()));
                    writeLine(contentStream, String.format("Max Duration: %.2f ms", metrics.get_httpReqDurationMax()));
                    writeLine(contentStream, String.format("Iterations: %d", metrics.get_iterationsCount()));
                    writeLine(contentStream, String.format("HTTP Requests: %d", metrics.get_httpReqsCount()));
                    writeLine(contentStream, String.format("Checks Passed: %d", metrics.get_checksPasses()));
                    writeLine(contentStream, String.format("HTTP Failures: %.2f", metrics.get_httpReqFailedValue()));
                    writeLine(contentStream, String.format("Data Received: %d KB", metrics.get_dataReceivedCount()));
                    writeLine(contentStream, String.format("VUs Max: %d", metrics.get_vusMaxValue()));
                    writeLine(contentStream, String.format("Max Waiting Time: %.2f ms", metrics.get_httpReqWaitingMax()));

                    contentStream.endText();
                }

                document.save(out);
            }

            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void writeLine(PDPageContentStream stream, String text) throws IOException {
        stream.showText(text.replaceAll("[^\\x00-\\x7F]", ""));
        stream.newLine();
    }

    @Override
    public InputStream GetAllTimeReport(List<Metrics> metricsList) {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PDDocument document = new PDDocument();

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);
            PDType1Font font = PDType1Font.HELVETICA;

            content.setFont(font, 12);

            float startX = 50;
            float startY = 750;
            float lineHeight = 18f;
            float labelColWidth = 150f;
            float valueColX = startX + labelColWidth;
            float bottomMargin = 50f;

            int index = 1;

            for (Metrics m : metricsList) {
                // 👉 Проверка: хватит ли места на 11 строк
                if (startY - (11 * lineHeight) < bottomMargin) {
                    content.close();

                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    content.setFont(font, 12);
                    startY = 750;
                }

                content.beginText();
                content.newLineAtOffset(startX, startY);
                content.showText("Run #" + index++);
                content.endText();
                startY -= lineHeight;

                String[][] rows = {
                        {"Avg Duration:",      String.format("%.2f ms", m.get_httpReqDurationAvg())},
                        {"P95 Duration:",      String.format("%.2f ms", m.get_httpReqDurationP95())},
                        {"Max Duration:",      String.format("%.2f ms", m.get_httpReqDurationMax())},
                        {"HTTP Requests:",     String.valueOf(m.get_httpReqsCount())},
                        {"Iterations:",        String.valueOf(m.get_iterationsCount())},
                        {"Checks Passed:",     String.valueOf(m.get_checksPasses())},
                        {"HTTP Failures:",     String.format("%.2f%%", m.get_httpReqFailedValue() * 100)},
                        {"Data Received:",     m.get_dataReceivedCount() + " KB"},
                        {"Max VUs:",           String.valueOf(m.get_vusMaxValue())},
                        {"Max Waiting Time:",  String.format("%.2f ms", m.get_httpReqWaitingMax())}
                };

                for (String[] row : rows) {
                    content.beginText();
                    content.newLineAtOffset(startX, startY);
                    content.showText(row[0]);
                    content.endText();

                    content.beginText();
                    content.newLineAtOffset(valueColX, startY);
                    content.showText(row[1]);
                    content.endText();

                    startY -= lineHeight;
                }

                startY -= lineHeight; // Отступ между блоками
            }

            content.close();
            document.save(out);
            document.close();

            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }
}
