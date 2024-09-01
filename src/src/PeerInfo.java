import java.net.InetSocketAddress;

public class PeerInfo{
    public InetSocketAddress address;

    public PeerInfo(InetSocketAddress peerAddress) {
        address = peerAddress;
    }
}