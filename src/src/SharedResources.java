import java.util.concurrent.ConcurrentHashMap;

public class SharedResources {
    private static String userName;

    private static ConcurrentHashMap<Integer, PeerInfo> connectedPeers = new ConcurrentHashMap<>();

    public static void addPeer(String host, int port, String peerName) {
        var peer = new PeerInfo(host, port, peerName, null);
        connectedPeers.put(port, peer);
        System.out.println("[SharedResources] Added peer " + peerName + " to " + host + ":" + port);
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        SharedResources.userName = userName;
    }

    public static PeerInfo removePeer(String key) {
        return connectedPeers.remove(key);
    }

    public static PeerInfo getPeer(String key) {
        return connectedPeers.get(key);
    }

    public static ConcurrentHashMap<Integer, PeerInfo> getAllPeers() {
        return connectedPeers;
    }
}


