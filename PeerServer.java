import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class PeerServer {
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 1024;
    private Selector selector;
    private Map<SocketChannel, InetSocketAddress> peers;

    public PeerServer() throws IOException {
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
        peers.put(socketChannel, peerAddress);
        
        System.out.println("New peer connected: " + peerAddress);
    }

    private void handleRead(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        try {
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead == -1) {
                InetSocketAddress address = peers.remove(socketChannel);
                socketChannel.close();
                System.out.println("Peer disconnected: " + address);
                return;
            }

            buffer.flip();
            broadcastMessage(buffer, socketChannel);
        } catch (IOException e) {
            InetSocketAddress address = peers.remove(socketChannel);
            try {
                socketChannel.close();
            } catch (IOException closeException) {
                closeException.printStackTrace();
            }
            System.out.println("Peer connection reset: " + address);
        }
    }

    private void broadcastMessage(ByteBuffer buffer, SocketChannel senderChannel) throws IOException {
        for (SocketChannel peer : peers.keySet()) {
            if (peer != senderChannel) {
                peer.write(buffer.duplicate());
            }
        }
    }

    public static void main(String[] args) {
        try {
            PeerServer server = new PeerServer();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
