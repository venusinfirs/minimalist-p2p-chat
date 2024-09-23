package com.p2pChat;

import java.io.IOException;

public interface ConnectionListener {

    void onNewConnection() throws IOException;

    void onRead(String message) throws IOException;

    void onWrite(String message) throws IOException;
}

