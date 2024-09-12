import java.util.Scanner;

public class UserLogin extends Thread {

    @Override
    public void run() {
        getUserName();
    }

    private void getUserName(){

        System.out.println("Enter username: ");

        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        SharedResources.setUserName(input); //name validation is needed here
    }
}
