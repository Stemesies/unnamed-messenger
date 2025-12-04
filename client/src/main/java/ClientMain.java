import cli.CommandProcessor;
import network.SimpleSocket;

import java.util.Scanner;

public class ClientMain {

    private final String host;
    private final int port;

    private final CommandProcessor commandProcessor = new CommandProcessor();
    private SimpleSocket socket = null;
    private final Scanner in = new Scanner(System.in);

    public ClientMain(String host, int port) {
        this.host = host;
        this.port = port;
        registerCommands();
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

            if (msg.charAt(0) == '/') {
                var procError = commandProcessor.execute(msg);
                if (procError != null)
                    procError.explain();

                continue;
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

    private void registerCommands() {
        commandProcessor.register("exit", (it) -> it
            .executes(this::exit)
        );
        commandProcessor.register("retry", (it) -> it
            .require("Already connected.", this::isDisconnected)
            .executes(this::connect)
        );

    }

    public static void main(String[] args) {
        var client = new ClientMain("127.0.0.1", 8080);
        client.connect();
        client.processInput();
    }
}
