import java.io.IOException;

public interface ConnectionListener {

    void onNewConnection() throws IOException;
}
