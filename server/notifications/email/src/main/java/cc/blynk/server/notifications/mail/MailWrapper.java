package cc.blynk.server.notifications.mail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 06.04.15.
 */
public class MailWrapper {

    private static final Logger log = LogManager.getLogger(MailWrapper.class);

    private final Session session;
    private final InternetAddress from;

    public MailWrapper(Properties mailProperties) {
        String username = mailProperties.getProperty("mail.smtp.username");
        String password = mailProperties.getProperty("mail.smtp.password");

        log.info("Initializing mail transport. Username : {}. SMTP host : {}:{}",
                username, mailProperties.getProperty("mail.smtp.host"), mailProperties.getProperty("mail.smtp.port"));

        this.session = Session.getInstance(mailProperties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        try {
            this.from = new InternetAddress(username);
        } catch (AddressException e) {
            throw new RuntimeException("Error initializing MailWrapper.");
        }
    }

    public void send(String to, String subj, String body) throws MessagingException {
        send(to, subj, body, null);
    }

    public void send(String to, String subj, String body, String contentType) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(from);
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subj);
        if (contentType == null) {
            message.setText(body);
        } else {
            message.setContent(body, contentType);
        }

        Transport.send(message);
        log.trace("Mail to {} was sent. Subj : {}, body : {}", to, subj, body);
    }

}
