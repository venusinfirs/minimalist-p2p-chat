package com.p2pChat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConnectionEventsManager {
    private static List<ConnectionListener> listeners = new ArrayList();

    private static ConnectionListener readListener;
    private static ConnectionListener writeListener;

    public ConnectionEventsManager() {
    }

    public static synchronized void addListener(ConnectionListener var0) {
        listeners.add(var0);
    }

    public static synchronized void addReadListener(ConnectionListener listener) {
        readListener = listener;
    }

    public static synchronized void addWriteListener(ConnectionListener listener) {
        writeListener = listener;
    }

    public static synchronized void notifyOnConnection() throws IOException {
        Iterator var0 = listeners.iterator();

        while(var0.hasNext()) {
            ConnectionListener var1 = (ConnectionListener)var0.next();
            var1.onNewConnection();
        }

    }

    public static synchronized void notifyOnRead(String message) throws IOException {
        Iterator var0 = listeners.iterator();

        while(var0.hasNext()) {
            ConnectionListener var1 = (ConnectionListener)var0.next();
            var1.onRead(message);
        }

    }

    public static synchronized void notifyOnWrite(String message) throws IOException {
        Iterator var0 = listeners.iterator();

        while(var0.hasNext()) {
            ConnectionListener var1 = (ConnectionListener)var0.next();
            var1.onRead(message);
        }

    }
}
