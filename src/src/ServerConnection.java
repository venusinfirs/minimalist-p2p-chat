import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

public class ServerConnection extends Thread {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int NAVIGATION_SERVER_PORT = 12345;
    private static final int BUFFER_SIZE = 1024;
    private static final String PEERS_LIST_MARKER = "PEERS";

    private Selector selector;
    private SocketChannel peerChannel;

    private LinkedList<InetSocketAddress> peers = new LinkedList<>();

    public ServerConnection() throws IOException {
        ConnectToNavServer();
    }
    private void ConnectToNavServer() throws IOException {
        selector = Selector.open();
        peerChannel = SocketChannel.open();
        peerChannel.configureBlocking(false);
        peerChannel.connect(new InetSocketAddress(SERVER_ADDRESS, NAVIGATION_SERVER_PORT));
        peerChannel.register(selector, SelectionKey.OP_CONNECT);
        
        SessionDataUtils.LocalAddress = (InetSocketAddress) peerChannel.getLocalAddress();
    }

    @Override
    public void run() {

        System.out.println("Running server connection...");

        while (true) {
            try {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (key.isConnectable()) {
                        handleConnect(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleConnect(SelectionKey key) throws IOException {

        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.finishConnect()) {
            channel.register(selector, SelectionKey.OP_READ);
            System.out.println("Connected to the server.");
        }
    }

    private void handleRead(SelectionKey key) throws IOException { //seems that it's not necessary to pass selection key here each time
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        System.out.println("Handle read");

        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) {
            channel.close();
            return;
        }

        buffer.flip();

        getPeers(buffer); // may be it's better to move this call to handle input

        if (buffer.hasRemaining()) { //need to be removed
            byte[] remainingBytes = new byte[buffer.remaining()];
            buffer.get(remainingBytes);
            String message = new String(remainingBytes);
            System.out.println("Received message: " + message);
        }
    }

    private synchronized void getPeers(ByteBuffer buffer) throws IOException {
        if (buffer.remaining() >= 5) {

            System.out.println("Try get peers");
            var buffCopy = buffer.duplicate();
            var markerLength = PEERS_LIST_MARKER.length();

            buffCopy.limit(markerLength);
            byte[] bytes = new byte[markerLength];
            buffCopy.get(bytes);

            var markerString = new String(bytes);

            if (PEERS_LIST_MARKER.equals(markerString)) {

                buffer.position(5);
                while (buffer.remaining() >= 8) {
                    byte[] addressBytes = new byte[4];
                    buffer.get(addressBytes);
                    int port = buffer.getInt();

                    InetAddress ipAddress = InetAddress.getByAddress(addressBytes);
                    InetSocketAddress peerAddress = new InetSocketAddress(ipAddress, port);

                    if(!peerAddress.equals(peerChannel.getLocalAddress())){
                        //peers.add(peerAddress);
                        SharedResources.addPeer(ipAddress,port);
                        //System.out.println("Connected peer: " + peerAddress
                          //     + ", serverChannel.getLocalAddress: " + serverChannel.getLocalAddress());
                    }
                }
            }
        }
    }
}
