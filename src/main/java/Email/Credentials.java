package Email;

public class Credentials {
    private String username;
    private char[] password;

    public void setCredentials(String username, char[] password) {
        setUsername(username);
        setPassword(password);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return new String(password);
    }

    public void setPassword(char[] password) {
        this.password = password;
    }
}
