package Email;

public class Credentials {
    private String username;
    private char[] password;

    /**
     * Set the username and password for this object.
     * @param username String to be set as the username
     * @param password char[] to be set as the password
     */
    public void setCredentials(String username, char[] password) {
        setUsername(username);
        setPassword(password);
    }

    /**
     * Returns the username
     * @return Return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username
     * @param username String to be set as the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the password
     * @return Return the password as a String
     */
    public String getPassword() {
        return new String(password);
    }

    /**
     * Sets the password
     * @param password char[] to be set as the password
     */
    public void setPassword(char[] password) {
        this.password = password;
    }
}
