package plugin.Followers;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;
import plugin.Abstractions.Follower;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


public class EmailFollower implements Follower {

    private final String smtpHost;
    private final String smtpPort;
    private final String username;
    private final String password;
    private final List<String> recipients;

    public EmailFollower() {
        Config config = ConfigFactory.load();
        smtpHost = config.getString("transporter.email.smtpHost");
        smtpPort = config.getString("transporter.email.smtpPort");
        username = config.getString("transporter.email.username");
        password = config.getString("transporter.email.password");

        String recipientsEnv = config.getString("transporter.email.recipients");
        recipients = Arrays.stream(recipientsEnv.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    @Override
    public void SendData(InputStream file, String fileName) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));

        InternetAddress[] addresses = recipients.stream()
                .map(addr -> {
                    try {
                        return new InternetAddress(addr);
                    } catch (AddressException e) {
                        throw new RuntimeException("Invalid email: " + addr, e);
                    }
                })
                .toArray(InternetAddress[]::new);
        message.setRecipients(Message.RecipientType.TO, addresses);
        message.setSubject("Отчет после нагрузочного тестирования");

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("Привет, во вложении файл.");

        MimeBodyPart filePart = new MimeBodyPart();
        DataSource source = new ByteArrayDataSource(file, "application/octet-stream");
        filePart.setDataHandler(new DataHandler(source));
        filePart.setFileName(fileName);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(filePart);

        message.setContent(multipart);

        Transport.send(message);
    }
}
