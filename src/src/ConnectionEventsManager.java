import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionEventsManager {

    private static List<ConnectionListener> listeners = new ArrayList<>();

    public static synchronized void addListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    public static synchronized void notifyOnConnection() throws IOException {
        for (ConnectionListener listener : listeners) {
            listener.onNewConnection();
        }
    }
}
