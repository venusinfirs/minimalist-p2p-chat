import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class  NavigationServer {
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 1024;
    
    private Selector selector;
    private Map<String, PeerInfo> peers; //it's better to transform map to InetSocketAddress arrayList
    // when i'll get rid of message broadcasting
    private ArrayList<SocketChannel> existingChannels = new ArrayList<>();

    private static final int idSize = 32;
    private static final int hostSize = 45;
    private PeerInfo newPeer;

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

        existingChannels.add(socketChannel);

        trySendExistingPeers(socketChannel);
        sendNewPeerToExisting();

        InetSocketAddress peerAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
        
        System.out.println("New peer connected: " + peerAddress);
    }

    private void trySendExistingPeers(SocketChannel channel) throws IOException {
        if (peers.isEmpty()) {
            System.out.println("[NavigationServer] No peers found");
            return;
        }

        int totalPeers = peers.size();
        ByteBuffer buffer = ByteBuffer.allocate(totalPeers * (hostSize + idSize + Integer.BYTES));

        for(var peer : peers.entrySet()) { // this part of code is reused
            buffer.put(peer.getKey().getBytes());
            buffer.putInt(peer.getValue().port);
            byte[] paddedHostBytes = Arrays.copyOf(peer.getValue().host.getBytes(), 45); // pad to 45 bytes
            buffer.put(paddedHostBytes);
        }

        buffer.flip();
        channel.write(buffer);
    }

    private void sendNewPeerToExisting() throws IOException {
        if (existingChannels.size() <= 1) {
            return;
        }
        for(var channel : existingChannels) {

            var buffer = getPeerInfoBytes(newPeer);

            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            buffer.clear();
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(idSize + Integer.BYTES + hostSize);

        try {
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead == -1) {
                socketChannel.close();
                System.out.println("Peer disconnected.");
                return;
            }

            buffer.flip();

            if (buffer.remaining() >= idSize + Integer.BYTES + hostSize) {
                byte[] data32Bytes = new byte[idSize];
                buffer.get(data32Bytes);
                String peerId = new String(data32Bytes);

                int port = buffer.getInt();

                byte[] hostBytes = new byte[hostSize];
                buffer.get(hostBytes);
                String host = new String(hostBytes);

                newPeer = new PeerInfo(host, port, peerId);

                peers.put(peerId, newPeer);

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

    private ByteBuffer getPeerInfoBytes(PeerInfo peerInfo){
        ByteBuffer buffer = ByteBuffer.allocate(idSize + Integer.BYTES + hostSize);
        buffer.put(peerInfo.id.getBytes());
        buffer.putInt(peerInfo.port);
        byte[] paddedHostBytes = Arrays.copyOf(peerInfo.host.getBytes(), 45); // pad to 45 bytes
        buffer.put(paddedHostBytes);
        buffer.flip();

        return buffer;
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
