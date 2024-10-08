import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class  NavigationServer {
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 1024;
    private static final int ALLOWED_CONNECTIONS_NUMBER = 2;
    
    private Selector selector;
    private Map<String, PeerInfo> peersPrints;
    private ArrayList<SocketChannel> existingChannels = new ArrayList<>();

    private static final int idSize = 32;
    private static final int hostSize = 45;
    private PeerInfo newPeer;

    public NavigationServer() throws IOException {
        selector = Selector.open();
        peersPrints = new HashMap<>();
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
                        handlePeerAccept(key);
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

    private void handlePeerAccept(SelectionKey key) throws IOException {

        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocket.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);

        if(existingChannels.size() >= ALLOWED_CONNECTIONS_NUMBER){ // number of allowed peers must be extended
            ByteBuffer buffer = ByteBuffer.wrap("EXIT".getBytes());
            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
            socketChannel.shutdownOutput();
            socketChannel.close();

            System.out.println("More than allowed peers number ");
            return;
        }

        InetSocketAddress peerAddress = (InetSocketAddress) socketChannel.getRemoteAddress();

        System.out.println("New peer connected: " + peerAddress);
    }

    private void sendExistingPeers(SocketChannel channel) throws IOException {

        int totalPeers = peersPrints.size();
        ByteBuffer buffer = ByteBuffer.allocate(totalPeers * (hostSize + idSize + Integer.BYTES));

        for(var peer : peersPrints.entrySet()) {

            if(channel.socket().getPort() == peer.getValue().navigationPort.get()) {
                continue;
            }

            byte[] paddedIdBytes = Arrays.copyOf(peer.getValue().name.getBytes(), 32); //pad to 32
            buffer.put(paddedIdBytes);
            buffer.putInt(peer.getValue().port);
            byte[] paddedHostBytes = Arrays.copyOf(peer.getValue().host.getBytes(), 45); // pad to 45 bytes
            buffer.put(paddedHostBytes);
        }

        buffer.flip();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        buffer.clear();
    }

    private void sendNewPeerToExisting() throws IOException {
        
        for(var channel : existingChannels) {

            var buffer = getBytesFromPeerPrint(newPeer);

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

                handleClosedConnection(socketChannel);

                return;
            }

            extractPeerPrints(socketChannel, buffer);

            sendExistingPeers(socketChannel);

            sendNewPeerToExisting();

            existingChannels.add(socketChannel);

        } catch (IOException e) {
            try {
                socketChannel.close();
            } catch (IOException closeException) {
                closeException.printStackTrace();
            }
            System.out.println("Peer connection reset.");
            handleClosedConnection(socketChannel);
        }
    }

    private void extractPeerPrints(SocketChannel socketChannel, ByteBuffer buffer) throws IOException {
        buffer.flip();

        if (buffer.remaining() >= idSize + Integer.BYTES + hostSize) {
            byte[] data32Bytes = new byte[idSize];
            buffer.get(data32Bytes);
            String peerId = new String(data32Bytes);

            int port = buffer.getInt();

            byte[] hostBytes = new byte[hostSize];
            buffer.get(hostBytes);
            String host = new String(hostBytes);

            var navPort = socketChannel.socket().getPort();

            newPeer = new PeerInfo(host, port, peerId, Optional.of(navPort));

            peersPrints.put(peerId, newPeer);

            System.out.println("Peer ID: " + peerId);
            System.out.println("Port: " + port);
            System.out.println("Host: " + host);
        } else {
            System.out.println("Not enough data available in buffer.");
        }

        buffer.clear();
    }

    private ByteBuffer getBytesFromPeerPrint(PeerInfo peerInfo){
        ByteBuffer buffer = ByteBuffer.allocate(idSize + Integer.BYTES + hostSize);
        buffer.put(peerInfo.name.getBytes());
        buffer.putInt(peerInfo.port);
        byte[] paddedHostBytes = Arrays.copyOf(peerInfo.host.getBytes(), 45); // pad to 45 bytes
        buffer.put(paddedHostBytes);
        buffer.flip();

        return buffer;
    }

    private void handleClosedConnection(SocketChannel socketChannel) throws IOException {

        Iterator<Map.Entry<String, PeerInfo>> iterator = peersPrints.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, PeerInfo> entry = iterator.next();
            if (entry.getValue().navigationPort.get() == socketChannel.socket().getPort()) {
                iterator.remove();
                System.out.println("Peer removed from peer prints, port: " + entry.getValue().port);
            }
        }

        existingChannels.remove(socketChannel);

        System.out.println("Peer removed from existing channels, port: " + socketChannel.socket().getPort());
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
