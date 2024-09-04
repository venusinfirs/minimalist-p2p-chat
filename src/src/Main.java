import java.io.IOException;

public class Main{

    public static void main(String[] args) {
        try {
            var server = new ServerConnection();
            Peer client = new Peer();
            server.start();
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}