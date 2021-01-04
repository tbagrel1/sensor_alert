package com.tbagrel1.sensoralert;

import android.text.TextUtils;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Helper class containing all the static methods used to send and handle emails.
 */
public class EmailHandling {
    /**
     * Returns an email session allowing to send email through the SMTP set in the app preferences.
     *
     * @param preferences preferences of the application
     * @return an email session
     */
    public static Session getEmailSession(PreferencesWrapper preferences) {
        Properties properties = new Properties();
        String smtpHost = preferences.getString("smtp_host");
        int smtpPort = preferences.getInt("smtp_port");
        boolean useStarttls = preferences.getBoolean("use_starttls");
        String emailUsername = preferences.getString("smtp_username");
        String emailPassword = preferences.getString("smtp_password");

        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", String.valueOf(smtpPort));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", useStarttls ? "true" : "false");

        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUsername, emailPassword);
            }
        });
    }

    /**
     * Creates a MimeBodyPart from an HTML string representing an email body.
     *
     * @param htmlContent the HTML string representing the email body
     * @return a MimeBodyPart which can be included in a multipart email
     * @throws MessagingException if the MimeBodyPart cannot be made
     */
    public static MimeBodyPart makeHtmlBodyPart(String htmlContent) throws MessagingException {
        MimeBodyPart part = new MimeBodyPart();
        part.setContent(htmlContent, "text/html");
        return part;
    }

    /**
     * Creates a MimeBodyPart for an email attachement.
     *
     * @param path     full path to the file to attach
     * @param fileName filename displayed in the email client
     * @return a MimeBodyPart which can be included in a multipart email
     * @throws MessagingException if the MimeBodyPart cannot be made
     */
    public static MimeBodyPart makeAttachmentBodyPart(String path, String fileName)
        throws MessagingException {
        MimeBodyPart part = new MimeBodyPart();
        DataSource source = new FileDataSource(path);
        part.setDataHandler(new DataHandler(source));
        part.setFileName(fileName);
        return part;
    }

    /**
     * Creates a multipart from one or several MimeBodyParts
     *
     * @param parts the parts to package together
     * @return the corresponding multipart
     * @throws MessagingException if the multipart cannot be made
     */
    public static Multipart makeMultipart(MimeBodyPart... parts) throws MessagingException {
        Multipart multipart = new MimeMultipart();
        for (MimeBodyPart part : parts) {
            multipart.addBodyPart(part);
        }
        return multipart;
    }

    /**
     * Sends an email using the specified session.
     *
     * @param emailSession email session with the SMTP
     * @param sender       email address of the sender
     * @param recipients   email addresses of the recipients
     * @param subject      subject of the email
     * @param multipart    body of the email as a multipart bundle
     * @throws Exception if the email cannot be made or sent
     */
    public static void sendEmail(
        Session emailSession,
        String sender,
        String[] recipients,
        String subject,
        Multipart multipart
    ) throws Exception {
        Message message = new MimeMessage(emailSession);
        message.setFrom(new InternetAddress(sender));
        Address[] recipientAddresses = new Address[recipients.length];
        for (int i = 0; i < recipients.length; ++i) {
            recipientAddresses[i] = new InternetAddress(recipients[i]);
        }
        message.setRecipients(Message.RecipientType.TO, recipientAddresses);
        message.setSubject(subject);
        message.setContent(multipart);
        Transport.send(message);
    }

    /**
     * Regex email validation
     *
     * @param email the email address to validate
     * @return true iif the email is valid according to the RFC
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) &&
               android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
