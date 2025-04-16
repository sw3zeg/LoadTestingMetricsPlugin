package plugin.DataShells;

import org.springframework.stereotype.Component;
import plugin.Abstractions.DataShell;
import plugin.DTO.Metrics;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class TextFileShell implements DataShell {


    @Override
    public InputStream GetCurrentReport(Metrics metrics) {

        StringBuilder report = new StringBuilder();

        report.append("📊 Отчёт по результатам тестирования\n");
        report.append("------------------------------------\n");
        report.append(String.format("⏱ Средняя задержка: %.2f ms\n", metrics.get_httpReqDurationAvg()));
        report.append(String.format("⏱ P95 задержка:     %.2f ms\n", metrics.get_httpReqDurationP95()));
        report.append(String.format("⏱ Максимальная:     %.2f ms\n", metrics.get_httpReqDurationMax()));
        report.append("\n");

        report.append(String.format("🔁 Кол-во итераций:        %d\n", metrics.get_iterationsCount()));
        report.append(String.format("🌐 Кол-во HTTP-запросов:   %d\n", metrics.get_httpReqsCount()));
        report.append(String.format("✅ Успешные проверки:       %d\n", metrics.get_checksPasses()));
        report.append(String.format("❌ Ошибки HTTP:             %.2f\n", metrics.get_httpReqFailedValue()));
        report.append("\n");

        report.append(String.format("📥 Получено данных:         %d КБ\n", metrics.get_dataReceivedCount()));
        report.append(String.format("👥 Максимум VUs:            %d\n", metrics.get_vusMaxValue()));
        report.append(String.format("⌛ Максимальное ожидание:   %.2f ms\n", metrics.get_httpReqWaitingMax()));

        return new ByteArrayInputStream(report.toString().getBytes(StandardCharsets.UTF_8));
    }


    @Override
    public InputStream GetAllTimeReport(List<Metrics> metricsList) {

        StringBuilder txt = new StringBuilder();

        txt.append("История всех запусков\n");
        txt.append("=====================\n\n");

        int index = 1;
        for (Metrics m : metricsList) {
            double errorPercent = m.get_httpReqFailedValue() * 100;

            txt.append(String.format("Запуск #%d\n", index++));
            txt.append(String.format("Средняя задержка:      %.2f ms\n", m.get_httpReqDurationAvg()));
            txt.append(String.format("P95 задержка:          %.2f ms\n", m.get_httpReqDurationP95()));
            txt.append(String.format("Макс. задержка:        %.2f ms\n", m.get_httpReqDurationMax()));
            txt.append(String.format("HTTP-запросов:         %d\n", m.get_httpReqsCount()));
            txt.append(String.format("Итераций:              %d\n", m.get_iterationsCount()));
            txt.append(String.format("Проверки (успешные):   %d\n", m.get_checksPasses()));
            txt.append(String.format("Ошибки HTTP:           %.2f%%\n", errorPercent));
            txt.append(String.format("Получено данных:       %d КБ\n", m.get_dataReceivedCount()));
            txt.append(String.format("Макс. VUs:             %d\n", m.get_vusMaxValue()));
            txt.append(String.format("Макс. ожидание:        %.2f ms\n", m.get_httpReqWaitingMax()));
            txt.append("\n----------------------------------------\n\n");
        }

        return new ByteArrayInputStream(txt.toString().getBytes(StandardCharsets.UTF_8));
    }
}
