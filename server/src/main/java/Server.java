import java.util.ArrayList;
import java.util.Scanner;
import network.SimpleServerSocket;
import network.SimpleSocket;
import utils.Utils;

public class Server {
    SimpleServerSocket socket = null;
    private final Scanner in = new Scanner(System.in);

    private final ArrayList<SimpleSocket> clients = new ArrayList<>();

    public void start() {
        socket = new SimpleServerSocket(8080);
        if (socket.isClosed())
            return;

        System.out.println("Server started");

        acceptClients();
        processInput();
    }

    private void acceptClients() {
        new Thread(() -> {
            while (socket != null) {
                var client = socket.accept();
                if (client == null)
                    continue;
                System.out.println("new client");
                clients.add(client);
                processClient(client);
            }
        }).start();
    }

    public void stop() {
        broadcast("Closing server...");

        new ArrayList<>(clients).forEach(SimpleSocket::close);
        System.out.println("Server closed.");
        socket.close();
        System.exit(0);
    }

    /**
     * Вещает сообщение всем подключенным пользователям.
     *
     * @param message сообщение для вещания
     */
    public void broadcast(String message) {
        for (var client : clients)
            client.sendMessage(message);
    }

    /**
     * Вещает сообщение всем подключенным пользователям, кроме указанного.
     *
     * @param message сообщение для вещания
     * @param excludedClient клиент, которому не следует присылать сообщение
     */
    public void broadcast(String message, SimpleSocket excludedClient) {
        for (var client : clients)
            if (!client.equals(excludedClient))
                client.sendMessage(message);
    }

    /**
     * Создает и запускает новый поток для обработки клиента
     * (получение и отправка сообщений).<br>
     * Данный поток живет до тех пор, пока не будет разорвано соединение.
     */
    public void processClient(SimpleSocket client) {
        new Thread(() -> {
            System.out.printf("Client %s connected\n", client);

            while (client.hasNewMessage()) {
                var line = client.receiveMessage();
                var chatMessage = Utils.createChatMessage(client, line);

                System.out.println(chatMessage);

                client.sendMessage(Utils.createChatMessage("you", line));
                broadcast(chatMessage, client);
            }

            clients.remove(client);
            System.out.printf("Client %s disconnected\n", client);
        }).start();
    }

    private void processInput() {
        while (true) {
            if (!in.hasNextLine()) {
                stop();
                return;
            }
            var msg = in.nextLine();

            if (msg.equals("/exit")) {
                stop();
                return;
            }
        }
    }

    public static void main(String[] args) {
        new Server().start();
    }
}
