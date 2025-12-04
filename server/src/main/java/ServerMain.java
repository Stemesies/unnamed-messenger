import elements.Client;
import network.SimpleServerSocket;
import utils.Utils;

import java.util.ArrayList;
import java.util.Scanner;

public class ServerMain {
    SimpleServerSocket socket = null;
    private final Scanner in = new Scanner(System.in);

    private final ArrayList<Client> clients = new ArrayList<>();

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
                var clSocket = socket.accept();
                if (clSocket == null)
                    continue;
                var client = new Client(clSocket);
                System.out.println("new client");
                clients.add(client);
                processClient(client);
            }
        }).start();
    }

    public void stop() {
        broadcast("Closing server...");

        new ArrayList<>(clients).forEach(Client::close);
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
    public void broadcast(String message, Client excludedClient) {
        for (var client : clients)
            if (client != excludedClient)
                client.sendMessage(message);
    }

    /**
     * Создает и запускает новый поток для обработки клиента
     * (получение и отправка сообщений).<br>
     * Данный поток живет до тех пор, пока не будет разорвано соединение.
     */
    public void processClient(Client client) {
        new Thread(() -> {
            System.out.printf("Client %s connected\n", client);

            while (client.hasNewMessage()) {
                var line = client.receiveMessage();

                if (line.charAt(0) == '/') {
                    var proc = ClientCommands.processor;
                    proc.execute(
                        line,
                        new ClientCommands.ClientContextData(client, client.user, null)
                    );

                    if (proc.getLastError() != null)
                        client.sendMessage(proc.getLastError());
                    else if (proc.getOutput() != null)
                        client.sendMessage(proc.getOutput());
                    continue;
                }

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
        ClientCommands.init();
        new ServerMain().start();
    }
}
