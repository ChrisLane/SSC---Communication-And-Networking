import Display.ClientView;
import Email.Credentials;
import Email.GmailClient;
import Email.SearchMessage;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.SearchTerm;
import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

public class GUI {
    private GmailClient gmail;
    private ClientView view;

    private Folder folder;

    private File attachment;

    private JButton loginButton;
    private JPanel viewMail;
    private JList<Folder> folderJList;
    private JTextField usernameTextField;
    private JPasswordField passwordPasswordField;
    private JButton mailboxesButton;
    private JList<String> messageJList;
    private JPanel sendMail;
    private JTextField emailTo;
    private JTextField emailCC;
    private JTextField emailSubject;
    private JTextArea emailBody;
    private JSplitPane splitPane;
    private JButton sendEmailButton;
    private JButton attachFileButton;
    private JTextField searchTextField;
    private JButton runSearchButton;
    private JButton moveToSpamButton;
    private JTextField criteriaTextField;
    private JButton applyFlagButton;
    private JTextField flagTextField;

    private GUI() {
        /**
         * When the login button is pressed, the username and password inputs will be taken to log into the account.
         */
        loginButton.addActionListener(e -> {
            // Take username and password input
            String username = usernameTextField.getText();
            char[] password = passwordPasswordField.getPassword();
            // Set credentials using username and password provided
            Credentials credentials = new Credentials();
            credentials.setCredentials(username, password);

            // Connect to GMail
            gmail = new GmailClient(credentials);
            view = new ClientView();
        });

        /**
         * When the "Load Mailboxes" button is pressed, all folders will be placed in the folders section.
         */
        mailboxesButton.addActionListener(e -> {
            Folder[] folders = gmail.getFolders();
            DefaultListModel<Folder> folderModel = new DefaultListModel<>();

            for (Folder folder1 : folders) {
                try {
                    if (folder1.list().length < 1) {
                        folderModel.addElement(folder1);
                    } else {
                        for (Folder innerFolder : folder1.list()) {
                            folderModel.addElement(innerFolder);
                        }
                    }
                } catch (MessagingException e1) {
                    e1.printStackTrace();
                }
            }
            folderJList.setModel(folderModel);
        });

        /**
         * When a folder is clicked, the folder's message subjects will be loaded into the messages section.
         */
        folderJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                folder = folderJList.getSelectedValue();
                Message[] messages = gmail.getMail(folder);
                view.printSubjects(messages, messageJList);
            }
        });

        /**
         * When a subject is clicked, the messages section will be replaced with the message belonging to said subject.
         */
        messageJList.addListSelectionListener(e -> {
            // Only runs if the JList isn't already displaying a message
            if (!e.getValueIsAdjusting() && !messageJList.getModel().equals(view.getMessageModel())) {
                Message[] messages = gmail.getMail(folder);

                // Check all messages for matching subjects to value from clicked and open a matching message
                for (Message message : messages) {
                    try {
                        // Remove possible flags that were added to the subject string
                        String selectedSubject = messageJList.getSelectedValue()
                                .replaceAll(" -", "")
                                .replaceAll(" READ", "")
                                .replaceAll(", ANSWERED", "")
                                .replaceAll(", RECENT", "");

                        String messageSubject = message.getSubject();
                        if (selectedSubject.equals(messageSubject)) {
                            // The subjects match! Print the message and flag as seen
                            view.printMessage(message, messageJList);
                            message.setFlag(Flags.Flag.SEEN, true);
                            break;
                        }
                    } catch (MessagingException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        /**
         * When the "Send Email" button is pressed, data will be taken from inputs, formed and sent as an email.
         */
        sendEmailButton.addActionListener(e -> {
            String to = emailTo.getText();
            String cc = emailCC.getText();
            String subject = emailSubject.getText();
            String message = emailBody.getText();

            // Send the email constructed from the inputs
            gmail.sendMessage(to, cc, subject, message, attachment);

            // Set all text boxes to blank
            emailTo.setText("");
            emailCC.setText("");
            emailSubject.setText("");
            emailBody.setText("");
            attachment = null;
        });

        /**
         * When the "Attach File" button is pressed, a file selection window will be opened to allow setting a file
         * to be attached to an email.
         */
        attachFileButton.addActionListener(e -> {
            // Open a new file selection window
            JFileChooser fileSelect = new JFileChooser();
            fileSelect.showOpenDialog(splitPane);

            // Set the email attachment to the selected file
            attachment = fileSelect.getSelectedFile();
        });

        /**
         * When the "Run Search" button is pressed, the program will check message headers and content for the given
         * string.
         */
        runSearchButton.addActionListener(e -> {
            Message[] messages = gmail.getMail(folder);
            String searchTerm = searchTextField.getText();

            SearchTerm search = new SearchMessage(searchTerm);

            ArrayList<Message> matchStore = new ArrayList<>();
            // Check all messages for matching the search criteria
            for (Message message : messages) {
                try {
                    if (message.match(search)) {
                        matchStore.add(message);
                    }
                } catch (MessagingException e1) {
                    e1.printStackTrace();
                }
            }
            // Create a message array from the arraylist to pass to the next method
            Message[] matchedMessages = new Message[matchStore.size()];
            matchStore.toArray(matchedMessages);

            // Print subjects of messages matching the search criteria
            view.printSubjects(matchedMessages, messageJList);
        });

        /**
         * When the "Move to Spam" button is pressed, messages matching the given criteria will be moved to a spam
         * folder and removed from the current folder.
         */
        moveToSpamButton.addActionListener(e -> {
            Message[] messages = gmail.getMail(folder);
            String searchTerm = criteriaTextField.getText();

            SearchTerm search = new SearchMessage(searchTerm);

            ArrayList<Message> matchStore = new ArrayList<>();
            // Check all messages for matching the search criteria
            for (Message message : messages) {
                try {
                    if (message.match(search)) {
                        matchStore.add(message);
                        // Mark the email for deletion from the current folder
                        message.setFlag(Flags.Flag.DELETED, true);
                    }
                } catch (MessagingException e1) {
                    e1.printStackTrace();
                }
            }
            Message[] matchedMessages = new Message[matchStore.size()];
            matchStore.toArray(matchedMessages);

            Folder spamFolder = null;
            // Find the spam folder
            for (Folder folder : gmail.getFolders()) {
                try {
                    if (folder.list().length < 1) {
                        if (folder.getName().contains("Spam")) {
                            spamFolder = folder;
                        }
                    } else {
                        for (Folder innerFolder : folder.list()) {
                            if (innerFolder.getName().contains("Spam")) {
                                spamFolder = innerFolder;
                            }
                        }
                    }
                } catch (MessagingException e1) {
                    e1.printStackTrace();
                }
            }

            // If the spam folder was found then copy the messages to it and delete messages from the old folder
            try {
                if (!(spamFolder == null)) {
                    folder.copyMessages(matchedMessages, spamFolder);
                    folder.expunge();
                }
            } catch (MessagingException e1) {
                e1.printStackTrace();
            }
        });

        /**
         * When the "Apply flag" button is pressed, messages meeting the given criteria will have the given custom flag
         * to them.
         */
        applyFlagButton.addActionListener(e -> {
            Message[] messages = gmail.getMail(folder);
            String searchTerm = criteriaTextField.getText();

            SearchTerm search = new SearchMessage(searchTerm);

            Flags flag = new Flags(flagTextField.getText());
            // Find all messages matching the search criteria and apply a given flag to them
            for (Message message : messages) {
                try {
                    if (message.match(search)) {
                        message.setFlags(flag, true);
                    }
                } catch (MessagingException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    /**
     * Run the email client
     *
     * @param args Runtime arguments
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("GUI");
        frame.setContentPane(new GUI().splitPane);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
