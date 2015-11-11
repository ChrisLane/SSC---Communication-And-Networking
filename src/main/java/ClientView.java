import javax.mail.*;
import javax.swing.*;
import java.io.IOException;

public class ClientView {
    private GmailClient gmail;
    private DefaultListModel<String> messageModel;

    public ClientView(GmailClient gmail) {
        this.gmail = gmail;
    }

    public void printSubjects(Message[] messages, JList<String> jList) {
        DefaultListModel<String> subjectModel = new DefaultListModel<>();

        try {
            for (Message message : messages) {
                subjectModel.addElement(message.getSubject() + " - Seen: " + message.isSet(Flags.Flag.SEEN));
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        jList.setModel(subjectModel);
    }

    public DefaultListModel<String> getMessageModel() {
        return messageModel;
    }

    public void printMessage(Message message, JList<String> jList) {
        messageModel = new DefaultListModel<>();
        try {
            if (message.getContentType().contains("TEXT/PLAIN")) {
                messageModel.addElement(message.getContent().toString());
            } else {
                Multipart multipart = (Multipart) message.getContent();
                for (int x = 0; x < multipart.getCount(); x++) {
                    BodyPart bodyPart = multipart.getBodyPart(x);
                    if (bodyPart.getContentType().contains("TEXT/PLAIN")) {
                        messageModel.addElement(bodyPart.getContent().toString());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Issue with IO!");
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        jList.setModel(messageModel);
    }
}
