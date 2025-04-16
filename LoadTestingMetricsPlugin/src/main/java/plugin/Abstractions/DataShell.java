package plugin.Abstractions;

import plugin.DTO.Metrics;

import java.io.InputStream;
import java.util.List;

public interface DataShell {

    public InputStream GetCurrentReport(Metrics metrics);
    public InputStream GetAllTimeReport(List<Metrics> metricsList);
}
