package plugin.DataShells;

import plugin.Abstractions.DataShell;
import plugin.DTO.Metrics;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MarkdownShell implements DataShell {
    @Override
    public InputStream GetCurrentReport(Metrics metrics) {

        StringBuilder md = new StringBuilder();

        md.append("# 📄 Отчёт по результатам теста\n\n");

        md.append("## 📊 Задержки\n");
        md.append(String.format("- **Средняя задержка:** `%.2f ms`\n", metrics.get_httpReqDurationAvg()));
        md.append(String.format("- **P95 задержка:** `%.2f ms`\n", metrics.get_httpReqDurationP95()));
        md.append(String.format("- **Максимальная задержка:** `%.2f ms`\n\n", metrics.get_httpReqDurationMax()));

        md.append("## 🔁 Метрики выполнения\n");
        md.append(String.format("- **Итерации:** `%d`\n", metrics.get_iterationsCount()));
        md.append(String.format("- **HTTP-запросы:** `%d`\n", metrics.get_httpReqsCount()));
        md.append(String.format("- **Проверки (успешные):** `%d`\n", metrics.get_checksPasses()));
        md.append(String.format("- **Ошибки HTTP:** `%.2f`\n\n", metrics.get_httpReqFailedValue()));

        md.append("## 📥 Объём и нагрузка\n");
        md.append(String.format("- **Получено данных:** `%d КБ`\n", metrics.get_dataReceivedCount()));
        md.append(String.format("- **Макс. кол-во пользователей:** `%d`\n", metrics.get_vusMaxValue()));
        md.append(String.format("- **Макс. время ожидания:** `%.2f ms`\n", metrics.get_httpReqWaitingMax()));

        return new ByteArrayInputStream(md.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public InputStream GetAllTimeReport(List<Metrics> metricsList) {
        StringBuilder md = new StringBuilder();

        md.append("# 📈 История всех запусков\n\n");
        md.append("| № | Средняя задержка (ms) | P95 (ms) | Ошибки (%) | Запросов | Пользователей |\n");
        md.append("|---|------------------------|----------|------------|----------|----------------|\n");

        int index = 1;
        for (Metrics m : metricsList) {
            double errorPercent = m.get_httpReqFailedValue() * 100;
            md.append(String.format(
                    "| %d | %.2f | %.2f | %.2f%% | %d | %d |\n",
                    index++,
                    m.get_httpReqDurationAvg(),
                    m.get_httpReqDurationP95(),
                    errorPercent,
                    m.get_httpReqsCount(),
                    m.get_vusMaxValue()
            ));
        }

        return new ByteArrayInputStream(md.toString().getBytes(StandardCharsets.UTF_8));
    }
}
