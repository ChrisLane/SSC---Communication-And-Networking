import javax.mail.*;
import javax.swing.*;
import java.io.IOException;
import java.util.Scanner;

public class ClientView {
    private Scanner in = new Scanner(System.in);
    private static Credentials login = new Credentials();
    private static GmailClient gmail;

    public static void main(String[] args) {
        ClientView clientView = new ClientView();
        clientView.collectCredentials();
        gmail = new GmailClient(login);

        clientView.optionSelect();
    }

    private void optionSelect() {
        int numberOfOptions = 2;
        boolean exit = false;

        while (!exit) {
            System.out.println("Please enter one of the following options:");
            printOptions();

            int option;
            while ((option = in.nextInt()) > numberOfOptions) {
                System.out.println("Your selection must be between 1 and " + numberOfOptions);
            }
            switch (option) {
                case 1:
                    printMail(gmail.getMail(gmail.getFolders()[0]));
                    break;
                case 2:
                    exit = true;
            }
        }
    }

    private void printOptions() {
        int i = 1;
        System.out.println(i + " - Show Emails");
        i++;
        System.out.println(i + " - Exit");
    }

    public void printMail(Message[] messages) {
        try {
            int count = 0;

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
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private void collectCredentials() {
        collectUsername();
        collectPassword();
    }

    private void collectUsername() {
        // Username input using JPasswordField
        JTextField username = new JTextField(20);
        int action = JOptionPane.showConfirmDialog(null, username, "Enter username", JOptionPane.OK_CANCEL_OPTION);
        if (action < 0) {
            JOptionPane.showMessageDialog(null, "Cancel, X or escape key selected");
            System.exit(0);
        } else {
            login.setUsername(username.getText());
        }
    }

    private void collectPassword() {
        // User password input using JPasswordField
        JPasswordField password = new JPasswordField(10);
        int action = JOptionPane.showConfirmDialog(null, password, "Enter Password", JOptionPane.OK_CANCEL_OPTION);
        if (action < 0) {
            JOptionPane.showMessageDialog(null, "Cancel, X or escape key selected");
            System.exit(0);
        } else {
            login.setPassword(new String(password.getPassword()));
        }
    }
}
