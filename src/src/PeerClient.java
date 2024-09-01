import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PeerClient {

    private static final int BUFFER_SIZE = 1024;
    
    private static final String PEERS_LIST_MARKER = "PEERS";
    private Selector selector;
    private SocketChannel serverChannel;

    private LinkedList<InetSocketAddress> peers = new LinkedList<>();

    public PeerClient() throws IOException {

    }

    public void start() {
        new Thread(this::handleInput).start();

        while (true) {
            try {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    while (!SharedResources.newPeersQueue.isEmpty()) {
                        getPeers();
                    }

                    if (key.isConnectable()) {
                       // handleConnect(key);
                    } else if (key.isReadable()) {
                        //handleRead(key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getPeers() throws IOException {
        System.out.println("Try get peers from shared res");
        ConcurrentLinkedQueue<PeerInfo> peerInfo = SharedResources.newPeersQueue;

        for (PeerInfo element : peerInfo) {
            System.out.println("Connected peer: " + element.address.getAddress().getHostAddress()
                    + ":" + element.address.getPort());;
        }

    }

    private void handleInput() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String message = scanner.nextLine();
            try {
                ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                serverChannel.write(buffer); //open server channels for all the peers
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
