import com.sun.mail.imap.IMAPFolder;

import javax.mail.*;
import java.io.IOException;
import java.util.Properties;

public class GmailClient {
    Store store = null;
    Folder folder;

    public GmailClient(Credentials login) {
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
            if (store != null) {
                store.connect(host, login.getUsername(), login.getPassword());
            } else {
                System.err.println("No imap store provided by provider");
            }
        } catch (MessagingException e) {
            System.err.println("Could not connect to store provided");
            e.printStackTrace();
        }
    }

    public void showMail() {
        try {
            // Step 3: Choose a folder, in this case, we chose inbox
            folder = (IMAPFolder) store.getFolder("inbox");

            // Step 4: Open the folder
            if (!folder.isOpen())
                folder.open(Folder.READ_WRITE);

            // Step 5: Get messages from the folder
            // Get total number of message
            System.out.println("No of Messages : " + folder.getMessageCount());
            // Get total number of unread message
            System.out.println("No of Unread Messages : " + folder.getUnreadMessageCount());

            int count = 0;
            Message messages[] = folder.getMessages();

            // Get all messages
            for (Message message : messages) {
                count++;

                // Get subject of each message
                System.out.println("The " + count + "th message is: " + message.getSubject());
                //System.out.println(message.getContentType());
                try {
                    if (message.getContentType().contains("TEXT/PLAIN")) {
                        System.out.println(message.getContent());
                    } else {
                        // How to get parts from multiple body parts of MIME message
                        Multipart multipart = (Multipart) message.getContent();
                        System.out.println("-----------" + multipart.getCount() + "----------------");
                        for (int x = 0; x < multipart.getCount(); x++) {
                            BodyPart bodyPart = multipart.getBodyPart(x);
                            // If the part is a plan text message, then print it out.
                            if (bodyPart.getContentType().contains("TEXT/PLAIN")) {
                                System.out.println(bodyPart.getContentType());
                                System.out.println(bodyPart.getContent().toString());
                            }

                        }
                    }
                } catch (IOException e) {
                    System.err.println("Issue with IO!");
                }

                Flags mes_flag = message.getFlags();
                System.out.println("Has this message been read?  " + mes_flag.contains(Flags.Flag.SEEN));
            }
        } catch (NoSuchProviderException e) {
            System.err.println("Provider does not exist");
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } finally {
            try {
                if (folder != null && folder.isOpen()) {
                    folder.close(true);
                }
                if (store != null) {
                    store.close();
                }
            } catch (MessagingException e) {
                System.err.println("Could not close the folder or store");
            }
        }
    }
}
