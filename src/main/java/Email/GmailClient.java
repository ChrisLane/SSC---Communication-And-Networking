package Email;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class GmailClient {
    private Store store = null;
    private Session session;
    private String smtpHost;
    private String username;
    private String password;

    public GmailClient(Credentials login) {
        username = login.getUsername();
        password = login.getPassword();
        String imapHost = "imap.gmail.com";
        smtpHost = "smtp.gmail.com";
        Properties props = new Properties();
        props.setProperty("mail.imap.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", "587");
        props.setProperty("mail.user", username);
        props.setProperty("mail.password", password);
        session = Session.getInstance(props);

        try {
            store = session.getStore("imap");
        } catch (NoSuchProviderException e) {
            System.err.println("We could not find this provider");
            e.printStackTrace();
        }

        try {
            if (!username.isEmpty()) {
                store.connect(imapHost, username, password);
            } else {
                System.err.println("No username provided");
            }
        } catch (MessagingException e) {
            System.err.println("Could not connect to store provided");
            e.printStackTrace();
        }
    }

    public Message[] getMail(Folder folder) {
        Message[] messages = null;
        // Step 4: Open the folder
        try {
            if (!folder.isOpen())
                folder.open(Folder.READ_WRITE);

            messages = folder.getMessages();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public Folder[] getFolders() {
        Folder[] folders = null;
        try {
            folders = store.getDefaultFolder().list();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return folders;
    }

    public void sendMessage(String to, String cc, String subject, String message, File attachment) {
        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            mimeMessage.setFrom(new InternetAddress(username));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            mimeMessage.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
            mimeMessage.setSubject(subject);

            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(message, "text/plain");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            if (!(attachment == null)) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(attachment);
                multipart.addBodyPart(attachmentPart);
            }

            mimeMessage.setContent(multipart);
            mimeMessage.saveChanges();

            // Step 4: Send the message by javax.mail.Transport .
            Transport tr = session.getTransport("smtp");    // Get Transport object from session
            tr.connect(smtpHost, username, password); // We need to connect
            tr.sendMessage(mimeMessage, mimeMessage.getAllRecipients()); // Send message
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
