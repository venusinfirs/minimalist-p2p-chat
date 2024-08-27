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
	
	int addressSize = 4; 
	int portSize = 4;   

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
   
        // Send the list of connected peers to the new peer
		sendExistingPeers(socketChannel);
		peers.put(socketChannel, peerAddress);
		
        System.out.println("New peer connected: " + peerAddress);
    }
	
	private void sendExistingPeers(SocketChannel newSocketChannel) throws IOException{
		
		if(peers.isEmpty()){
		    return;
		}

        int totalPeers = peers.size();		
		ByteBuffer buffer = ByteBuffer.allocate(totalPeers * (addressSize + portSize));
		
		 // Fill the buffer with all peer addresses and ports
		for (Map.Entry<SocketChannel, InetSocketAddress> entry : peers.entrySet()) {
			InetSocketAddress peerAddress = entry.getValue();
			buffer.put(peerAddress.getAddress().getAddress()); 
			buffer.putInt(peerAddress.getPort());              
		}
		
		buffer.flip(); 
		newSocketChannel.write(buffer);
        buffer.rewind();
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
            while (buffer.hasRemaining()) {
                for (SocketChannel peer : peers.keySet()) {
                    if (peer != socketChannel) {
                        peer.write(buffer.duplicate());
                    }
                }
            }
        } catch (IOException e) {
            // Handle the exception here, typically by removing the peer and closing the socket
            InetSocketAddress address = peers.remove(socketChannel);
            try {
                socketChannel.close();
            } catch (IOException closeException) {
                closeException.printStackTrace();
            }
            System.out.println("Peer connection reset: " + address);
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
