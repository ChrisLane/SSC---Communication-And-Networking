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

    /**
     * Create a new GmailClient object with a given login
     *
     * @param login Credentials to be used to sign in to the GMail account
     */
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

        // Connect to the IMAP store
        try {
            store = session.getStore("imap");
        } catch (NoSuchProviderException e) {
            System.err.println("We could not find this provider");
            e.printStackTrace();
        }

        // Login
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

    /**
     * Returns the messages from a given mail folder
     *
     * @param folder Folder to retrieve the messages from
     * @return All messages contained in the given folder
     */
    public Message[] getMail(Folder folder) {
        Message[] messages = null;

        try {
            if (!folder.isOpen())
                folder.open(Folder.READ_WRITE);

            messages = folder.getMessages();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Returns all folders of the account
     *
     * @return All folders of the account
     */
    public Folder[] getFolders() {
        Folder[] folders = null;

        try {
            folders = store.getDefaultFolder().list();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return folders;
    }

    /**
     * Send an email
     *
     * @param to         Email address to send to
     * @param cc         Email address to send carbon copy to
     * @param subject    Subject of email
     * @param message    Content of email
     * @param attachment File attachment for email
     */
    public void sendMessage(String to, String cc, String subject, String message, File attachment) {
        MimeMessage mimeMessage = new MimeMessage(session);

        try {
            // Set headers
            mimeMessage.setFrom(new InternetAddress(username));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            mimeMessage.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
            mimeMessage.setSubject(subject);

            // Set main content
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(message, "text/plain");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // Add file attachment
            if (!(attachment == null)) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(attachment);
                multipart.addBodyPart(attachmentPart);
            }

            mimeMessage.setContent(multipart);
            mimeMessage.saveChanges();

            // Send the message by javax.mail.Transport .
            Transport tr = session.getTransport("smtp");    // Get Transport object from session
            tr.connect(smtpHost, username, password); // We need to connect
            tr.sendMessage(mimeMessage, mimeMessage.getAllRecipients()); // Send message
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }
}
