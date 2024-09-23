package com.p2pChat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;


public class ServerConnection implements Runnable {

    private static final String SERVER_ADDRESS = "localhost";//"10.0.2.2";
    private static final int NAVIGATION_SERVER_PORT = 12345;
    private static final int BUFFER_SIZE = 1024;

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
        }
        else {
            System.out.println("Failed to connect to the server.");
        }
    }

    private void sendPeerInfo(SocketChannel channel) throws IOException {
        String peerId = "placeholder_name";//SharedResources.getUserName();
        var port = SessionDataUtils.getPeerPort();
        var host = SessionDataUtils.getPeerHostAddress();

        ByteBuffer buffer = ByteBuffer.allocate(SessionDataUtils.PeerIdLength + Integer.BYTES + SessionDataUtils.HostSize);

        byte[] paddedIdBytes = Arrays.copyOf(peerId.getBytes(), SessionDataUtils.PeerIdLength); // pad to 32 bytes
        buffer.put(paddedIdBytes);

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

        try {
            int bytesRead = channel.read(buffer);
            if (bytesRead == -1) {
                channel.close();
                return;
            }

            buffer.flip();
            extractPeers(buffer);

            ConnectionEventsManager.notifyOnConnection();

        }catch (IOException e) {
            System.out.println("Connection lost: " + e.getMessage());
            channel.close();
        }
    }

    private synchronized void extractPeers(ByteBuffer buffer) throws IOException {
        while (buffer.remaining() >= SessionDataUtils.HostSize + Integer.BYTES + SessionDataUtils.PeerIdLength) {

            byte[] idBytes = new byte[SessionDataUtils.PeerIdLength];
            buffer.get(idBytes);
            String peerId = new String(idBytes);

            int port = buffer.getInt();

            byte[] hostBytes = new byte[SessionDataUtils.HostSize];
            buffer.get(hostBytes);
            String host = new String(hostBytes);

            SharedResources.addPeer(host, port, peerId);

            System.out.println("[ServerConnection] Peer received: id " + peerId + ",port: " + port + ",host: " + host);
        }
    }
}

