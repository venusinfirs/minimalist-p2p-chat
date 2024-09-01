import java.io.IOException;

public class Main{

    public static void main(String[] args) {
        try {
            var server = new ServerConnection();
            PeerClient client = new PeerClient();
            server.start();
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}