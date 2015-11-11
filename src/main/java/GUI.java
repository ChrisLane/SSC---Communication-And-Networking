import Display.ClientView;
import Email.Credentials;
import Email.GmailClient;

import javax.mail.*;
import javax.mail.search.SearchTerm;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
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

    private GUI() {
        loginButton.addActionListener(e -> {
            String username = usernameTextField.getText();
            char[] password = passwordPasswordField.getPassword();
            Credentials credentials = new Credentials();
            credentials.setCredentials(username, password);

            gmail = new GmailClient(credentials);
            view = new ClientView();

        });

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
        folderJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                folder = folderJList.getSelectedValue();
                Message[] messages = gmail.getMail(folder);
                view.printSubjects(messages, messageJList);
            }
        });

        messageJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !messageJList.getModel().equals(view.getMessageModel())) {
                Message[] messages = gmail.getMail(folder);
                for (Message message : messages) {
                    try {
                        String selectedSubject = messageJList.getSelectedValue()
                                .replaceAll(" - READ", "");
                        String messageSubject = message.getSubject();
                        if (selectedSubject.equals(messageSubject)) {

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

        sendEmailButton.addActionListener(e -> {
            String to = emailTo.getText();
            String cc = emailCC.getText();
            String subject = emailSubject.getText();
            String message = emailBody.getText();

            gmail.sendMessage(to, cc, subject, message, attachment);

            emailTo.setText("");
            emailCC.setText("");
            emailSubject.setText("");
            emailBody.setText("");
            attachment = null;
        });

        attachFileButton.addActionListener(e -> {
            JFileChooser fileSelect = new JFileChooser();
            fileSelect.showOpenDialog(splitPane);

            attachment = fileSelect.getSelectedFile();
        });

        runSearchButton.addActionListener(e -> {
            Message[] messages = gmail.getMail(folder);
            String searchTerm = searchTextField.getText();

            SearchTerm search = new SearchTerm() {
                @Override
                public boolean match(Message message) {
                    try {
                        if (message.getSubject().contains(searchTerm) || message.getContent().toString().contains(searchTerm)) {
                            return true;
                        }
                    } catch (MessagingException | IOException e1) {
                        e1.printStackTrace();
                    }
                    return false;
                }
            };

            ArrayList<Message> matchStore = new ArrayList<>();
            for (Message message : messages) {
                try {
                    if (message.match(search)) {
                        matchStore.add(message);
                    }
                } catch (MessagingException e1) {
                    e1.printStackTrace();
                }
            }
            Message[] matchedMessages = new Message[matchStore.size()];
            matchStore.toArray(matchedMessages);

            view.printSubjects(matchedMessages, messageJList);
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("GUI");
        frame.setContentPane(new GUI().splitPane);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
