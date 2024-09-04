import java.net.InetSocketAddress;
import java.util.UUID;

public class SessionDataUtils {

    public static InetSocketAddress LocalAddress;

    public static String generateHexId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replaceAll("-", "");
    }
}
