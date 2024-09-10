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

        System.out.println("[Peer] Listening on port " + SessionDataUtils.getPeerPort());
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
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onNewConnection() throws IOException {
        updateConnections();
        System.out.println("New connection");
    }

    private void updateConnections() throws IOException {

        System.out.println("[Peer] Updating connections");

        var peers = SharedResources.getAllPeers();

        //currentConnections.clear();

        for (var peer : peers.entrySet()){
            System.out.println("[Peer] trying to create a new connection " + peer.getValue().port + peer.getValue().host);
            createNewConnection(peer.getValue());
        }
    }

    private void createNewConnection(PeerInfo peer) throws IOException {
        var peerChannel = SocketChannel.open();
        peerChannel.configureBlocking(false);
        peerChannel.connect(new InetSocketAddress("127.0.0.1", peer.port)); //temporal solution - i need to support IPv6 somehow as well as IPv4
        peerChannel.register(selector, SelectionKey.OP_CONNECT);

        System.out.println("Peer channel created " + peer.host + ":" + peer.port);

        currentConnections.put(peer.id ,peerChannel); //replace to handleAccept()
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocket.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);


        System.out.println("New peer connected: " + socketChannel.getRemoteAddress());
    }

    private void handleConnect(SelectionKey key) throws IOException {

        var peerChannel = (SocketChannel) key.channel();
        if (peerChannel.finishConnect()) {
            peerChannel.register(selector, SelectionKey.OP_READ);
            System.out.println("[Peer] handle connect " + peerChannel.socket().getRemoteSocketAddress());
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        System.out.println("[Peer] Handle read");

        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) {
            channel.close();
            return;
        }

        buffer.flip();
        var message = new String(buffer.array());

        System.out.println("Message received: " + message);
    }


    private void handleInput(){

        Scanner scanner = new Scanner(System.in);

        while (true) {
            String message = scanner.nextLine();

             try {
                 ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                 for(var channel : currentConnections.values()){

                     if (channel.isConnectionPending()) {
                         channel.finishConnect(); 
                     }

                     channel.write(buffer);
                 }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
