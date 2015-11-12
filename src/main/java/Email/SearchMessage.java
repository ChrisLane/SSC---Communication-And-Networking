package Email;

import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.util.Enumeration;

public class SearchMessage extends SearchTerm {
    private String searchTerm;

    /**
     * Create a new Email.SearchMessage object
     *
     * @param searchTerm The string to search a message for
     */
    public SearchMessage(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    /**
     * Search a message for a given string
     *
     * @param message Message to be searched
     * @return If a message contains the string being searched for
     */
    @Override
    public boolean match(Message message) {
        try {
            Enumeration headers = message.getAllHeaders();

            // Loop through all headers
            while (headers.hasMoreElements()) {
                Header header = (Header) headers.nextElement();
                if (header.getValue().contains(searchTerm)) {
                    return true;
                }
            }

            if (message.getContent().toString().contains(searchTerm)) {
                return true;
            }
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
