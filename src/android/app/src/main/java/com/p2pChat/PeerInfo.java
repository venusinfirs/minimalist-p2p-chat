package com.p2pChat;

import java.util.Optional;

public class PeerInfo{
    public int port;
    public String host;
    public String name;
    public Optional<Integer> navigationPort;

    public PeerInfo(String host, int port, String id, Optional<Integer> navigationPort) {
        this.port = port;
        this.host = host;
        this.name = id;
        this.navigationPort = navigationPort;
    }
}
