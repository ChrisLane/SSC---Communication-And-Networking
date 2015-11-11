import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    private JList messageJList;
    private JPanel sendMail;
    private JTextField emailTo;
    private JTextField emailCC;
    private JTextField emailSubject;
    private JTextArea emailBody;
    private JSplitPane splitPane;
    private JButton sendEmailButton;

    public GUI() {
        loginButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                Credentials credentials = new Credentials();
                credentials.setUsername(usernameTextField.getText());
                credentials.setPassword(passwordPasswordField.getPassword());

                gmail = new GmailClient(credentials);
                view = new ClientView(gmail);

            }
        });

        mailboxesButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                Folder[] folders = gmail.getFolders();
                DefaultListModel<Folder> folderModel = new DefaultListModel<>();

                for (Folder folder : folders) {
                    try {
                        if (folder.list().length < 1) {
                            folderModel.addElement(folder);
                        } else {
                            for (Folder innerFolder : folder.list()) {
                                folderModel.addElement(innerFolder);
                            }
                        }
                    } catch (MessagingException e1) {
                        e1.printStackTrace();
                    }
                }
                folderJList.setModel(folderModel);
            }
        });
        folderJList.addListSelectionListener(new ListSelectionListener() {
            /**
             * Called whenever the value of the selection changes.
             *
             * @param e the event that characterizes the change.
             */
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    folder = folderJList.getSelectedValue();
                    Message[] messages = gmail.getMail(folder);
                    view.printSubjects(messages, messageJList);
                }
            }
        });

        messageJList.addListSelectionListener(new ListSelectionListener() {
            /**
             * Called whenever the value of the selection changes.
             *
             * @param e the event that characterizes the change.
             */
            @Override
            public void valueChanged(ListSelectionEvent e) {
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
            }
        });

        sendEmailButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                String to = emailTo.getText();
                String cc = emailCC.getText();
                String subject = emailSubject.getText();
                String message = emailBody.getText();

                gmail.sendMessage(to, cc, subject, message);

                emailTo.setText("");
                emailCC.setText("");
                emailSubject.setText("");
                emailBody.setText("");
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("GUI");
        frame.setContentPane(new GUI().splitPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
