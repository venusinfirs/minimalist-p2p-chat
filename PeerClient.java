import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Scanner;

public class PeerClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final int BUFFER_SIZE = 1024;
    private Selector selector;
    private SocketChannel serverChannel;

    public PeerClient() throws IOException {
        selector = Selector.open();
        serverChannel = SocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.connect(new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT));
        serverChannel.register(selector, SelectionKey.OP_CONNECT);
    }

    public void start() {
        new Thread(this::handleInput).start();

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

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) {
            channel.close();
            return;
        }

        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        String message = new String(bytes);
        System.out.println("Received: " + message);
    }

    private void handleInput() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String message = scanner.nextLine();
            try {
                ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                serverChannel.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            PeerClient client = new PeerClient();
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
