import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.util.Enumeration;

public class SearchMessage extends SearchTerm {
    private String searchTerm;

    public SearchMessage(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    @Override
    public boolean match(Message message) {
        try {
            Enumeration headers = message.getAllHeaders();
            while (headers.hasMoreElements()) {
                Header header = (Header) headers.nextElement();
                if (header.getValue().contains(searchTerm)) {
                    return true;
                }
            }
            if (message.getContent().toString().contains(searchTerm)) {
                return true;
            }
        } catch (MessagingException | IOException e1) {
            e1.printStackTrace();
        }
        return false;
    }
}
