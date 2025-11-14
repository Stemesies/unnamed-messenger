import java.util.Scanner;
import network.SimpleSocket;

public class Client {

    private final String host;
    private final int port;

    private SimpleSocket socket = null;
    private final Scanner in = new Scanner(System.in);

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    boolean isConnected() {
        return socket != null;
    }

    boolean isDisconnected() {
        return socket == null;
    }

    /**
     * Создает, если это возможно, соединение с сервером и начинает прослушивать сообщения.
     */
    public void connect() {
        socket = new SimpleSocket(host, port);
        if (socket.isClosed())
            socket = null;
        else {
            System.out.println("Connected to the server");
            processConnection();
        }
    }

    public void exit() {
        disconnect();
        System.exit(0);
    }

    /**
     * Разрывает соединение с сервером, если таковое имеется. <br>
     * Все потоки, работающие с ним также будут автоматически остановлены.
     */
    public void disconnect() {
        if (socket == null)
            return;

        socket.close();
        socket = null;
        System.out.println("Disconnected from the server");
    }

    private void processInput() {
        while (true) {

            if (!in.hasNextLine()) {
                exit();
                return;
            }
            var msg = in.nextLine();

            if (msg.equals("/retry") && isDisconnected()) {
                connect();
                continue;
            } else if (msg.equals("/exit")) {
                exit();
                return;
            }

            if (isConnected())
                socket.sendMessage(msg);
            else
                System.err.println("Not connected to server.");
        }
    }

    private void processConnection() {
        new Thread(() -> {
            while (isConnected()) {
                if (socket.hasNewMessage())
                    System.out.println(socket.receiveMessage());
                else {
                    disconnect();
                    break;
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        var client = new Client("127.0.0.1", 8080);
        client.connect();
        client.processInput();
    }
}
