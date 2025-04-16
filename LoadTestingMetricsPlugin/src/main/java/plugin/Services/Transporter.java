package plugin.Services;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import plugin.Abstractions.DataShell;
import plugin.Abstractions.Follower;
import plugin.DTO.Metrics;
import plugin.DataShells.MarkdownShell;
import plugin.DataShells.PdfFileShell;
import plugin.DataShells.TextFileShell;
import plugin.Followers.EmailFollower;
import plugin.Followers.FilesystemFollower;
import plugin.Followers.TelegramFollower;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class Transporter {

    private final String TEXT_FORMAT = "TEXT_FORMAT";
    private final String PDF_FORMAT = "PDF_FORMAT";
    private final String MARKDOWN = "MARKDOWN";
    private final Config _config;
    private final FilesManager _filesManager;
    private final DataShell _dataShell;

    private String _fileName;
    private Follower _folower;

    @Autowired
    public Transporter(FilesManager filesManager) {

        _config = ConfigFactory.load();
        _filesManager = filesManager;

        String sendingFormat = _config.getString("transporter.sendingFormat");

        switch (sendingFormat) {
            case TEXT_FORMAT:
                _dataShell = new TextFileShell();
                _fileName = "Report_" + LocalDateTime.now() + ".txt";
                break;
            case PDF_FORMAT:
                _dataShell = new PdfFileShell();
                _fileName = "Report_" + LocalDateTime.now() + ".pdf";
                break;
            case MARKDOWN:
                _dataShell = new MarkdownShell();
                _fileName = "Report_" + LocalDateTime.now() + ".md";
                break;
            default:
                throw new Error("No such sending format: " + sendingFormat);
        }
    }


    @PostConstruct
    public void Initialize() throws Exception {

        boolean onlyCurrentTest = Boolean.parseBoolean(_config.getString("transporter.onlyCurrentTest"));
        InputStream file;

        if (onlyCurrentTest){
            Metrics metrics = _filesManager.ReadCurrentSummary();
            file = _dataShell.GetCurrentReport(metrics);
        } else {
            List<Metrics> metrics = _filesManager.ReadAllTimeSummary();
            file = _dataShell.GetAllTimeReport(metrics);
        }

        byte[] fileBytes;
        try (var buffer = new java.io.ByteArrayOutputStream()) {
            file.transferTo(buffer);
            fileBytes = buffer.toByteArray();
        }

        if (_config.hasPath("transporter.telegram.subscribeTelegram") &&
                Boolean.parseBoolean(_config.getString("transporter.telegram.subscribeTelegram"))) {
            _folower = new TelegramFollower();
            _folower.SendData(new java.io.ByteArrayInputStream(fileBytes), _fileName);
        }

        if (_config.hasPath("transporter.email.subscribeEmail") &&
                Boolean.parseBoolean(_config.getString("transporter.email.subscribeEmail"))) {
            _folower = new EmailFollower();
            _folower.SendData(new java.io.ByteArrayInputStream(fileBytes), _fileName);
        }

        if (_config.hasPath("transporter.filesystem.subscribeFilesystem") &&
                Boolean.parseBoolean(_config.getString("transporter.filesystem.subscribeFilesystem"))) {
            _folower = new FilesystemFollower();
            _folower.SendData(new java.io.ByteArrayInputStream(fileBytes), _fileName);
        }
    }
}
