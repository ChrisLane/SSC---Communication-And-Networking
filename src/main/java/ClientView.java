import javax.swing.*;
import java.util.Scanner;

public class ClientView {
    private Scanner in = new Scanner(System.in);
    private static Credentials login = new Credentials();
    private static GmailClient gmail;

    public static void main(String[] args) {
        ClientView clientView = new ClientView();
        clientView.collectCredentials();
        gmail = new GmailClient(login);

        clientView.optionSelect();
    }

    public void optionSelect() {
        int numberOfOptions = 2;
        boolean exit = false;

        while (!exit) {
            System.out.println("Please enter one of the following options:");
            printOptions();

            int option;
            while ((option = in.nextInt()) > numberOfOptions) {
                System.out.println("Your selection must be between 1 and " + numberOfOptions);
            }
            switch (option) {
                case 1:
                    gmail.showMail();
                    break;
                case 2:
                    exit = true;
            }
        }
    }

    private void printOptions() {
        int i = 1;
        System.out.println(i + " - Show Emails");
        i++;
        System.out.println(i + " - Exit");
    }

    private void collectCredentials() {
        collectUsername();
        collectPassword();
    }

    private void collectUsername() {
        // Username input using JPasswordField
        JTextField username = new JTextField(20);
        int action = JOptionPane.showConfirmDialog(null, username, "Enter username", JOptionPane.OK_CANCEL_OPTION);
        if (action < 0) {
            JOptionPane.showMessageDialog(null, "Cancel, X or escape key selected");
            System.exit(0);
        } else {
            login.setUsername(username.getText());
        }
    }

    private void collectPassword() {
        // User password input using JPasswordField
        JPasswordField password = new JPasswordField(10);
        int action = JOptionPane.showConfirmDialog(null, password, "Enter Password", JOptionPane.OK_CANCEL_OPTION);
        if (action < 0) {
            JOptionPane.showMessageDialog(null, "Cancel, X or escape key selected");
            System.exit(0);
        } else {
            login.setPassword(new String(password.getPassword()));
        }
    }
}
