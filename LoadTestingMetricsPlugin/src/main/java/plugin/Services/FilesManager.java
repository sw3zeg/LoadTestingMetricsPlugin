package plugin.Services;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.stereotype.Component;
import plugin.DTO.Metrics;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class FilesManager {

    private String dirPath;
    private String summaryFile;

    public FilesManager() {
        Config config = ConfigFactory.load();

        dirPath = config.getString("k6.dirPath");
        summaryFile = config.getString("k6.summaryName");
    }

    public Metrics ReadCurrentSummary() throws InterruptedException {

        Path folder = Paths.get(dirPath);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Metrics> result = new AtomicReference<>();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                Files.list(folder)
                        .filter(Files::isRegularFile)
                        .forEach(path -> {

                            String fileName = path.getFileName().toString();

                            if (fileName.equals(summaryFile)) {
                                try {
                                    result.set(GetMetricsFromFile(path));
                                    latch.countDown();
                                } catch (Exception e) {
                                    throw new RuntimeException("No k6 summary file");
                                }
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException("File dir invalid");
            }
        }, 0, 5, TimeUnit.SECONDS);


        latch.await();
        scheduler.shutdown();
        return result.get();
    }


    public List<Metrics> ReadAllTimeSummary() throws InterruptedException {

        ReadCurrentSummary();

        if (dirPath == null)
            throw new RuntimeException("Dir path is required");

        Path folder = Paths.get(dirPath);


        List<Metrics> result = new ArrayList<>();

        try {
            Files.list(folder)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {

                            String fileName = path.getFileName().toString();
                            if (fileName.startsWith("summary_") && fileName.endsWith(".json")){
                                result.add(GetMetricsFromFile(path));
                            }
                        } catch (IOException e) {
                            System.err.println("Ошибка чтения файла " + path + ": " + e.getMessage());
                            throw new RuntimeException("Invalid file data");
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("File dir invalid");
        }

        return result;
    }


    private Metrics GetMetricsFromFile(Path filePath) throws IOException{

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(filePath.toFile());

        return Metrics.builder()
                ._httpReqDurationAvg(root.path("metrics").path("http_req_duration").path("avg").asDouble())
                ._httpReqDurationP95(root.path("metrics").path("http_req_duration").path("p(95)").asDouble())
                ._httpReqDurationMax(root.path("metrics").path("http_req_duration").path("max").asDouble())
                ._iterationsCount(root.path("metrics").path("iterations").path("count").asInt())
                ._httpReqsCount(root.path("metrics").path("http_reqs").path("count").asInt())
                ._checksPasses(root.path("metrics").path("checks").path("passes").asInt())
                ._httpReqFailedValue(root.path("metrics").path("http_req_failed").path("value").asDouble())
                ._dataReceivedCount(root.path("metrics").path("data_received").path("count").asInt())
                ._vusMaxValue(root.path("metrics").path("vus_max").path("value").asInt())
                ._httpReqWaitingMax(root.path("metrics").path("http_req_waiting").path("max").asDouble())
                .build();
    }
}
