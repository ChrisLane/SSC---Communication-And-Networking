import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.swing.*;

public class GUI {
    private GmailClient gmail;
    private ClientView view;

    private Folder folder;

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
                        if (messageJList.getSelectedValue().equals(message.getSubject() + " - Seen: " + true) ||
                                messageJList.getSelectedValue().equals(message.getSubject() + " - Seen: " + false)) {

                            view.printMessage(message, messageJList);
                            message.setFlag(Flags.Flag.SEEN, true);
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

            gmail.sendMessage(to, cc, subject, message);

            emailTo.setText("");
            emailCC.setText("");
            emailSubject.setText("");
            emailBody.setText("");
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
