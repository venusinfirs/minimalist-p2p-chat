import java.util.Optional;

public class PeerInfo{
    public int port;
    public String host;
    public String id;
    public Optional<Integer> navigationPort;

    public PeerInfo(String address, int port, String id, Optional<Integer> navigationPort) {
        this.port = port;
        this.host = address;
        this.id = id;
        this.navigationPort = navigationPort;
    }
}