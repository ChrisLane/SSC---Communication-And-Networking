package Display;

import javax.mail.*;
import javax.swing.*;
import java.io.IOException;

public class ClientView {
    private DefaultListModel<String> messageModel;

    public void printSubjects(Message[] messages, JList<String> jList) {
        DefaultListModel<String> subjectModel = new DefaultListModel<>();

        try {
            for (Message message : messages) {
                String subject = message.getSubject();
                if (message.isSet(Flags.Flag.SEEN)) {
                    subjectModel.addElement(subject + " - READ");
                } else {
                    subjectModel.addElement(subject);
                }
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
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
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
