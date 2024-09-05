import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class  NavigationServer {
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 1024;
    private static final String PEERS_LIST_MARKER = "PEERS";
    
    private Selector selector;
    private Map<String, PeerInfo> peers; //it's better to transform map to InetSocketAddress arrayList
    // when i'll get rid of message broadcasting

    private static final int idSize = 32;
    private static final int addressSize = 45;

    public NavigationServer() throws IOException {
        selector = Selector.open();
        peers = new HashMap<>();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(PORT));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void start() {
        System.out.println("Peer server started...");
        while (true) {
            try {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocket.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);

        InetSocketAddress peerAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
        
        System.out.println("New peer connected: " + peerAddress);
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(idSize + Integer.BYTES + addressSize);

        try {
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead == -1) {
                socketChannel.close();
                System.out.println("Peer disconnected.");
                return;
            }

            buffer.flip();

            // Check if there is enough data available in the buffer
            if (buffer.remaining() >= idSize + Integer.BYTES + addressSize) {
                byte[] data32Bytes = new byte[idSize];
                buffer.get(data32Bytes);
                String peerId = new String(data32Bytes);

                int port = buffer.getInt();

                byte[] hostBytes = new byte[addressSize];
                buffer.get(hostBytes);
                String host = new String(hostBytes);

                peers.put(peerId, new PeerInfo(host, port, peerId));
                System.out.println("Peer ID: " + peerId);
                System.out.println("Port: " + port);
                System.out.println("Host: " + host);
            } else {
                System.out.println("Not enough data available in buffer.");
            }

            buffer.clear();

        } catch (IOException e) {
            try {
                socketChannel.close();
            } catch (IOException closeException) {
                closeException.printStackTrace();
            }
            System.out.println("Peer connection reset.");
        }
    }



    public static void main(String[] args) {
        try {
            NavigationServer server = new NavigationServer();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
