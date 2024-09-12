import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Main{

    public static void main(String[] args) {

        try {
            var userLogin = new UserLogin();
            userLogin.start();
            userLogin.join();

            var server = new ServerConnection();
            Peer client = new Peer();
            server.start();
            client.start();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}