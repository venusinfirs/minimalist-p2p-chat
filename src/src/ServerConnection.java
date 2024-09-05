import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
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
                return;
            }
        }
    }

    private void handleConnect(SelectionKey key) throws IOException {

        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.finishConnect()) {
            sendPeerInfo(channel);
            channel.register(selector, SelectionKey.OP_READ);
            System.out.println("Connected to the server.");
        }
    }

    private void sendPeerInfo(SocketChannel channel) throws IOException {
        String peerId = SessionDataUtils.generateHexId();
        var port = SessionDataUtils.getPeerPort();
        var host = SessionDataUtils.getPeerHostAddress();

        int portSize = Integer.BYTES;

        System.out.println("[ServerConnection] Sending peer info: peer id " + peerId + ",port: "
                + port + ",host: " + SessionDataUtils.getPeerHostAddress());

        ByteBuffer buffer = ByteBuffer.allocate(peerId.length() + Integer.BYTES + SessionDataUtils.HostSize);
        buffer.put(peerId.getBytes());
        buffer.putInt(port);
        byte[] paddedHostBytes = Arrays.copyOf(host.getBytes(), 45); // pad to 45 bytes
        buffer.put(paddedHostBytes);
        buffer.flip();

        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }

        buffer.clear();

    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        System.out.println("Handle read");

        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) {
            channel.close();
            return;
        }

        buffer.flip();

        getPeers(buffer);
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
                    //SharedResources.addPeer(ipAddress,port);
                }
            }
        }
    }
}