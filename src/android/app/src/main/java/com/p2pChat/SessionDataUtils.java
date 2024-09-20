package com.p2pChat;

import java.net.InetSocketAddress;
import java.util.UUID;

public class SessionDataUtils {

    public static final int HostSize = 45; //padding space for IPv6
    public static final int PeerIdLength = 32;

    private static InetSocketAddress localServerAddress;

    public static String generateHexId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replaceAll("-", "");
    }

    public static void setServerAddress(InetSocketAddress address) {
        localServerAddress = address;
    }

    public static int getPeerPort() {
        return localServerAddress.getPort();
    }

    public static String getPeerHostAddress() {
        return localServerAddress.getAddress().getHostAddress();
    }

    public static String getUserNameByPort(int port) {
        var peers = SharedResources.getAllPeers();
        var name = peers.get(port);

        if (name == null) {
            System.out.println("[SessionDataUtils] No user name associated with port: " + port);
            return null;
        }
        return name.name;
    }
}

