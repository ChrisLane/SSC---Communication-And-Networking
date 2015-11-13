import Display.ClientView;
import Email.Credentials;
import Email.GmailClient;
import Email.SearchMessage;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.SearchTerm;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Contains the main method to run the email client, GUI code and event handling
 */
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
        // Fixes tiny window on  High DPI screens
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("GUI");
        frame.setContentPane(new GUI().splitPane);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        // Create JSplitPanes to hold email view and send panels
        splitPane = new JSplitPane();
        splitPane.setBorder(BorderFactory.createTitledBorder("Email Client"));

        // Create the JPanel
        viewMail = new JPanel();
        viewMail.setLayout(new GridLayoutManager(6, 6, new Insets(20, 20, 20, 20), -1, -1));
        splitPane.setLeftComponent(viewMail);

        // Create and add the username input field
        usernameTextField = new JTextField();
        usernameTextField.setText("");
        usernameTextField.setToolTipText("Username");
        viewMail.add(usernameTextField, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));

        // Create and add the login button
        loginButton = new JButton();
        loginButton.setText("Login");
        loginButton.setMnemonic('L');
        loginButton.setDisplayedMnemonicIndex(0);
        loginButton.setToolTipText("Login");
        viewMail.add(loginButton, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Create and add the password input field
        passwordPasswordField = new JPasswordField();
        passwordPasswordField.setText("");
        passwordPasswordField.setToolTipText("Password");
        viewMail.add(passwordPasswordField, new GridConstraints(1, 3, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));

        // Create and add the "Username" label
        final JLabel label1 = new JLabel();
        label1.setText("Username");
        label1.setDisplayedMnemonic('U');
        label1.setDisplayedMnemonicIndex(0);
        viewMail.add(label1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Create and add the "Load Mailboxes" button
        mailboxesButton = new JButton();
        mailboxesButton.setText("Load Mailboxes");
        mailboxesButton.setMnemonic('O');
        mailboxesButton.setDisplayedMnemonicIndex(1);
        mailboxesButton.setToolTipText("Load Mailboxes");
        viewMail.add(mailboxesButton, new GridConstraints(3, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Create and add the scroll pane to hold the folders JList
        final JScrollPane scrollPane1 = new JScrollPane();
        viewMail.add(scrollPane1, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));

        // Create and add the JList for holding folders
        folderJList = new JList();
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        folderJList.setModel(defaultListModel1);
        folderJList.setToolTipText("Folders");
        scrollPane1.setViewportView(folderJList);

        // Create and add the scroll pane to hold the messages JList
        final JScrollPane scrollPane2 = new JScrollPane();
        viewMail.add(scrollPane2, new GridConstraints(3, 3, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));

        // Create and add the messages JList for holding messages
        messageJList = new JList();
        messageJList.setToolTipText("Messages");
        scrollPane2.setViewportView(messageJList);

        // Create and add the "Folders" label
        final JLabel label2 = new JLabel();
        label2.setText("Folders");
        viewMail.add(label2, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Create and add the "Search:" label
        final JLabel label3 = new JLabel();
        label3.setText("Search:");
        viewMail.add(label3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Create and add the message searching text field
        searchTextField = new JTextField();
        viewMail.add(searchTextField, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));

        // Create and add the "Run Search" button
        runSearchButton = new JButton();
        runSearchButton.setText("Run Search");
        viewMail.add(runSearchButton, new GridConstraints(4, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Create and add the criteria text input field
        criteriaTextField = new JTextField();
        viewMail.add(criteriaTextField, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));

        // Create and add the "Criteria:" label
        final JLabel label4 = new JLabel();
        label4.setText("Criteria:");
        viewMail.add(label4, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Create and add the "Move to Spam" button
        moveToSpamButton = new JButton();
        moveToSpamButton.setText("Move to Spam");
        viewMail.add(moveToSpamButton, new GridConstraints(5, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Create and add the "Apply Flag" button
        applyFlagButton = new JButton();
        applyFlagButton.setText("Apply Flag");
        viewMail.add(applyFlagButton, new GridConstraints(5, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Create and add the flag text input field
        flagTextField = new JTextField();
        viewMail.add(flagTextField, new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));

        // Create and add the "Password" label
        final JLabel label5 = new JLabel();
        label5.setText("Password");
        label5.setDisplayedMnemonic('P');
        label5.setDisplayedMnemonicIndex(0);
        viewMail.add(label5, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Create and add the "Mail" label
        final JLabel label6 = new JLabel();
        label6.setText("Mail");
        viewMail.add(label6, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Create and add the "Flag" label
        final JLabel label7 = new JLabel();
        label7.setText("Flag");
        viewMail.add(label7, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Create and add a panel to hold components used to send mail
        sendMail = new JPanel();
        sendMail.setLayout(new GridLayoutManager(10, 1, new Insets(20, 20, 20, 20), -1, -1));
        splitPane.setRightComponent(sendMail);

        // Create and add the Email To input text field
        emailTo = new JTextField();
        emailTo.setText("");
        emailTo.setToolTipText("To");
        sendMail.add(emailTo, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));

        // Create and add the Email CC input text field
        emailCC = new JTextField();
        emailCC.setText("");
        emailCC.setToolTipText("CC");
        sendMail.add(emailCC, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));

        // Create and add the subject input text field
        emailSubject = new JTextField();
        emailSubject.setText("");
        emailSubject.setToolTipText("Subject");
        sendMail.add(emailSubject, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));

        // Create the Email "To" label
        final JLabel label8 = new JLabel();
        label8.setText("To");
        label8.setDisplayedMnemonic('T');
        label8.setDisplayedMnemonicIndex(0);
        sendMail.add(label8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Create the Email "CC" label
        final JLabel label9 = new JLabel();
        label9.setText("CC");
        label9.setDisplayedMnemonic('C');
        label9.setDisplayedMnemonicIndex(0);
        sendMail.add(label9, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Create the Email "Subject" label
        final JLabel label10 = new JLabel();
        label10.setText("Subject");
        sendMail.add(label10, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Create the Email "Message" label
        final JLabel label11 = new JLabel();
        label11.setText("Message");
        sendMail.add(label11, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Create and add the "Send Email" button
        sendEmailButton = new JButton();
        sendEmailButton.setText("Send Email");
        sendEmailButton.setMnemonic('S');
        sendEmailButton.setDisplayedMnemonicIndex(0);
        sendEmailButton.setToolTipText("Send Email");
        sendMail.add(sendEmailButton, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Create and add the scroll pane for email body
        final JScrollPane scrollPane3 = new JScrollPane();
        sendMail.add(scrollPane3, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(200, 200), null, 0, false));
        emailBody = new JTextArea();
        emailBody.setLineWrap(true);
        emailBody.setToolTipText("Message");
        scrollPane3.setViewportView(emailBody);

        // Create and add attachment button
        attachFileButton = new JButton();
        attachFileButton.setText("Attach File");
        sendMail.add(attachFileButton, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Link labels to other components
        label1.setLabelFor(usernameTextField);
        label2.setLabelFor(scrollPane1);
        label3.setLabelFor(searchTextField);
        label4.setLabelFor(criteriaTextField);
        label5.setLabelFor(passwordPasswordField);
        label6.setLabelFor(scrollPane2);
        label7.setLabelFor(flagTextField);
        label8.setLabelFor(emailTo);
        label9.setLabelFor(emailCC);
        label10.setLabelFor(emailSubject);
        label11.setLabelFor(emailBody);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return splitPane;
    }
}
