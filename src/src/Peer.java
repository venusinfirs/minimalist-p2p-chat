import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class Peer extends Thread implements ConnectionListener {

    private static final int BUFFER_SIZE = 1024;
    
    private static final String PEERS_LIST_MARKER = "PEERS";
    private Selector selector;
    private SocketChannel serverChannel;

    private LinkedList<InetSocketAddress> peers = new LinkedList<>();
    private HashMap<String,SocketChannel> currentConnections = new HashMap<String,SocketChannel>();

    public Peer() throws IOException {
        selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(0));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        SessionDataUtils.setServerAddress((InetSocketAddress) serverSocket.getLocalAddress());

        ConnectionEventsManager eventManager = new ConnectionEventsManager();

        eventManager.addListener(this);

       // System.out.println("[Peer] Listening on port " + SessionDataUtils.getPeerPort());
    }

    @Override
    public void run() {
        new Thread(this::handleInput).start();
        //System.out.println("Running peer client...");

        while (true) {
            try {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (key.isConnectable()) {
                        handleConnect(key);
                    } else if (key.isAcceptable()) {

                    } else if (key.isReadable()) {
                        handleRead(key);
                    } else if (key.isAcceptable()) {
                        //handleAccept();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onNewConnection() {
        System.out.println("New connection");
    }

    private void updateConnections() throws IOException {
        //initiate peer connections here after receiving peers list
        var peers = SharedResources.getAllPeers();

        currentConnections.clear();

        for (var peer : peers.entrySet()){
            if(currentConnections.containsKey(peer.getKey())){
                continue;
            }
            createNewConnection(peer.getValue());
        }
    }

    private void createNewConnection(PeerInfo peer) throws IOException {
        var peerChannel = SocketChannel.open();
        peerChannel.configureBlocking(false);
        peerChannel.connect(new InetSocketAddress(peer.host, peer.port));
        currentConnections.put(peer.id ,peerChannel);
    }

    private void handleRead(SelectionKey key){

    }

    private void handleConnect(SelectionKey key) throws IOException {

    }

    private void handleInput(){

       /* Scanner scanner = new Scanner(System.in);

        while (true) {
            String message = scanner.nextLine();

             try {
                 ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                 //peerChannel.write(buffer);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }
}
