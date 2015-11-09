import java.util.Scanner;

public class ClientView {
    private Scanner in = new Scanner(System.in);
    private Credentials login = new Credentials();
    private GmailClient gmail = new GmailClient(login);

    public static void main(String[] args) {
        ClientView clientView = new ClientView();

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
}
