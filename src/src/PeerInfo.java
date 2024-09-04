import java.net.InetAddress;
import java.net.InetSocketAddress;

public class PeerInfo{
    public int port;
    public InetAddress address;
    public String id;

    public PeerInfo(InetAddress address, int port, String id) {
        this.port = port;
        this.address = address;
        this.id = id;
    }
}