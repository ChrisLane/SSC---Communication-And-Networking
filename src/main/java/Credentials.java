import javax.swing.*;

public class Credentials {
    private String username;
    private String password;

    public Credentials() {
        setCredentials();
    }

    public void setCredentials() {
        setUsername();
        setPassword();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername() {
        // Username input using JPasswordField
        JTextField username = new JTextField(20);
        int action = JOptionPane.showConfirmDialog(null, username, "Enter username", JOptionPane.OK_CANCEL_OPTION);
        if (action < 0) {
            JOptionPane.showMessageDialog(null, "Cancel, X or escape key selected");
            System.exit(0);
        } else {
            this.username = username.getText();
            System.out.println(this.username);
        }
    }

    public void setPassword() {
        // User password input using JPasswordField
        JPasswordField password = new JPasswordField(10);
        int action = JOptionPane.showConfirmDialog(null, password, "Enter Password", JOptionPane.OK_CANCEL_OPTION);
        if (action < 0) {
            JOptionPane.showMessageDialog(null, "Cancel, X or escape key selected");
            System.exit(0);
        } else {
            this.password = new String(password.getPassword());
            System.out.println(this.password);
        }
    }
}
