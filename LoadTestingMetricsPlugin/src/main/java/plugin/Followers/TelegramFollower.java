package plugin.Followers;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import plugin.Abstractions.Follower;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TelegramFollower implements Follower {

    private final String botToken;
    private final String chatId;

    public TelegramFollower() {

        Config _config = ConfigFactory.load();
        botToken = _config.getString("transporter.telegram.botToken");
        chatId = _config.getString("transporter.telegram.chatId");
    }


    @Override
    public void SendData(InputStream file, String fileName) throws Exception {
        String boundary = generateBoundary();
        HttpURLConnection conn = openTelegramConnection(boundary);

        try (DataOutputStream output = new DataOutputStream(conn.getOutputStream())) {
            writeChatIdPart(output, boundary);
            writeFilePart(output, boundary, fileName, file);
            writeClosingBoundary(output, boundary);
        }

        int responseCode = conn.getResponseCode();
        System.out.println("Telegram response: " + responseCode);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines().forEach(System.out::println);
        }

        conn.disconnect();
    }

    private String generateBoundary() {
        return "----WebKitFormBoundary" + System.currentTimeMillis();
    }

    private HttpURLConnection openTelegramConnection(String boundary) throws IOException {
        URL url = new URL("https://api.telegram.org/bot" + botToken + "/sendDocument");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        return conn;
    }

    private void writeChatIdPart(DataOutputStream output, String boundary) throws IOException {
        String lineEnd = "\r\n";
        output.writeBytes("--" + boundary + lineEnd);
        output.writeBytes("Content-Disposition: form-data; name=\"chat_id\"" + lineEnd + lineEnd);
        output.writeBytes(chatId + lineEnd);
    }

    private void writeFilePart(DataOutputStream output, String boundary, String fileName, InputStream file) throws IOException {
        String lineEnd = "\r\n";
        output.writeBytes("--" + boundary + lineEnd);
        output.writeBytes("Content-Disposition: form-data; name=\"document\"; filename=\"" + fileName + "\"" + lineEnd);
        output.writeBytes("Content-Type: application/octet-stream" + lineEnd + lineEnd);
        file.transferTo(output);
        output.writeBytes(lineEnd);
    }

    private void writeClosingBoundary(DataOutputStream output, String boundary) throws IOException {
        String lineEnd = "\r\n";
        output.writeBytes("--" + boundary + "--" + lineEnd);
    }
}

