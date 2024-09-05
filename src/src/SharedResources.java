import java.net.InetAddress;

import java.util.concurrent.ConcurrentHashMap;

public class SharedResources {
    private static ConcurrentHashMap<String, PeerInfo> peersMap = new ConcurrentHashMap<>();

    public static void addPeer(String address, int port, String peerName) {
        var peer = new PeerInfo(address, port, peerName);
        peersMap.put(peerName, peer);
    }

    public static PeerInfo removePeer(String key) {
        return peersMap.remove(key);
    }

    public static PeerInfo getPeer(String key) {
        return peersMap.get(key);
    }

    public static ConcurrentHashMap<String, PeerInfo> getAllPeers() {
        return peersMap;
    }
}


