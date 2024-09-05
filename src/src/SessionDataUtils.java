import java.net.InetSocketAddress;
import java.util.UUID;

public class SessionDataUtils {

    public static final int HostSize = 45; //padding space for IPv6

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
}
