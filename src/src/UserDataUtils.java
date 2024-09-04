import java.util.UUID;

public class UserDataUtils {

    public static String generateHexId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replaceAll("-", "");
    }
}
