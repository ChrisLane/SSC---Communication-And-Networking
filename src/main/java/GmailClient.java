import javax.mail.*;
import java.util.Properties;

public class GmailClient {
    Store store = null;

    public GmailClient(Credentials login) {
        String username = login.getUsername();
        String host = "imap.gmail.com";
        Properties props = new Properties();
        props.setProperty("mail.imap.ssl.enable", "true");
        Session session = Session.getInstance(props);

        try {
            store = session.getStore("imap");
        } catch (NoSuchProviderException e) {
            System.err.println("We could not find this provider");
            e.printStackTrace();
        }

        try {
            if (!username.isEmpty()) {
                store.connect(host, username, login.getPassword());
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
}
