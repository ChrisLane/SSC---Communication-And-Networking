package Display;

import javax.mail.*;
import javax.swing.*;
import java.io.IOException;

/**
 * Contains methods for printing the information to be viewed in the GUI
 */
public class ClientView {
    private DefaultListModel<String> subjectModel;

    /**
     * Returns the model for the subject JList
     *
     * @return The model for the subject JList
     */
    public DefaultListModel getSubjectModel() {
        return subjectModel;
    }

    /**
     * Print subjects of messages to a given JList
     *
     * @param messages Messages to retrieve subjects from
     * @param jList    JList to print subjects to
     */
    public void printSubjects(Message[] messages, JList<String> jList) {
        subjectModel = new DefaultListModel<>();

        try {
            for (Message message : messages) {
                String subject = message.getSubject();

                // Add standard flags to the end of the subject if applicable
                String subjectWithFlags = subject + " -";
                if (message.isSet(Flags.Flag.SEEN)) {
                    subjectWithFlags += " READ";
                }
                if (message.isSet(Flags.Flag.ANSWERED)) {
                    subjectWithFlags += ", ANSWERED";
                }
                if (message.isSet(Flags.Flag.RECENT)) {
                    subjectWithFlags += ", RECENT";
                }

                // Add user flags to the end of the subject
                String[] userFlags = message.getFlags().getUserFlags();
                for (String flag : userFlags) {
                    subjectWithFlags += ", " + flag;
                }

                // Add string of subject and flags to the model
                subjectModel.addElement(subjectWithFlags);
                jList.setModel(subjectModel);
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        // Set completed model to the list
        jList.setModel(subjectModel);
    }

    /**
     * Print a message to a given JList
     *
     * @param message Message to be printed to the JList
     * @param jList   JList to print the message to
     */
    public void printMessage(Message message, JList<String> jList) {
        DefaultListModel<String> messageModel = new DefaultListModel<>();

        try {
            // Print standard text/plain email
            if (message.getContentType().contains("TEXT/PLAIN")) {
                messageModel.addElement(message.getContent().toString());
            }
            // Deconstruct multipart message and print the body
            else {
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

        // Set completed model of the printed message to the list
        jList.setModel(messageModel);
    }
}
