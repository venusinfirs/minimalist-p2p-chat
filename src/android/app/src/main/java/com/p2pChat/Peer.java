package com.p2pChat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;


public class Peer extends Thread implements ConnectionListener {

    private static final int BUFFER_SIZE = 1024;
    private Selector selector;

    private HashMap<Integer,SocketChannel> currentConnections = new HashMap<>();

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
    }

    private void updateConnections() throws IOException {

        var peers = SharedResources.getAllPeers();

        for (var peer : peers.entrySet()){
            createNewConnection(peer.getValue());
        }
    }

    private void createNewConnection(PeerInfo peer) throws IOException {
        var peerChannel = SocketChannel.open();
        peerChannel.configureBlocking(false);
        peerChannel.connect(new InetSocketAddress("127.0.0.1", peer.port)); //temporal solution - i need to support IPv6 somehow as well as IPv4
        peerChannel.register(selector, SelectionKey.OP_CONNECT);

        currentConnections.put(peerChannel.socket().getPort(), peerChannel);
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocket.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void handleConnect(SelectionKey key) throws IOException {

        var peerChannel = (SocketChannel) key.channel();
        if (peerChannel.finishConnect()) {
            peerChannel.register(selector, SelectionKey.OP_READ);
        }

        System.out.println(SessionDataUtils.getUserNameByPort(peerChannel.socket().getPort()) + " entered chat");
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) {
            channel.close();
            return;
        }

        buffer.flip();
        var message = new String(buffer.array());

        System.out.println("Message received: " + message);
    }


    public void handleInput(String message){

        while (true) {

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
