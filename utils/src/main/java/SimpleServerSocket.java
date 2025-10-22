import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;

public class SimpleServerSocket implements Closeable {
    ServerSocket serverSocket;

    private boolean isClosed = false;

    public SimpleServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public  SimpleServerSocket(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Error creating server socket: " + e.getMessage());
            close();
        }
    }

    public SimpleSocket accept() {
        try {
            return new SimpleSocket(serverSocket.accept());
        } catch (IOException e) {
            System.err.println("Error accepting client: " + e.getMessage());
            return null;
        }
    }

    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void close() {
        isClosed = true;
        try {
            serverSocket.close();
        } catch (IOException ignored) {}
        serverSocket = null;
    }
}
