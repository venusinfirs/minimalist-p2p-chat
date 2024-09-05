public class PeerInfo{
    public int port;
    public String host;
    public String id;

    public PeerInfo(String address, int port, String id) {
        this.port = port;
        this.host = address;
        this.id = id;
    }
}