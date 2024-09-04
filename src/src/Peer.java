import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.LinkedList;
import java.util.Scanner;

public class Peer extends Thread {

    private static final int BUFFER_SIZE = 1024;
    
    private static final String PEERS_LIST_MARKER = "PEERS";
    private Selector selector;
    private SocketChannel serverChannel;

    private LinkedList<InetSocketAddress> peers = new LinkedList<>();

    public Peer() throws IOException {

    }

    @Override
    public void run() {
        new Thread(this::handleInput).start();
        System.out.println("Running peer client...");


       /* while (true) {
            try {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();


                    if (key.isConnectable()) {
                       // handleConnect(key);
                    } else if (key.isReadable()) {
                        //handleRead(key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }  */
    }

    private void getPeers() throws IOException {
        System.out.println("Try get peers from shared res");

        SharedResources.getAllPeers().forEach((key, peer) -> System.out.println("[Peer] Getting peer from shared resources: " + key + ": " + peer.port));

    }

    private void handleInput(){
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String message = scanner.nextLine();

            if(message.equals(PEERS_LIST_MARKER)){
                try {
                    getPeers();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

           /* try {
                 ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                serverChannel.write(buffer); //open server channels for all the peers
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }
}
