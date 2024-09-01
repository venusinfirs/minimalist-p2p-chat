import java.util.concurrent.ConcurrentLinkedQueue;

public class SharedResources {
    public static ConcurrentLinkedQueue<PeerInfo> newPeersQueue = new ConcurrentLinkedQueue<>();
}

